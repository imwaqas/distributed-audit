package dev.waqas.audit.ingest.ingestor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.waqas.audit.AuditEvent;
import dev.waqas.audit.ingest.config.IngestProperties;

@Component
@Order(50)
@ConditionalOnProperty(prefix = "app.audit.ingest.forward", name = "enabled", havingValue = "true")
public class HttpWebhookAuditLogIngestor implements AuditLogIngestor {

    private static final Logger log = LoggerFactory.getLogger(HttpWebhookAuditLogIngestor.class);

    private final IngestProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public HttpWebhookAuditLogIngestor(IngestProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Override
    public void ingest(List<AuditEvent> events) {
        String url = properties.getForward().getUrl();
        if (!StringUtils.hasText(url)) {
            log.warn("HTTP forward enabled but app.audit.ingest.forward.url is empty; skipping");
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(events);
            RestClient.RequestBodySpec request = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(properties.getForward().getApiKey())) {
                request = request.header(
                    properties.getForward().getApiKeyHeader(),
                    properties.getForward().getApiKey());
            }
            request.body(json).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to forward {} audit event(s) via HTTP ingestor", events.size(), e);
            throw new IllegalStateException("HTTP audit forward failed", e);
        }
    }
}
