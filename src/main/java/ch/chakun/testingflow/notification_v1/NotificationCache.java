package ch.chakun.testingflow.notification_v1;

import ch.chakun.ehcache.CacheWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("notificationCache")
public class NotificationCache extends CacheWrapper<Long, Notification> {

    private static final String NOTIFICATION_CACHE_NAME = "notificationCache";

    public NotificationCache() {
        super(NOTIFICATION_CACHE_NAME);
    }

}