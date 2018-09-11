package come.minelittlepony.unicopia.forgebullshit;

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

    public static void lock(RegistryNamespaced<?, ?> registry) {
        if (registry instanceof ILockableRegistry) {
            ((ILockableRegistry) registry).lock();
        }
    }
}
