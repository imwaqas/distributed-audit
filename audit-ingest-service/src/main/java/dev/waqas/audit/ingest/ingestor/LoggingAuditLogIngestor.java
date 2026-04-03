package dev.waqas.audit.ingest.ingestor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.waqas.audit.AuditEvent;

@Component
@Order(100)
public class LoggingAuditLogIngestor implements AuditLogIngestor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuditLogIngestor.class);

    private final ObjectMapper objectMapper;

    public LoggingAuditLogIngestor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void ingest(List<AuditEvent> events) {
        for (AuditEvent event : events) {
            try {
                log.info("AUDIT_INGEST {}", objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                log.warn("AUDIT_INGEST {}", event);
            }
        }
    }
}
