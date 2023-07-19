package ch.chakun.testingflow.eventbus_v2;

import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;


@ToString
public class Topic {

    private final CompletableFuture<List<Serializable>> future = new CompletableFuture<>();

    private final List<Serializable> events = new ArrayList<>();

    private final Predicate<List<Serializable>> waitingCondition;

    public Topic(Predicate<List<Serializable>> waitingCondition) {
        this.waitingCondition = waitingCondition;
    }

    public void addEvent(Serializable event) {
        events.add(event);
    }

    public boolean isWaitingConditionMet() {
        return waitingCondition.test(events);
    }

    public void complete() {
        future.complete(new ArrayList<>(events));
    }

    public boolean isCompleted() {
        return future.isDone();
    }

    public CompletableFuture<List<Serializable>> getFuture() {
        return future;
    }

}
