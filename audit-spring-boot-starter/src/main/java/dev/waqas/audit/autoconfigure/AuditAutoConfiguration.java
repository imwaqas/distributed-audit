package dev.waqas.audit.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import dev.waqas.audit.web.AuditMdcFilter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "app.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties.class)
@Import({AuditPublisherConfiguration.class, AuditRabbitExchangeConfiguration.class})
@ComponentScan(
    basePackages = "dev.waqas.audit",
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "dev\\.waqas\\.audit\\.autoconfigure\\..*"))
public class AuditAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.web.filter.OncePerRequestFilter")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class AuditServletConfiguration {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE + 20)
        @ConditionalOnProperty(prefix = "app.audit.mdc", name = "filter-enabled", havingValue = "true", matchIfMissing = true)
        AuditMdcFilter auditMdcFilter(AuditProperties properties) {
            return new AuditMdcFilter(properties);
        }
    }
}
