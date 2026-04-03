package dev.waqas.audit;

import java.util.Optional;

/**
 * Supplies ambient context (user, session, correlation, client IP) when not set on the event.
 */
public interface AuditContextProvider {

    Optional<String> userId();

    Optional<String> sessionId();

    Optional<String> correlationId();

    Optional<String> clientIp();
}
