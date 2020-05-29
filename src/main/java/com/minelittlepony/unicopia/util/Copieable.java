package com.minelittlepony.unicopia.util;

public interface Copieable<T extends Copieable<T>> {
    void copyFrom(T other);
}
