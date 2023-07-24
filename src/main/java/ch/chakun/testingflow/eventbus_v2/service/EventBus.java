package ch.chakun.testingflow.eventbus_v2.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface EventBus<T> {

    void publishEvent(String contextKey, T event);

    CompletableFuture<List<T>> subscribe(String contextKey,
                                         String subscriberKey,
                                         Predicate<List<T>> waitingCondition,
                                         int timeoutSeconds);

    CompletableFuture<List<T>> subscribe(String contextKey,
                                         String subscriberKey,
                                         Predicate<List<T>> waitingCondition);

    CompletableFuture<List<T>> subscribe(String contextKey,
                                         Predicate<List<T>> waitingCondition);

    CompletableFuture<List<T>> subscribe(String contextKey, int timeoutSeconds);

    CompletableFuture<List<T>> subscribe(String contextKey);

    void unsubscribe(String contextKey, String subscriberKey);


    boolean containSubscribers(String contextKey);

}
