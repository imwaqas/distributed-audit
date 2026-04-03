package dev.waqas.audit.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.audit.ingest")
public class IngestProperties {

    private String queue = "audit-events";

    private String exchange = "app-audit-exchange";

    private String routingKey = "audit-event";

    private final Forward forward = new Forward();

    private final Listener listener = new Listener();

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
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

    public Forward getForward() {
        return forward;
    }

    public Listener getListener() {
        return listener;
    }

    /**
     * Settings for {@link dev.waqas.audit.ingest.listener.ListenerConfiguration}.
     */
    public static class Listener {

        private int concurrency = 2;

        private int batchMaximumMessages = 100;

        private int batchBufferLimitMb = 1;

        private int batchTimeoutSeconds = 30;

        /**
         * Pause duration (ms) after a listener failure before restarting the container. {@code 0} restarts as soon as scheduled (local dev).
         */
        private long circuitBreakerTripDurationMs = 60_000L;

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public int getBatchMaximumMessages() {
            return batchMaximumMessages;
        }

        public void setBatchMaximumMessages(int batchMaximumMessages) {
            this.batchMaximumMessages = batchMaximumMessages;
        }

        public int getBatchBufferLimitMb() {
            return batchBufferLimitMb;
        }

        public void setBatchBufferLimitMb(int batchBufferLimitMb) {
            this.batchBufferLimitMb = batchBufferLimitMb;
        }

        public int getBatchTimeoutSeconds() {
            return batchTimeoutSeconds;
        }

        public void setBatchTimeoutSeconds(int batchTimeoutSeconds) {
            this.batchTimeoutSeconds = batchTimeoutSeconds;
        }

        public long getCircuitBreakerTripDurationMs() {
            return circuitBreakerTripDurationMs;
        }

        public void setCircuitBreakerTripDurationMs(long circuitBreakerTripDurationMs) {
            this.circuitBreakerTripDurationMs = circuitBreakerTripDurationMs;
        }
    }

    public static class Forward {

        private boolean enabled = false;

        private String url = "";

        private String apiKeyHeader = "X-Api-Key";

        private String apiKey = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
