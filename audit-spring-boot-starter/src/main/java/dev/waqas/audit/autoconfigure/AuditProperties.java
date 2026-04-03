package dev.waqas.audit.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {

    /**
     * Master switch; when false, no audit beans are registered.
     */
    private boolean enabled = true;

    private final Rabbit rabbitmq = new Rabbit();

    private final Masking masking = new Masking();

    private final Mdc mdc = new Mdc();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Rabbit getRabbitmq() {
        return rabbitmq;
    }

    public Masking getMasking() {
        return masking;
    }

    public Mdc getMdc() {
        return mdc;
    }

    public static class Rabbit {

        /**
         * When true and RabbitMQ is available, publish audit events to the broker.
         */
        private boolean enabled = false;

        private String exchange = "app-audit-exchange";

        private String routingKey = "audit-event";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    public static class Masking {

        private List<String> patterns = new ArrayList<>();

        public List<String> getPatterns() {
            return patterns;
        }

        public void setPatterns(List<String> patterns) {
            this.patterns = patterns;
        }
    }

    public static class Mdc {

        /**
         * Register servlet filter that copies HTTP headers into MDC.
         */
        private boolean filterEnabled = true;

        private String correlationIdHeader = "X-Correlation-Id";

        private String sessionIdHeader = "X-Session-Id";

        private String userIdHeader = "X-User-Id";

        public boolean isFilterEnabled() {
            return filterEnabled;
        }

        public void setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;
        }

        public String getCorrelationIdHeader() {
            return correlationIdHeader;
        }

        public void setCorrelationIdHeader(String correlationIdHeader) {
            this.correlationIdHeader = correlationIdHeader;
        }

        public String getSessionIdHeader() {
            return sessionIdHeader;
        }

        public void setSessionIdHeader(String sessionIdHeader) {
            this.sessionIdHeader = sessionIdHeader;
        }

        public String getUserIdHeader() {
            return userIdHeader;
        }

        public void setUserIdHeader(String userIdHeader) {
            this.userIdHeader = userIdHeader;
        }
    }
}
