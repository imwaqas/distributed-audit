package dev.waqas.audit;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditableAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditableAspect.class);

    private final Auditor auditor;
    private final SpelAuditMessageResolver spelAuditMessageResolver;
    private final String applicationName;

    public AuditableAspect(
        Auditor auditor,
        SpelAuditMessageResolver spelAuditMessageResolver,
        @Value("${spring.application.name:unknown}") String applicationName) {
        this.auditor = auditor;
        this.spelAuditMessageResolver = spelAuditMessageResolver;
        this.applicationName = applicationName;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        audit(joinPoint, auditable, result, null);
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void afterFailure(JoinPoint joinPoint, Auditable auditable, Throwable ex) {
        audit(joinPoint, auditable, null, ex);
    }

    private void audit(JoinPoint joinPoint, Auditable auditable, Object result, Throwable error) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Object[] args = null;
        try {
            args = joinPoint.getArgs();
            String message;
            if (error == null) {
                message = spelAuditMessageResolver.resolve(auditable.successMessage(), joinPoint, result);
            } else if (StringUtils.isNotBlank(auditable.failureMessage())) {
                message = spelAuditMessageResolver.resolve(auditable.failureMessage(), joinPoint, null);
            } else {
                try {
                    message = "Exception on "
                        + spelAuditMessageResolver.resolve(auditable.successMessage(), joinPoint, null)
                        + " — "
                        + error.getMessage();
                } catch (Exception spelEx) {
                    message = "Exception in " + sig.getMethod().getName() + " — " + error.getMessage();
                }
            }
            String action = StringUtils.isBlank(auditable.action())
                ? sig.getMethod().getName()
                : auditable.action();
            AuditLevel level = error == null ? AuditLevel.INFO : AuditLevel.WARN;
            String responsePayload = null;
            if (error != null) {
                responsePayload = StringUtils.substring(ExceptionUtils.getStackTrace(error), 0, 300);
            }
            AuditEvent event = new AuditEvent.Builder()
                .message(message)
                .action(action)
                .level(level)
                .service(applicationName)
                .responsePayload(responsePayload)
                .build();
            auditor.accept(event);
        } catch (Exception ex) {
            log.error(
                "Unable to audit method {} args {}",
                sig.getMethod().getName(),
                args != null ? Arrays.deepToString(args) : "null",
                ex);
        }
    }
}
