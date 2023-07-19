package ch.chakun.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
    public static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    /**
     * Will catch the InterruptedException that can be thrown by Thread.sleep(). <br/>
     * <p>
     * This is an workaround, see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}