//package ch.chakun;
//
//import ch.chakun.testingflow.eventbus_v2.EventBusCache;
//import ch.chakun.testingflow.eventbus_v2.domain.Subscriber;
//import ch.chakun.testingflow.eventbus_v2.domain.Topic;
//import ch.chakun.testingflow.notification_v1.Notification;
//import ch.chakun.testingflow.notification_v1.NotificationCache;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class OnAppStart implements ApplicationRunner {
//
//    @Autowired
//    EventBusCache eventBusCache;
//    @Autowired
//    NotificationCache notificationCache;
//
//    private static Topic getTopic(Notification notification) {
//        Topic topic = new Topic();
//        Subscriber subscriber = new Subscriber(events -> true);
//        topic.addSubscriber(subscriber);
//        topic.addEvent(notification.getEventNotification());
//        return topic;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        System.out.println("App started");
//
//        try {
//            List<Long> oldCacheKeys = notificationCache.getKeysWithExpiryCheck();
//
//            String defaultEventPostfix = "DEFAULT_EVENT";
//
//            for (Long key : oldCacheKeys) {
//                Notification notification = notificationCache.get(key);
//
//                String contextKey = key + "_" + defaultEventPostfix;
//                Topic topic = getTopic(notification);
//
//                eventBusCache.put(contextKey, topic);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}