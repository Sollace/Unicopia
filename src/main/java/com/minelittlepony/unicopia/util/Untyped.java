package com.minelittlepony.unicopia.util;

import java.util.Optional;

public interface Untyped {
    @SuppressWarnings("unchecked")
    static <K, T extends K> T cast(K t) {
        return (T)t;
    }

    static <K, T extends K> Optional<T> cast(Optional<K> t) {
        return t.map(Untyped::cast);
    }
}
