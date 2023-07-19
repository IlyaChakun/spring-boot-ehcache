package ch.chakun.testingflow.eventbus_v2;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface EventBus {

    void publish(String contextKey, Serializable event);

    CompletableFuture<List<Serializable>> subscribe(String contextKey,
                                                    Predicate<List<Serializable>> waitingCondition, int timeoutSeconds);

    CompletableFuture<List<Serializable>> subscribe(String contextKey,
                                                    Predicate<List<Serializable>> waitingCondition);

    CompletableFuture<List<Serializable>> subscribe(String contextKey);

    CompletableFuture<List<Serializable>> subscribe(String contextKey, int timeoutSeconds);

    //void unsubscribe(String contextKey);


    boolean containsSubscribers(String contextKey);

}
