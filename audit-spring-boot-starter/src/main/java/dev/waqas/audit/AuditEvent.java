package dev.waqas.audit;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Immutable audit payload published to the bus or logs. Field lengths are truncated in the builder.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = AuditEvent.Builder.class)
public class AuditEvent {

    private static final int SHORT_LIMIT = 50;
    private static final int MESSAGE_LIMIT = 100;
    private static final int PAYLOAD_LIMIT = 5000;

    private final String uuid;
    private final String user;
    private final String session;
    private final String correlationId;
    private final String clientIp;
    private final String host;
    private final String service;
    private final Instant createdAt;
    private final String action;
    private final String message;
    private final AuditLevel level;
    private final String customerId;
    private final String requestPayload;
    private final Instant requestAt;
    private final String responsePayload;
    private final Instant responseAt;

    private AuditEvent(
        String uuid,
        String user,
        String session,
        String correlationId,
        String clientIp,
        String host,
        String service,
        Instant createdAt,
        String action,
        String message,
        AuditLevel level,
        String customerId,
        String requestPayload,
        Instant requestAt,
        String responsePayload,
        Instant responseAt) {
        this.uuid = uuid;
        this.user = user;
        this.session = session;
        this.correlationId = correlationId;
        this.clientIp = clientIp;
        this.host = host;
        this.service = service;
        this.createdAt = createdAt;
        this.action = action;
        this.message = message;
        this.level = level;
        this.customerId = customerId;
        this.requestPayload = requestPayload;
        this.requestAt = requestAt;
        this.responsePayload = responsePayload;
        this.responseAt = responseAt;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUser() {
        return user;
    }

    public String getSession() {
        return session;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getHost() {
        return host;
    }

    public String getService() {
        return service;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public AuditLevel getLevel() {
        return level;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public Instant getRequestAt() {
        return requestAt;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public Instant getResponseAt() {
        return responseAt;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        @JsonProperty("uuid")
        private String uuid = UUID.randomUUID().toString();

        @JsonProperty("user")
        private String user;

        @JsonProperty("session")
        private String session;

        @JsonProperty("correlationId")
        private String correlationId;

        @JsonProperty("clientIp")
        private String clientIp;

        @JsonProperty("host")
        private String host;

        @JsonProperty("service")
        private String service;

        @JsonProperty("createdAt")
        private Instant createdAt = Instant.now();

        @JsonProperty("action")
        private String action;

        @JsonProperty("message")
        private String message;

        @JsonProperty("level")
        private AuditLevel level = AuditLevel.INFO;

        @JsonProperty("customerId")
        private String customerId;

        @JsonProperty("requestPayload")
        private String requestPayload;

        @JsonProperty("requestAt")
        private Instant requestAt;

        @JsonProperty("responsePayload")
        private String responsePayload;

        @JsonProperty("responseAt")
        private Instant responseAt;

        public Builder() {
        }

        public Builder(AuditEvent e) {
            this.uuid = e.uuid;
            this.user = e.user;
            this.session = e.session;
            this.correlationId = e.correlationId;
            this.clientIp = e.clientIp;
            this.host = e.host;
            this.service = e.service;
            this.createdAt = e.createdAt;
            this.action = e.action;
            this.message = e.message;
            this.level = e.level;
            this.customerId = e.customerId;
            this.requestPayload = e.requestPayload;
            this.requestAt = e.requestAt;
            this.responsePayload = e.responsePayload;
            this.responseAt = e.responseAt;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder user(String user) {
            this.user = abbreviate(user, SHORT_LIMIT);
            return this;
        }

        public Builder session(String session) {
            this.session = abbreviate(session, SHORT_LIMIT);
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = abbreviate(correlationId, SHORT_LIMIT);
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = abbreviate(clientIp, SHORT_LIMIT);
            return this;
        }

        public Builder host(String host) {
            this.host = abbreviate(host, SHORT_LIMIT);
            return this;
        }

        public Builder service(String service) {
            this.service = abbreviate(service, SHORT_LIMIT);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt != null ? createdAt : Instant.now();
            return this;
        }

        public Builder action(String action) {
            this.action = abbreviate(action, SHORT_LIMIT);
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder level(AuditLevel level) {
            this.level = level != null ? level : AuditLevel.INFO;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = abbreviate(customerId, SHORT_LIMIT);
            return this;
        }

        public Builder requestPayload(String requestPayload) {
            this.requestPayload = abbreviate(requestPayload, PAYLOAD_LIMIT);
            return this;
        }

        public Builder requestAt(Instant requestAt) {
            this.requestAt = requestAt;
            return this;
        }

        public Builder responsePayload(String responsePayload) {
            this.responsePayload = abbreviate(responsePayload, PAYLOAD_LIMIT);
            return this;
        }

        public Builder responseAt(Instant responseAt) {
            this.responseAt = responseAt;
            return this;
        }

        public AuditEvent build() {
            String msgFull = message != null ? message : "";
            String req = requestPayload;
            if (req == null && msgFull.length() > MESSAGE_LIMIT) {
                req = msgFull;
            }
            String shortMessage = abbreviate(msgFull, MESSAGE_LIMIT);
            String reqPayload = req != null ? abbreviate(req, PAYLOAD_LIMIT) : null;
            String resPayload = abbreviate(responsePayload, PAYLOAD_LIMIT);
            return new AuditEvent(
                uuid,
                user,
                session,
                correlationId,
                clientIp,
                host,
                service,
                createdAt != null ? createdAt : Instant.now(),
                action,
                shortMessage,
                level != null ? level : AuditLevel.INFO,
                customerId,
                reqPayload,
                requestAt,
                resPayload,
                responseAt);
        }

        private static String abbreviate(String s, int max) {
            if (s == null) {
                return null;
            }
            return s.length() <= max ? s : s.substring(0, max);
        }
    }
}
