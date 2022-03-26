package com.minelittlepony.unicopia.util;

import java.util.HashSet;
import java.util.Set;

public final class ItemTrace {
    private static final Set<Class<?>> noticedClasses = new HashSet<>();
    public static void traceItem(Class<?> c) {
        if (noticedClasses.add(c)) {
            System.out.println(c.getCanonicalName());
        }
    }
}