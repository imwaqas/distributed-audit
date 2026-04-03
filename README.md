# distributed-audit

Personal, vendor-neutral audit pipeline for Spring Boot: **`@Auditable` methods** publish structured **`AuditEvent`** records to **logs** and/or **RabbitMQ**; **`audit-ingest-service`** consumes the queue and hands batches to **`AuditLogIngestor`** implementations (structured logging, optional HTTP forward, or your own bean).

## Modules

| Module | Role |
|--------|------|
| **audit-spring-boot-starter** | Library: `@Auditable`, AOP aspect, masking, MDC context, Rabbit or logging publisher, auto-configuration |
| **audit-ingest-service** | Deployable consumer: declares queue/binding, **batched** `SimpleMessageListenerContainer` + **circuit breaker**, pluggable **`AuditLogIngestor`** beans |

**Coordinates (local):** `group` `dev.waqas.audit`, `version` `0.1.0-SNAPSHOT` — publish to Maven Local with `./gradlew publishToMavenLocal` if you add the `maven-publish` plugin, or depend as a composite Gradle project.

## How it works

1. A business service adds **`implementation project(':audit-spring-boot-starter')`** (or the published JAR).
2. Spring Boot loads **`AuditAutoConfiguration`** (via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`).
3. Methods annotated with **`@Auditable`** trigger an aspect after success or after an exception. SpEL builds the message from **`#args`**, **`#methodName`**, and **`#result`** (success only).
4. **`Auditor`** applies regex **masking**, fills missing **user / session / correlation / client IP** from **SLF4J MDC** (optional servlet filter maps HTTP headers into MDC), sets **service** from **`spring.application.name`**, then **publishes**.
5. If **`app.audit.rabbitmq.enabled=true`** and **`RabbitTemplate`** is available, events are JSON-serialized to **`app.audit.rabbitmq.exchange`** with **`app.audit.rabbitmq.routing-key`**. The starter also declares a matching **direct exchange** so producers can start before the ingest service.
6. Otherwise **`LoggingAuditEventPublisher`** writes one JSON line per event at the matching log level.
7. **Ingest** uses a **`SimpleMessageListenerContainer`** (not `@RabbitListener`) with **consumer batching**: Rabbit delivers messages; the container accumulates up to **`app.audit.ingest.listener.batch-maximum-messages`** (or buffer size / timeout), then calls **`AuditIngestBatchListener.onMessageBatch`** once. Each AMQP body is one **`AuditEvent`** JSON object; **`AuditLogIngestor`** implementations receive **`List<AuditEvent>`** per batch via **`ingest(...)`**. A **`ListenerBackoffInterceptor`** on the container’s advice chain stops consumption if the listener throws (bad payload, downstream outage); after **`circuit-breaker-trip-duration-ms`** the container **restarts**, avoiding a tight failure loop.

## Consumer microservice setup

**`build.gradle`**

```gradle
dependencies {
    implementation 'dev.waqas.audit:audit-spring-boot-starter:0.1.0-SNAPSHOT'
    // If you use RabbitMQ publishing:
    // (transitive from starter: spring-boot-starter-amqp)
}
```

**`application.yml`**

```yaml
spring:
  application:
    name: orders-service
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

app:
  audit:
    rabbitmq:
      enabled: true
      exchange: app-audit-exchange
      routing-key: audit-event
    masking:
      patterns:
        - "\\b\\d{16}\\b"   # example: mask 16-digit runs
```

**Example**

```java
@Service
public class OrderService {

    @Auditable(
        successMessage = "'Order placed: '.concat(#args[0].id())",
        failureMessage = "'Order failed: '.concat(#args[0].id())",
        action = "placeOrder"
    )
    public void placeOrder(OrderId id) {
        // ...
    }
}
```

Disable all audit beans: `app.audit.enabled=false`.

## MDC keys (optional filter)

When **`spring-web`** is on the classpath, a filter (enabled by default) copies headers into MDC:

| MDC key | Default header |
|---------|----------------|
| `audit.userId` | `X-User-Id` |
| `audit.sessionId` | `X-Session-Id` |
| `audit.correlationId` | `X-Correlation-Id` |
| `audit.clientIp` | from `request.getRemoteAddr()` if absent |

Configure under **`app.audit.mdc.*`** (`filter-enabled`, `user-id-header`, etc.).

## RabbitMQ topology

Defaults align across producer and ingest:

- **Exchange:** `app-audit-exchange` (**direct**, durable)
- **Routing key:** `audit-event`
- **Queue:** `audit-events` (created by ingest, bound to the exchange)

Start **ingest** (or declare the queue/binding) before relying on delivery; unroutable messages may be dropped depending on broker settings.

## Ingest service

**Run locally**

```bash
./gradlew :audit-ingest-service:bootRun
```

Requires RabbitMQ and excludes **`AuditAutoConfiguration`** so the ingest app does not register the auditing aspect.

**Listener (batch + circuit breaker)**

| Property | Default | Purpose |
|----------|---------|---------|
| `app.audit.ingest.listener.concurrency` | `2` | `SimpleMessageListenerContainer` concurrent consumers |
| `app.audit.ingest.listener.batch-maximum-messages` | `100` | Max messages per batch before invoking the listener |
| `app.audit.ingest.listener.batch-buffer-limit-mb` | `1` | Input to buffer limit passed to `SimpleBatchingStrategy` (same scaling as reference audit-service) |
| `app.audit.ingest.listener.batch-timeout-seconds` | `30` | Max wait to fill a partial batch |
| `app.audit.ingest.listener.circuit-breaker-trip-duration-ms` | `0` (default yaml); `60000` on `docker` profile | Pause after listener failure before container restart |

**HTTP forward**

```yaml
app:
  audit:
    ingest:
      forward:
        enabled: true
        url: https://your-log-endpoint.example/logs
        api-key-header: X-Api-Key
        api-key: ${INGEST_FORWARD_API_KEY:}
```

## Docker smoke test

```bash
./gradlew :audit-ingest-service:bootJar
docker compose up --build
```

- RabbitMQ management UI: http://localhost:15672 (guest/guest)  
- Ingest HTTP port: **18083**

## Build requirements

- **JDK 17+** (toolchain in root `build.gradle`)
- Network for **Maven Central** on first build

## License

Personal project — use and license as you prefer.
