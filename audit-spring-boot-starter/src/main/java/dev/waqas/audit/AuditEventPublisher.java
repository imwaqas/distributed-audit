package dev.waqas.audit;

@FunctionalInterface
public interface AuditEventPublisher {

    void publish(AuditEvent event);
}
