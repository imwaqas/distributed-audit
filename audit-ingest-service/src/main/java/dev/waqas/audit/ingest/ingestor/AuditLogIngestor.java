package dev.waqas.audit.ingest.ingestor;

import java.util.List;

import dev.waqas.audit.AuditEvent;

/**
 * Takes a batch of audit events from the queue listener and writes them to a destination
 * (application logs, HTTP log pipeline, etc.).
 */
@FunctionalInterface
public interface AuditLogIngestor {

    void ingest(List<AuditEvent> events);
}
