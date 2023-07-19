//package ch.chakun;
//
//import ch.chakun.ehcache.CacheManager;
//import ch.chakun.testingflow.notification_v1.Notification;
//import net.sf.ehcache.Cache;
//import net.sf.ehcache.Element;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class OnAppStart implements ApplicationRunner {
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        System.out.println("App started");
//
//        try {
//            Cache cache = CacheManager.getCache("eventCache");
//
//            List keys = cache.getKeys();
//
//            String defaultEvent = "DEFAULT_EVENT";
//
//
//            for (Object key : keys) {
//                Element element = cache.get(key);
//                System.out.println(element);
//
//                System.out.println(element.getObjectValue());
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}
