package ch.chakun.testingflow.eventbus_v2.domain;

import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@ToString

public class Topic {

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    private final List<Serializable> topicEvents = new ArrayList<>();

    public Topic() {
    }


    public List<Subscriber> getSubscribers() {
        return subscribers;
    }


    public void addEvent(Serializable event) {
        topicEvents.add(event);
    }

    public void notifySubscribers() {
        for (Subscriber subscriber : subscribers) {
            if (subscriber.isWaitingConditionMet(topicEvents)) {
                subscriber.complete();
            }
        }
    }

    public void removeCompletedSubscribers() {
        subscribers.removeIf(Subscriber::isCompleted);
    }

}