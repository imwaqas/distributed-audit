package dev.waqas.audit.ingest.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IngestProperties.class)
public class RabbitTopologyConfiguration {

    @Bean
    Queue auditIngestQueue(IngestProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    DirectExchange auditExchange(IngestProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    Binding auditBinding(Queue auditIngestQueue, DirectExchange auditExchange, IngestProperties properties) {
        return BindingBuilder.bind(auditIngestQueue)
            .to(auditExchange)
            .with(properties.getRoutingKey());
    }
}
