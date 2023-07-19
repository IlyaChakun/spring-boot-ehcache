package ch.chakun.testingflow.eventbus_v2;

import ch.chakun.ehcache.CacheWrapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("eventBusCache")
@Getter
public class EventBusCache extends CacheWrapper<String, List<Topic>> {

    private static final String NOTIFICATION_CACHE_NAME = "eventBusCache";

    public EventBusCache() {
        super(NOTIFICATION_CACHE_NAME);
    }

}
