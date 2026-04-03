package dev.waqas.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class SpelAuditMessageResolver {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    public String resolve(String spel, JoinPoint joinPoint, Object result) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        EvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("args", joinPoint.getArgs());
        ctx.setVariable("methodName", sig.getMethod().getName());
        if (result != null) {
            ctx.setVariable("result", result);
        }
        Expression expression = parser.parseExpression(spel);
        Object value = expression.getValue(ctx);
        return value != null ? value.toString() : "";
    }
}
