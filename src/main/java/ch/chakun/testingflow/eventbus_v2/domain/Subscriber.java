package ch.chakun.testingflow.eventbus_v2.domain;

import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@ToString
public class Subscriber {

    private final Predicate<List<Serializable>> waitingCondition;

    private final CompletableFuture<List<Serializable>> future = new CompletableFuture<>();

    private List<Serializable> events = new ArrayList<>();

    public Subscriber(Predicate<List<Serializable>> waitingCondition) {
        this.waitingCondition = waitingCondition;
    }

    public boolean isWaitingConditionMet(List<Serializable> events) {
        this.events = new ArrayList<>(events);
        return waitingCondition.test(this.events);
    }

    public boolean isCompleted() {
        return future.isDone();
    }

    public CompletableFuture<List<Serializable>> getFuture() {
        return future;
    }
    

    public void complete() {
        future.complete(events);
    }

}
