package ch.chakun.testingflow.eventbus_v2.domain;

import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@ToString

public class Topic<T> implements Serializable {

    private final List<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();

    private final List<T> topicEvents = new ArrayList<>();

    public Topic() {
    }


    public List<Subscriber<T>> getSubscribers() {
        return subscribers;
    }

    public void addEvent(T event) {
        topicEvents.add(event);
    }

    public void addSubscriber(Subscriber<T> subscriber) {
        subscribers.add(subscriber);
    }

    public void notifySubscribers() {
        for (Subscriber<T> subscriber : subscribers) {
            if (subscriber.isWaitingConditionMet(topicEvents)) {
                subscriber.complete();
            }
        }
    }

    public void removeCompletedSubscribers() {
        subscribers.removeIf(Subscriber::isCompleted);
    }

}