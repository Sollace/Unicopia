package com.minelittlepony.unicopia.util;

public interface Untyped {
    @SuppressWarnings("unchecked")
    static <K, T extends K> T cast(K t) {
        return (T)t;
    }
}
