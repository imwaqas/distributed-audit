package dev.waqas.audit;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcAuditContextProvider implements AuditContextProvider {

    @Override
    public Optional<String> userId() {
        return Optional.ofNullable(MDC.get(AuditMdcKeys.USER_ID));
    }

    @Override
    public Optional<String> sessionId() {
        return Optional.ofNullable(MDC.get(AuditMdcKeys.SESSION_ID));
    }

    @Override
    public Optional<String> correlationId() {
        return Optional.ofNullable(MDC.get(AuditMdcKeys.CORRELATION_ID));
    }

    @Override
    public Optional<String> clientIp() {
        return Optional.ofNullable(MDC.get(AuditMdcKeys.CLIENT_IP));
    }
}
