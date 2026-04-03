package dev.waqas.audit.ingest.listener;

import java.time.Clock;
import java.time.Duration;

import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import dev.waqas.audit.ingest.config.IngestProperties;

/**
 * Configures a dedicated {@link SimpleMessageListenerContainer} with <strong>consumer-side batching</strong>
 * RabbitMQ delivers debatched messages; the container accumulates
 * up to {@link IngestProperties.Listener#getBatchMaximumMessages()} (or buffer/timeout) then invokes
 * {@link AuditIngestBatchListener#onMessageBatch} once per batch for efficient ingestor I/O.
 */
@Configuration
public class ListenerConfiguration {

    @Bean
    SimpleMessageListenerContainer auditIngestContainer(
        ConnectionFactory connectionFactory,
        IngestProperties properties,
        TaskScheduler taskScheduler,
        Clock clock,
        AuditIngestBatchListener auditIngestBatchListener) {
        IngestProperties.Listener listener = properties.getListener();
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(properties.getQueue());
        container.setAdviceChain(
            new ListenerBackoffInterceptor(
                container,
                listener.getCircuitBreakerTripDurationMs(),
                taskScheduler,
                clock));
        container.setForceStop(true);
        container.setConsumerBatchEnabled(true);
        container.setBatchSize(listener.getBatchMaximumMessages());
        container.setBatchingStrategy(
            new SimpleBatchingStrategy(
                listener.getBatchMaximumMessages(),
                listener.getBatchBufferLimitMb() * 1024 * 1024 * 8,
                Duration.ofSeconds(listener.getBatchTimeoutSeconds()).toMillis()));
        container.setMessageListener(auditIngestBatchListener);
        container.setShutdownTimeout(10_000);
        container.setConcurrentConsumers(listener.getConcurrency());
        container.setDeBatchingEnabled(true);
        return container;
    }
}
