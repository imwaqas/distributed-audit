package dev.waqas.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.waqas.audit.autoconfigure.AuditProperties;

/**
 * Publishes JSON audit events to RabbitMQ when {@code app.audit.rabbitmq.enabled=true}.
 */
public class RabbitMqAuditEventPublisher implements AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqAuditEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final AuditProperties properties;

    public RabbitMqAuditEventPublisher(
        RabbitTemplate rabbitTemplate,
        ObjectMapper objectMapper,
        AuditProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void publish(AuditEvent event) {
        if (event == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                properties.getRabbitmq().getExchange(),
                properties.getRabbitmq().getRoutingKey(),
                json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit event {}", event.getUuid(), e);
        } catch (Exception e) {
            log.error("Failed to publish audit event to RabbitMQ; falling back to log line", e);
            log.warn("AUDIT_FALLBACK {}", event);
        }
    }
}
