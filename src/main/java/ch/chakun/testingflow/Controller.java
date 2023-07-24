package ch.chakun.testingflow;

import ch.chakun.ehcache.CacheManager;
import ch.chakun.testingflow.apm.TestApmEvent;
import ch.chakun.testingflow.dto.EventBusEventPublishSettings;
import ch.chakun.testingflow.dto.EventBusSubscriberSettings;
import ch.chakun.testingflow.eventbus_v2.service.EventBus;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

        System.out.println("Publishing event..: " + settings.toString());

        eventBus.publish(settings.getContextKey(), new TestApmEvent(settings.getEventKey()));

        String message = "Event published. contextKey=" + settings.getContextKey() + ", eventKey=" + settings.getEventKey();

        System.out.println(message);

        return ResponseEntity.ok(message);
    }


    ///////


    private void waitForRequestEvents(ExecutorService executor, String contextKey, CompletableFuture<List<Serializable>> request) {
        executor.submit(() -> {
            List<Serializable> receivedRequestEvents = null;
            try {
                receivedRequestEvents = request.get();
                System.out.println("RequestSubscriber Waiting done  = contextKey=" + contextKey + " received events: " + receivedRequestEvents);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void waitForPaymentEvents(ExecutorService executor, String contextKey, CompletableFuture<List<Serializable>> payment, String authorizationConfirmed) {
        executor.submit(() -> {
            try {

                List<Serializable> receivedPaymentEvents = payment.get();
                System.out.println("PaymentCallback Waiting done  = contextKey=" + contextKey + " received events: " + receivedPaymentEvents);

                System.out.println("Doing payment confirmation..");
                System.out.println("Publishing event..: " + authorizationConfirmed);

                eventBus.publish(contextKey, new TestApmEvent(authorizationConfirmed));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void waitForRegistrationEvents(ExecutorService executor, String contextKey,
                                           CompletableFuture<List<Serializable>> registration, String registrationConfirmed) {
        executor.submit(() -> {
            try {
                List<Serializable> receivedRegistrationEvents = registration.get();
                System.out.println("RegistrationCallback Waiting done  = contextKey=" + contextKey + " received events: " + receivedRegistrationEvents);


                System.out.println("Doing registration Confimration. ");
                System.out.println("Publishing event..: " + registrationConfirmed);

                eventBus.publish(contextKey, new TestApmEvent(registrationConfirmed));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @GetMapping("/test")
    public void testFlow() {

        // Subscriber1 -> Request
        // Subscriber2 -> RegistrationCallback
        // Subscriber3 -> PaymentCallback

        //  RegistrationCallback needs:  REGISTRATION_SUCCESSFUL or REGISTRATION_FAILED
        //  PaymentCallback needs: AUTHORIZATION_SUCCESSFUL AND REGISTRATION_FAILED OR REGISTRATION_CONFIRMED
        //  Request needs: AUTHORIZATION_CONFIRMED


        ExecutorService executor = Executors.newFixedThreadPool(5);

        System.out.println("Starting flow..");

        String contextKey = "PAYMENT_WITH_REGISTRATION";


        String registrationSuccessful = "REGISTRATION_SUCCESSFUL";
        String registrationFailed = "REGISTRATION_FAILED";
        String registrationConfirmed = "REGISTRATION_CONFIRMED";

        String authorizationSuccessful = "AUTHORIZATION_SUCCESSFUL";
        String authorizationConfirmed = "AUTHORIZATION_CONFIRMED";


        CompletableFuture<List<Serializable>> request = eventBus.subscribe(
                contextKey,
                events -> events.stream()
                        .map(TestApmEvent.class::cast)
                        .map(TestApmEvent::getKey)
                        .anyMatch(key -> key.equals(authorizationConfirmed))
        );
        System.out.println("RequestSubscriber Starting to wait..");

        waitForRequestEvents(executor, contextKey, request);


        CompletableFuture<List<Serializable>> registration = eventBus.subscribe(
                contextKey,
                events -> {
                    Set<String> keys = events.stream()
                            .map(TestApmEvent.class::cast)
                            .map(TestApmEvent::getKey)
                            .collect(Collectors.toSet());

                    return keys.contains(registrationSuccessful) || keys.contains(registrationFailed);
                }
        );
        System.out.println("RegistrationCallback Starting to wait..");

        waitForRegistrationEvents(executor, contextKey, registration, registrationConfirmed);

        CompletableFuture<List<Serializable>> payment = eventBus.subscribe(
                contextKey,
                events -> {
                    Set<String> keys = events.stream()
                            .map(TestApmEvent.class::cast)
                            .map(TestApmEvent::getKey)
                            .collect(Collectors.toSet());

                    return keys.contains(authorizationSuccessful) && (keys.contains(registrationFailed) || keys.contains(registrationConfirmed));
                }
        );
        System.out.println("PaymentCallback Starting to wait..");

        waitForPaymentEvents(executor, contextKey, payment, authorizationConfirmed);


        /////////////////////

        System.out.println("Starting to publish events..");

        System.out.println("Publishing event..: " + authorizationSuccessful);
        eventBus.publish(contextKey, new TestApmEvent(authorizationSuccessful));

        System.out.println("Publishing event..: " + registrationSuccessful);
        eventBus.publish(contextKey, new TestApmEvent(registrationSuccessful));

    }

}
