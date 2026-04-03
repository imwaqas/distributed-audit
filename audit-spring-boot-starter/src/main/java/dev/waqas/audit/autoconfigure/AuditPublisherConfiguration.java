package dev.waqas.audit.autoconfigure;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.waqas.audit.AuditEventPublisher;
import dev.waqas.audit.LoggingAuditEventPublisher;
import dev.waqas.audit.RabbitMqAuditEventPublisher;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
public class AuditPublisherConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.audit.rabbitmq", name = "enabled", havingValue = "true")
    @ConditionalOnBean(RabbitTemplate.class)
    AuditEventPublisher rabbitMqAuditEventPublisher(
        RabbitTemplate rabbitTemplate,
        ObjectMapper objectMapper,
        AuditProperties properties) {
        return new RabbitMqAuditEventPublisher(rabbitTemplate, objectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventPublisher.class)
    AuditEventPublisher loggingAuditEventPublisher(ObjectMapper objectMapper) {
        return new LoggingAuditEventPublisher(objectMapper);
    }
}
