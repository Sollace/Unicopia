package com.minelittlepony.util.collection;

import java.util.HashMap;
import java.util.Optional;

public class Pool<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = -4794854344664655790L;

    private final K defaultKey;

    @SuppressWarnings("unchecked")
    public static <K, V> Pool<K, V> of(K def, Object...entries) {
        Pool<K, V> result = new Pool<>(def);

        for (int i = 0; i < entries.length - 1; i += 2) {
            result.put((K)entries[i], (V)entries[i + 1]);
        }

        return result;
    }

    public Pool(K defKey) {
        defaultKey = defKey;
    }

    public Pool<K, V> add(K key, V value) {
        put(key, value);

        return this;
    }

    @Override
    public V get(Object key) {
        if (key == null || !containsKey(key)) {
            key = defaultKey;
        }

        return super.get(key);
    }

    public Optional<V> getOptional(K key) {
        return Optional.ofNullable(get(key));
    }
}
