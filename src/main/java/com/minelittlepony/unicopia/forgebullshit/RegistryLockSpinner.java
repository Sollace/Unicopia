package com.minelittlepony.unicopia.forgebullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.ILockableRegistry;

public final class RegistryLockSpinner {

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

        Field modifieres = null;
        try {
            modifieres = Field.class.getDeclaredField("modifiers");
            modifieres.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e1) {
            e1.printStackTrace();
        }

        for (Field i : inClass.getDeclaredFields()) {
            try {
                if (i.get(null) == from) {
                    i.setAccessible(true);

                    if (Modifier.isFinal(i.getModifiers())) {
                        if (modifieres == null) {
                            continue;
                        }

                        modifieres.setInt(i, i.getModifiers() & ~Modifier.FINAL);
                    }

                    i.set(null, to);
                }
            } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
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
