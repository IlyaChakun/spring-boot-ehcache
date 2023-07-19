package ch.chakun.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CacheManager {
    final static Logger logger = LoggerFactory.getLogger(CacheManager.class);
    public static int DAY = 86400;
    public static int HOUR = 3600;
    public static int MINUTE = 60;
    static net.sf.ehcache.CacheManager cacheManager;
    static Cache localCache;

    public static synchronized void init() throws CacheException {
        if (cacheManager == null) {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ehcache.xml")) {
                if (is == null) {
                    throw new CacheException("Could not find configuration file ehcache.xml");
                }

                Configuration conf = ConfigurationFactory.parseConfiguration(is);

                conf = updateConfiguration(conf);

                cacheManager = new net.sf.ehcache.CacheManager(conf);

                localCache = cacheManager.getCache("localCache");

                if (localCache == null) {
                    throw new CacheException("Could not find configuration for cache 'localCache'");
                }

                logger.info("initialized {}", cacheManager.getName());
            } catch (IOException e) {
                throw new CacheException("could not initialize", e);
            }
        }
    }

    public static void shutdown() {
        if (cacheManager != null) {
            cacheManager.shutdown();
            cacheManager = null;
            localCache = null;
        }

        logger.info("shutdown");
    }

    public static Cache getCache() {
        if (localCache == null) {
            init();
        }

        return localCache;
    }

    public static Cache getCache(String cacheName) {
        if (localCache == null) {
            init();
        }

        return cacheManager.getCache(cacheName);
    }

    public static net.sf.ehcache.CacheManager get() {
        return cacheManager;
    }

    private static Configuration updateConfiguration(Configuration config) {

        String rmiHostName = System.getProperty("ehcache.rmireplication.hostname");

        if (config != null && StringUtils.isNotBlank(rmiHostName)) {
            List<FactoryConfiguration> peerProvFactories = config.getCacheManagerPeerProviderFactoryConfiguration();
            if (peerProvFactories != null) {
                FactoryConfiguration rmiPeerProvFact = peerProvFactories.stream()
                        .filter(fc -> "net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory".equals(fc.getFullyQualifiedClassPath()))
                        .findFirst()
                        .orElse(null);
                if (rmiPeerProvFact != null) {
                    String props = rmiPeerProvFact.getProperties();
                    if (!StringUtils.containsIgnoreCase(props, "hostName")) {
                        props = "hostName=" + rmiHostName + ((props != null) ? "," + props : "");
                        rmiPeerProvFact.setProperties(props);
                    } else {
                        logger.warn("Cannot configure RMICacheManagerPeerProviderFactory hostName! It's already configured. Current value=" + props);
                    }
                }
            }

            List<FactoryConfiguration> peerListFactories = config.getCacheManagerPeerListenerFactoryConfigurations();
            if (peerListFactories != null) {
                FactoryConfiguration rmiPeerListFact = peerListFactories.stream()
                        .filter(fc -> "net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory".equals(fc.getFullyQualifiedClassPath()))
                        .findFirst()
                        .orElse(null);
                if (rmiPeerListFact != null) {
                    String props = rmiPeerListFact.getProperties();
                    if (!StringUtils.containsIgnoreCase(props, "hostName")) {
                        props = "hostName=" + rmiHostName + ((props != null) ? "," + props : "");
                        rmiPeerListFact.setProperties(props);
                    } else {
                        logger.warn("Cannot configure RMICacheManagerPeerListenerFactory hostName! It's already configured. Current value=" + props);
                    }
                }
            }
        }

        return config;
    }
}
