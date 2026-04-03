package dev.waqas.audit.ingest.listener;

import java.time.Clock;
import java.time.Instant;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.scheduling.TaskScheduler;

/**
 * Wraps the listener invocation: on any {@link Exception} from the delegate (e.g. HTTP forward or
 * parse failure), stops the {@link MessageListenerContainer} so no new messages are consumed, then
 * schedules a restart after {@code circuitTripDuration} milliseconds.
 * <p>
 * This acts as a simple <strong>circuit breaker</strong> for the consumer: repeated failures against
 * a downstream (or poison messages) do not spin in a tight loop; the container pauses, giving the
 * broker and downstream time to recover. Use {@code circuit-breaker-trip-duration-ms: 0} for an
 * immediate restart attempt (still stops first, useful for local development).
 */
public class ListenerBackoffInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ListenerBackoffInterceptor.class);

    private final MessageListenerContainer messageListenerContainer;
    private final long circuitTripDuration;
    private final TaskScheduler taskScheduler;
    private final Clock clock;

    public ListenerBackoffInterceptor(
        MessageListenerContainer messageListenerContainer,
        long circuitTripDuration,
        TaskScheduler taskScheduler,
        Clock clock) {
        this.messageListenerContainer = messageListenerContainer;
        this.circuitTripDuration = circuitTripDuration;
        this.taskScheduler = taskScheduler;
        this.clock = clock;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            stopAndScheduleRestart(e);
            throw e;
        }
    }

    private void stopAndScheduleRestart(Exception e) {
        synchronized (this) {
            if (!messageListenerContainer.isRunning()) {
                return;
            }
            log.warn(
                "Stopping {}. Messages will not be read for {} ms due to error from delegate listener",
                messageListenerContainer.getClass().getSimpleName(),
                circuitTripDuration,
                e);
            messageListenerContainer.stop(this::scheduleMessageListenerContainerRestart);
        }
    }

    private void scheduleMessageListenerContainerRestart() {
        log.info(
            "Scheduling {} to restart in {} ms.",
            messageListenerContainer.getClass().getSimpleName(),
            circuitTripDuration);
        taskScheduler.schedule(messageListenerContainer::start, Instant.now(clock).plusMillis(circuitTripDuration));
    }
}
