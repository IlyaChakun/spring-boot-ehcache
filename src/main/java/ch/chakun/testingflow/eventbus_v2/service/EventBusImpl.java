package ch.chakun.testingflow.eventbus_v2.service;


import ch.chakun.testingflow.eventbus_v2.EventBusCache;
import ch.chakun.testingflow.eventbus_v2.domain.Subscriber;
import ch.chakun.testingflow.eventbus_v2.domain.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

@Slf4j
@Component
public class EventBusImpl implements EventBus {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Qualifier("eventBusCache")
    @Autowired
    private EventBusCache eventBusCache;


    @Override
    public void publish(String contextKey, Serializable event) {

        // todo Does it make sense to store published events which does not find eny subscribers?

        Topic topic = eventBusCache.getOrDefault(contextKey, null);

        if (topic != null) {
            topic.addEvent(event);
            topic.notifySubscribers();
        }
    }

    @Override
    public CompletableFuture<List<Serializable>> subscribe(String contextKey,
                                                           Predicate<List<Serializable>> waitingCondition,
                                                           int timeoutSeconds) {

        Subscriber subscriber = new Subscriber(waitingCondition);

        Topic topic = eventBusCache.computeIfAbsent(contextKey, k -> new Topic());
        topic.addSubscriber(subscriber);

        CompletableFuture<List<Serializable>> subscriberFuture = subscriber.getFuture();

        ScheduledFuture<?> timeoutTask = subscriberTimeoutTask(contextKey, timeoutSeconds, subscriberFuture);

        subscriberCleanupTask(contextKey, subscriberFuture, timeoutTask);

        return subscriberFuture;
    }

    @Override
    public CompletableFuture<List<Serializable>> subscribe(String contextKey, Predicate<List<Serializable>> waitingCondition) {
        return this.subscribe(contextKey, waitingCondition, DEFAULT_TIMEOUT_SECONDS);
    }

    @Override
    public CompletableFuture<List<Serializable>> subscribe(String contextKey) {
        return this.subscribe(contextKey, events -> true, DEFAULT_TIMEOUT_SECONDS);
    }

    @Override
    public CompletableFuture<List<Serializable>> subscribe(String contextKey, int timeoutSeconds) {
        return this.subscribe(contextKey, events -> true, timeoutSeconds);
    }

    @Override
    public boolean containSubscribers(String contextKey) {
        return eventBusCache.contains(contextKey);
    }

//    @Override
//    public void unsubscribe(String contextKey) {
//        cleanupContextSubscribers(contextKey); // todo if multiple subscribers, remove only one. how?
//    }

    private void subscriberCleanupTask(String contextKey, CompletableFuture<List<Serializable>> future, ScheduledFuture<?> timeoutTask) {
        // cleanup task
        future.whenComplete((result, exception) -> {
            if (!timeoutTask.isDone()) {
                timeoutTask.cancel(false);

                //scheduler.shutdown();//todo how to do cleanup?
            }
            System.out.println("Subscriber task done, doing cleanup, contextKey=" + contextKey);
            cleanupContextSubscribers(contextKey);
        });
    }

    private ScheduledFuture<?> subscriberTimeoutTask(String contextKey, int timeoutSeconds, CompletableFuture<List<Serializable>> future) {
        // timeout task
        return scheduler.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Subscriber timed out"));
            }
            cleanupContextSubscribers(contextKey);
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    private void cleanupContextSubscribers(String contextKey) {
        Topic contextTopic = eventBusCache.get(contextKey);
        if (contextTopic != null && contextTopic.getSubscribers() != null) {

            contextTopic.removeCompletedSubscribers();

            if (contextTopic.getSubscribers().isEmpty()) {
                eventBusCache.remove(contextKey);
            }
        }
    }
}
