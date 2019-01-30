package com.minelittlepony.unicopia.forgebullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.registries.ILockableRegistry;

@FUF(reason = "Forge locks the registries. We need a way to unlock them.")
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



        for (Field i : inClass.getDeclaredFields()) {
            try {
                if (i.get(null) == from) {
                    i.setAccessible(true);
                    makeNonFinal(i).set(null, to);
                }
            } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean init = false;
    private static Field modifieres = null;

    protected static void initModifiersField(Field f) {
        if (!init) {
            init = true;
            try {
                modifieres = Field.class.getDeclaredField("modifiers");
                modifieres.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException e1) {
                e1.printStackTrace();
            }
        }
    }

    @FUF(reason = "Not exactly forge's fault, but it was would be nice of them to not leave these as final")
    protected static Field makeNonFinal(Field f) throws IllegalArgumentException, IllegalAccessException {
        initModifiersField(f);
        if (Modifier.isFinal(f.getModifiers()) && modifieres != null) {
            modifieres.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        }

        return f;
    }

    public static void lock(RegistryNamespaced<?, ?> registry) {
        if (registry instanceof ILockableRegistry) {
            ((ILockableRegistry) registry).lock();
        }
    }
}
