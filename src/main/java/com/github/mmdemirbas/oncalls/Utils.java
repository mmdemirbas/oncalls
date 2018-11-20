package com.github.mmdemirbas.oncalls;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 12:20
 */
public final class Utils {
    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Entry<K, V>... entries) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Entry<K, V> pair(K key, V value) {
        return new SimpleImmutableEntry<>(key, value);
    }

    public static <T> T nextOrNull(Iterator<? extends T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static <T, R> List<R> map(Collection<? extends T> items, Function<? super T, ? extends R> mapper) {
        return items.stream()
                    .map(mapper)
                    .collect(Collectors.toList());
    }

    public static <T, R> R reduce(R seed,
                                  Iterable<? extends T> items,
                                  BiFunction<? super R, ? super T, ? extends R> reduce) {
        R result = seed;
        if (items != null) {
            for (T item : items) {
                result = reduce.apply(result, item);
            }
        }
        return result;
    }
}