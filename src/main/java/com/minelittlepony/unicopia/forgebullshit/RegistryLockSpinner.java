package com.minelittlepony.unicopia.forgebullshit;

import java.lang.reflect.Field;

import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.registries.ILockableRegistry;

public class RegistryLockSpinner {

    public static void unlock(RegistryNamespaced<?, ?> registry) {
        if (registry instanceof ILockableRegistry) {
            try {
                Field f = registry.getClass().getDeclaredField("locked");

                f.setAccessible(true);
                f.setBoolean(registry, false);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static <K, V> void commit(RegistryNamespaced<K, V> registry, V from, V to, Class<?> inClass) {

        registry.register(registry.getIDForObject(from), registry.getNameForObject(from), to);

        for (Field i : inClass.getDeclaredFields()) {
            try {
                if (i.get(null) == from) {
                    i.set(null, to);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void lock(RegistryNamespaced<?, ?> registry) {
        if (registry instanceof ILockableRegistry) {
            ((ILockableRegistry) registry).lock();
        }
    }
}
