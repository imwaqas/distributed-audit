package dev.waqas.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used when RabbitMQ publishing is not active: writes structured JSON to the application log.
 */
public class LoggingAuditEventPublisher implements AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuditEventPublisher.class);

    private final ObjectMapper objectMapper;

    public LoggingAuditEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(AuditEvent event) {
        if (event == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            switch (event.getLevel()) {
                case ERROR -> log.error(json);
                case WARN -> log.warn(json);
                default -> log.info(json);
            }
        } catch (JsonProcessingException e) {
            log.warn("AUDIT {}", event);
        }
    }
}
