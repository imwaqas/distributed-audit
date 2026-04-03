package dev.waqas.audit.ingest;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@Configuration
public class AuditIngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditIngestApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(TaskScheduler.class)
    TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("audit-ingest-schedule-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock clock() {
        return Clock.systemUTC();
    }
}
