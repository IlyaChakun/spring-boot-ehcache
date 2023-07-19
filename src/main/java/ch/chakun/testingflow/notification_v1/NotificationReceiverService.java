package ch.chakun.testingflow.notification_v1;

import ch.chakun.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NotificationReceiverService {

    private final long pollIntervalMillis = 100;
    private final long expireAfterMillis = TimeUnit.SECONDS.toMillis(60); //30

    @Autowired
    private NotificationCache notificationCache;

    public void register(long transactionId) {
        Notification callbackNotification = new Notification();
        callbackNotification.setDateCreated(System.currentTimeMillis());
        notificationCache.put(transactionId, callbackNotification);
    }

    public void deregister(long transactionId) {
        notificationCache.remove(transactionId);
    }

    public boolean isWaitingFor(long transactionId) {
        return notificationCache.contains(transactionId);
    }

    public void notify(long transactionId, Serializable eventNotification) {
        final Notification callbackNotification = notificationCache.get(transactionId);
        if (callbackNotification == null) {
            log.warn("No context found for transactionId={}", transactionId);
            return;
        }
        callbackNotification.setDateReceived(System.currentTimeMillis());
        callbackNotification.setEventNotification(eventNotification);
        notificationCache.put(transactionId, callbackNotification);
    }

    public long getExpireAfterMillis() {
        return expireAfterMillis;
    }

    public Notification await(long transactionId) {
        return await(transactionId, expireAfterMillis);
    }

    public Notification await(long transactionId, long expireAfterMillis) {

        long startTime = System.currentTimeMillis();

        while (notificationCache.contains(transactionId)) {

            final Notification callbackNotification = notificationCache.get(transactionId);

            if (callbackNotification != null && callbackNotification.getEventNotification() == null) {

                if (System.currentTimeMillis() - startTime > expireAfterMillis) {
                    break;
                } else {
                    ThreadUtils.sleep(pollIntervalMillis);
                }

            } else {

                log.info("Callback received, transactionId={}, {}",
                        transactionId,
                        callbackNotification);

                return callbackNotification;
            }
        }

        return null;
    }

    public Notification getNotificationByTransactionId(long transactionId) {
        final Notification callbackNotification = notificationCache.get(transactionId);
        if (callbackNotification != null && callbackNotification.getEventNotification() != null) {
            return callbackNotification;
        } else {
            return null;
        }
    }
}
