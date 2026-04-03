package dev.waqas.audit.autoconfigure;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures the audit exchange exists before publishers send (RabbitMQ will not auto-create a matching direct exchange).
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@ConditionalOnProperty(prefix = "app.audit.rabbitmq", name = "enabled", havingValue = "true")
@ConditionalOnBean(ConnectionFactory.class)
public class AuditRabbitExchangeConfiguration {

    @Bean
    DirectExchange auditEventsExchange(AuditProperties properties) {
        return new DirectExchange(properties.getRabbitmq().getExchange(), true, false);
    }
}
