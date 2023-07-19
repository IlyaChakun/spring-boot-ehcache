package ch.chakun;

import ch.chakun.testingflow.apm.TestApmEvent;
import ch.chakun.testingflow.eventbus_v2.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

@SpringBootApplication(scanBasePackages = "ch.chakun")
public class Application implements CommandLineRunner {

    @Autowired
    EventBus eventBus;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static void subscriberTwo(EventBus eventBus) {
        String contextKey = "Subscriber2";
        CompletableFuture<List<Serializable>> subscriber2 = eventBus.subscribe(contextKey, events -> events.size() >= 2, 5);

        eventBus.publish(contextKey, new TestApmEvent("Event 4"));
        eventBus.publish(contextKey, new TestApmEvent("Event 5"));

        try {
            List<Serializable> events2 = subscriber2.get();
            System.out.println("Subscriber 2 received events: " + events2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static String callbackSubscriber(EventBus eventBus, ExecutorService executor) {
        String eventNotificationRegistrationAuthorized = "EVENT_NOTIFICATION_REGISTRATION_AUTHORIZED";
        String eventNotificationAuthorizationSuccessful = "EVENT_NOTIFICATION_AUTHORIZATION_SUCCESSFUL";
        String contextKey = "debit_with_registration";


        Predicate<List<Serializable>> firstSubscriberWaitingCondition = events -> {
            return events.stream()
                    .map(TestApmEvent.class::cast)
                    .filter(it -> it.getKey().equals(eventNotificationRegistrationAuthorized)
                            || it.getKey().equals(eventNotificationAuthorizationSuccessful)
                            || it.getKey().equals("IS_REGISTRATION_DONE"))
                    .distinct()
                    .count() == 3;
        };
        CompletableFuture<List<Serializable>> subscriber1 = eventBus.subscribe(contextKey, firstSubscriberWaitingCondition, 30);
        executor.submit(() -> {
            try {
                List<Serializable> events1 = subscriber1.get();

                System.out.println("Subscriber 1 = contextKey=" + contextKey + " received events: " + events1);
                System.out.println("doing payment...");

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        ///////////////////////////

        Predicate<List<Serializable>> secondSubscriberWaitingCondition = events -> {
            return events.stream()
                    .map(TestApmEvent.class::cast)
                    .filter(it -> it.getKey().equals(eventNotificationRegistrationAuthorized)
                            || it.getKey().equals(eventNotificationAuthorizationSuccessful))
                    .distinct()
                    .count() == 2;
        };

        CompletableFuture<List<Serializable>> subscriber11 = eventBus.subscribe(contextKey, secondSubscriberWaitingCondition, 30);
        executor.submit(() -> {
            try {
                doSleep(1000);

                List<Serializable> events1 = subscriber11.get();

                System.out.println("subscriber11= contextKey=" + contextKey + " received events: " + events1);
                System.out.println("doing registration...");

                eventBus.publish(contextKey, new TestApmEvent("IS_REGISTRATION_DONE"));

                System.out.println("registration done, event sent");

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return contextKey;
    }

    private static void publishEvents(EventBus eventBus, String contextKey, ExecutorService executor) {

        executor.submit(() -> {
            eventBus.publish(contextKey, new TestApmEvent("EVENT_NOTIFICATION_REGISTRATION_AUTHORIZED"));

        });

        executor.submit(() -> eventBus.publish(contextKey, new TestApmEvent("EVENT_NOTIFICATION_AUTHORIZATION_SUCCESSFUL")));

    }

    private static void doSleep(int millis) {
        try {
            Thread.sleep(millis); // Sleep for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(5);

        String contextKey = callbackSubscriber(eventBus, executor);

        publishEvents(eventBus, contextKey, executor);

        executor.shutdown();


        System.out.println("\n----------------------------------------\n");

        subscriberTwo(eventBus);
    }
}
