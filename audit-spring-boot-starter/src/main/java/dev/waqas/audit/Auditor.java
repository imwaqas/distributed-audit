package dev.waqas.audit;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Enriches from {@link AuditContextProvider}, masks sensitive fields, then publishes.
 */
@Component
public class Auditor implements Consumer<AuditEvent> {

    private static final Logger log = LoggerFactory.getLogger(Auditor.class);

    private final AuditEventPublisher publisher;
    private final PatternBasedMasker masker;
    private final AuditContextProvider contextProvider;
    private final String applicationName;

    public Auditor(
        AuditEventPublisher publisher,
        PatternBasedMasker masker,
        AuditContextProvider contextProvider,
        @Value("${spring.application.name:unknown}") String applicationName) {
        this.publisher = publisher;
        this.masker = masker;
        this.contextProvider = contextProvider;
        this.applicationName = applicationName;
    }

    @Override
    public void accept(AuditEvent event) {
        try {
            publisher.publish(enrichAndMask(event));
        } catch (Exception e) {
            log.error("Audit pipeline error for event {}", event != null ? event.getUuid() : "null", e);
        }
    }

    public void audit(AuditEvent event) {
        accept(event);
    }

    private AuditEvent enrichAndMask(AuditEvent event) {
        AuditEvent.Builder b = event.toBuilder();
        b.message(masker.mask(event.getMessage()));
        b.requestPayload(masker.mask(event.getRequestPayload()));
        b.responsePayload(masker.mask(event.getResponsePayload()));
        if (StringUtils.isBlank(event.getUser())) {
            contextProvider.userId().ifPresent(b::user);
        }
        if (StringUtils.isBlank(event.getSession())) {
            contextProvider.sessionId().ifPresent(b::session);
        }
        if (StringUtils.isBlank(event.getCorrelationId())) {
            contextProvider.correlationId().ifPresent(b::correlationId);
        }
        if (StringUtils.isBlank(event.getClientIp())) {
            contextProvider.clientIp().ifPresent(b::clientIp);
        }
        if (StringUtils.isBlank(event.getService())) {
            b.service(applicationName);
        }
        return b.build();
    }
}
