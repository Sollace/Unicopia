package com.minelittlepony.unicopia.compat.pehkui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;

@SuppressWarnings({ "rawtypes", "unchecked" })
public interface PehkUtil {
    boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("pehkui");

    static void copyScale(Entity from, @Nullable Entity to) {
        if (!IS_LOADED || to == null) {
            return;
        }
        Map toScales = ((EntityDuck)to).pehkui_getScales();
        toScales.clear();
        toScales.putAll(((EntityDuck)from).pehkui_getScales());
    }

    static void clearScale(@Nullable Entity entity) {
        if (!IS_LOADED || entity == null) {
            return;
        }
        ((EntityDuck)entity).pehkui_getScales().clear();
    }

    static <T> T ignoreScaleFor(@Nullable Entity entity, Function<Entity, T> action) {
        if (!IS_LOADED || entity == null) {
            return action.apply(entity);
        }
        Map scales = ((EntityDuck)entity).pehkui_getScales();
        Map copy = new HashMap<>(scales);
        //scales.clear();
        try {
            return action.apply(entity);
        } finally {
            scales.putAll(copy);
        }
    }
}
