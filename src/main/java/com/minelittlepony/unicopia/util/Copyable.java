package com.minelittlepony.unicopia.util;

public interface Copyable<T extends Copyable<T>> {
    void copyFrom(T other);
}
