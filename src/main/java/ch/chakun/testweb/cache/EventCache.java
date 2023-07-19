package ch.chakun.testweb.cache;

import ch.chakun.testweb.dto.Event;
import ch.chakun.ehcache.CacheWrapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("eventCache")
@Getter
public class EventCache extends CacheWrapper<String, Event> {

    private static final String NOTIFICATION_CACHE_NAME = "eventCache";

    public EventCache() {
        super(NOTIFICATION_CACHE_NAME);
    }

}
