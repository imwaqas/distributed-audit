package dev.waqas.audit.ingest.listener;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.waqas.audit.AuditEvent;
import dev.waqas.audit.ingest.ingestor.AuditLogIngestor;

/**
 * Batch consumer: maps each AMQP {@link Message} body to {@link AuditEvent}, then passes the full
 * batch to every {@link AuditLogIngestor} (logging and optional HTTP) in one call per ingestor.
 */
@Component
public class AuditIngestBatchListener implements BatchMessageListener {

    private static final Logger log = LoggerFactory.getLogger(AuditIngestBatchListener.class);

    private final ObjectMapper objectMapper;
    private final List<AuditLogIngestor> ingestors;

    public AuditIngestBatchListener(ObjectMapper objectMapper, List<AuditLogIngestor> ingestors) {
        this.objectMapper = objectMapper;
        this.ingestors = ingestors;
    }

    @Override
    public void onMessageBatch(List<Message> messages) {
        List<AuditEvent> records = messages.stream().map(this::toAuditEvent).toList();
        for (AuditLogIngestor ingestor : ingestors) {
            ingestor.ingest(records);
        }
        log.debug("Processed batch of {} audit event(s)", records.size());
    }

    private AuditEvent toAuditEvent(Message message) {
        String json = new String(message.getBody(), UTF_8);
        try {
            return objectMapper.readValue(json, AuditEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not process audit message: " + json, e);
        }
    }
}
