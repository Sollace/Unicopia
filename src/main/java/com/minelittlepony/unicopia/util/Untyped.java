package com.minelittlepony.unicopia.util;

public interface Untyped {
    @SuppressWarnings("unchecked")
    static <K, T> T cast(K t) {
        return (T)t;
    }
}
