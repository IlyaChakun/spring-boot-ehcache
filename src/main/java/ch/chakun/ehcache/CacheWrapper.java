package ch.chakun.ehcache;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class CacheWrapper<KeyType, ValueType> {
    private final Serializer<ValueType> serializer;
    private final Cache cache;
    private final String cacheName;

    public CacheWrapper(String cacheName) {
        this(cacheName, new Serializer<>() {
            @Override
            public Object serialize(ValueType value) {
                return value;
            }

            @Override
            public ValueType deserialize(Object value) {
                //noinspection unchecked
                return (ValueType) value;
            }
        });
    }

    public CacheWrapper(String cacheName, Serializer<ValueType> serializer) {
        this.cacheName = cacheName;
        this.cache = CacheManager.getCache(cacheName);
        this.serializer = serializer;

        if (cache == null) // should we fail immediately?
            log.error("Unable to create the CacheWrapper, ehcache configuration is missing, cacheName={}", cacheName);
    }

    public void put(KeyType key, ValueType value) {
        if (cache == null)
            throw new RuntimeException("Unable to store the value, ehcache configuration is missing, cacheName=" + cacheName);
        cache.put(new Element(key, serializer.serialize(value)));
    }

    public ValueType get(KeyType key) {
        if (cache == null) return null;
        return Optional.ofNullable(cache.get(key))
                .map(Element::getObjectValue)
                .map(serializer::deserialize)
                .orElse(null);
    }


    public ValueType getOrDefault(KeyType key, ValueType defaultValue) {
        ValueType valueType;
        return (((valueType = get(key)) != null) || contains(key))
                ? valueType
                : defaultValue;
    }

    public ValueType computeIfAbsent(KeyType key,
                                     Function<? super KeyType, ? extends ValueType> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        ValueType valueType;
        if ((valueType = get(key)) == null) {
            ValueType newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return valueType;
    }

    public boolean remove(KeyType key) {
        return cache.remove(key);
    }

    public List<KeyType> getKeysWithExpiryCheck() {
        //noinspection unchecked
        return (List<KeyType>) cache.getKeysWithExpiryCheck();
    }

    public boolean contains(KeyType key) {
        return cache.get(key) != null;
    }

    public ValueType getAndRemove(KeyType key) {
        ValueType value = get(key);
        if (key != null) {
            remove(key);
        }
        return value;
    }

    public interface Serializer<ValueType> {
        Object serialize(ValueType value);

        ValueType deserialize(Object value);
    }
}
