package ch.chakun.testingflow;

import ch.chakun.ehcache.CacheManager;
import ch.chakun.testingflow.apm.TestApmEvent;
import ch.chakun.testingflow.dto.EventBusEventPublishSettings;
import ch.chakun.testingflow.dto.EventBusSubscriberSettings;
import ch.chakun.testingflow.eventbus_v2.EventBus;
import ch.chakun.testingflow.notification_v1.Notification;
import ch.chakun.testingflow.notification_v1.NotificationReceiverService;
import ch.chakun.testingflow.notification_v1.NotificationTestEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/flows")
public class Controller {

    long transactionId = 22222222;
    String expectedEvent = "DEFAULT_EVENT";


    @Autowired
    private NotificationReceiverService notificationReceiverService;
    @Autowired
    private EventBus eventBus;

    @GetMapping("/cache/{cacheName}")
    public ResponseEntity<Object> getCache(@PathVariable String cacheName) {

        Cache cache = CacheManager.getCache(cacheName);

        List keys = cache.getKeys();

        StringBuilder stringBuilder = new StringBuilder();

        for (Object key : keys) {
            Element element = cache.get(key);

            stringBuilder
                    .append("       ")
                    .append(element.getObjectKey())
                    .append(",")
                    .append(element.getObjectValue());

        }

        return ResponseEntity.ok(stringBuilder.toString());
    }

    @PostMapping("/notification-service")
    public ResponseEntity<String> registerEvent() {

        notificationReceiverService.register(transactionId);

        String message = "Transaction registered. transactionId=" + transactionId;

        System.out.println(message);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Notification> waitForEvent() {
        Notification notification = notificationReceiverService.await(transactionId);

        String message = "Notification Received. transactionId=" + transactionId + ", Notification= " + notification;
        System.out.println(message);

        return ResponseEntity.ok(notification);
    }

    @PutMapping("/notification-service")
    public ResponseEntity<String> putEvent() {
        notificationReceiverService.notify(transactionId, new NotificationTestEvent(expectedEvent));

        String message = "Notification event published. transactionId=" + transactionId;
        System.out.println(message);

        return ResponseEntity.ok(message);
    }

    /////////////////


    @PostMapping("/event-bus")
    public ResponseEntity<String> subscribe(@RequestBody EventBusSubscriberSettings settings) throws ExecutionException, InterruptedException {

        System.out.println("Waiting condition..: " + settings.toString());

        CompletableFuture<List<Serializable>> subscriber = eventBus.subscribe(
                settings.getContextKey(),
                events -> {
                    Set<String> eventSet = events.stream()
                            .map(TestApmEvent.class::cast)
                            .map(TestApmEvent::getKey)
                            .collect(Collectors.toSet());
                    return eventSet.containsAll(settings.getExpectedEventKeys());
                }
        );

        System.out.println("Starting to wait..");
        List<Serializable> events = subscriber.get();

        String message = "Waiting done Subscriber  = contextKey=" + settings.getContextKey() + " received events: " + events;

        System.out.println(message);

        return ResponseEntity.ok(message);
    }

    @PutMapping("/event-bus")
    public ResponseEntity<String> publishEvent(@RequestBody EventBusEventPublishSettings settings) {

        eventBus.publish(settings.getContextKey(), new TestApmEvent(settings.getEventKey()));

        String message = "Event published. contextKey=" + settings.getContextKey() + ", eventKey=" + settings.getEventKey();

        System.out.println(message);

        return ResponseEntity.ok(message);
    }


}
