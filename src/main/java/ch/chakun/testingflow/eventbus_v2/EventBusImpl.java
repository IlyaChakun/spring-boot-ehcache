package ch.chakun.testingflow.eventbus_v2;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

        List<Topic> contextTopics = new ArrayList<>(eventBusCache.getOrDefault(contextKey, Collections.emptyList()));

        boolean allSubscribersCompleted = true;

        for (Topic topic : contextTopics) {

            topic.addEvent(event);

            if (!topic.isWaitingConditionMet()) {
                allSubscribersCompleted = false;
            } else {
                topic.complete();
            }

        }

        if (allSubscribersCompleted) {
            eventBusCache.remove(contextKey);
        }
    }

    @Override
    public CompletableFuture<List<Serializable>> subscribe(String contextKey,
                                                           Predicate<List<Serializable>> waitingCondition,
                                                           int timeoutSeconds) {

        Topic topic = new Topic(waitingCondition);
        eventBusCache.computeIfAbsent(contextKey, k -> new ArrayList<>()).add(topic);

        CompletableFuture<List<Serializable>> future = topic.getFuture();

        ScheduledFuture<?> timeoutTask = timeoutTask(contextKey, timeoutSeconds, future);

        cleanupTask(contextKey, future, timeoutTask);

        return future;
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
    public boolean containsSubscribers(String contextKey) {
        return eventBusCache.contains(contextKey);
    }

//    @Override
//    public void unsubscribe(String contextKey) {
//        cleanupContextSubscribers(contextKey); // todo if multiple subscribers, remove only one. how?
//    }

    private void cleanupTask(String contextKey, CompletableFuture<List<Serializable>> future, ScheduledFuture<?> timeoutTask) {
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

    private ScheduledFuture<?> timeoutTask(String contextKey, int timeoutSeconds, CompletableFuture<List<Serializable>> future) {
        // timeout task
        return scheduler.schedule(() -> {
            if (!future.isDone()) {
                System.out.println("Subscriber timed out, contextKey=" + contextKey);
                future.completeExceptionally(new TimeoutException("Subscriber timed out"));
            }
            cleanupContextSubscribers(contextKey);
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    private void cleanupContextSubscribers(String contextKey) {
        List<Topic> contextTopics = eventBusCache.get(contextKey);
        if (contextTopics != null) {
            contextTopics.removeIf(Topic::isCompleted);
            if (contextTopics.isEmpty()) {
                eventBusCache.remove(contextKey);
            }
        }
    }
}
