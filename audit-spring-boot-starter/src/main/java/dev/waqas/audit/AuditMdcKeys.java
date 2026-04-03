package dev.waqas.audit;

/**
 * Default MDC keys read by {@link MdcAuditContextProvider}.
 */
public final class AuditMdcKeys {

    public static final String USER_ID = "audit.userId";
    public static final String SESSION_ID = "audit.sessionId";
    public static final String CORRELATION_ID = "audit.correlationId";
    public static final String CLIENT_IP = "audit.clientIp";

    private AuditMdcKeys() {
    }
}
