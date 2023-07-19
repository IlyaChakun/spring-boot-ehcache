package ch.chakun.testweb.service;

import ch.chakun.testweb.dto.Event;

public interface EventService {

    void put(String contextKey, Event event);

    Event get(String contextKey);
}
