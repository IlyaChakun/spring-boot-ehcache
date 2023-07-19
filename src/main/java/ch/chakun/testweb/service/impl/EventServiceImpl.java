package ch.chakun.testweb.service.impl;

import ch.chakun.testweb.dto.Event;
import ch.chakun.testweb.cache.EventCache;
import ch.chakun.testweb.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventCache eventCache;

    @Override
    public void put(String contextKey, Event event) {
        System.out.println("Put Event. ContextKey = " + contextKey + ", Event = " + event);
        eventCache.put(contextKey, event);
    }

    @Override
    public Event get(String contextKey) {
        System.out.println("Get Event. ContextKey = " + contextKey + ", EventExist = " + eventCache.contains(contextKey));

        return eventCache.getOrDefault(contextKey, new Event(contextKey, "EVENT_NOT_FOUND"));
    }
}
