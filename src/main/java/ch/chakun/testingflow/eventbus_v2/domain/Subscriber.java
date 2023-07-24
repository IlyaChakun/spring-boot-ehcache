package ch.chakun.testingflow.eventbus_v2.domain;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@ToString
public class Subscriber<T> {

    private final Predicate<List<T>> waitingCondition;

    private final CompletableFuture<List<T>> future = new CompletableFuture<>();

    private List<T> subscriberPersonalEvents = new ArrayList<>();

    public Subscriber(Predicate<List<T>> waitingCondition) {
        this.waitingCondition = waitingCondition;
    }

    public boolean isWaitingConditionMet(List<T> events) {
        this.subscriberPersonalEvents = new ArrayList<>(events);
        return waitingCondition.test(this.subscriberPersonalEvents);
    }

    public boolean isCompleted() {
        return future.isDone();
    }

    public CompletableFuture<List<T>> getFuture() {
        return future;
    }


    public void complete() {
        future.complete(subscriberPersonalEvents);
    }

}
