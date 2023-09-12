package com.minelittlepony.unicopia.compat.ad_astra;

import com.minelittlepony.unicopia.mixin.ad_astra.MixinOxygenUtils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public interface OxygenUtils {
    boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("ad_astra");

    static boolean entityHasOxygen(World world, LivingEntity entity) {
        if (MOD_LOADED) {
            return MixinOxygenUtils.entityHasOxygen(world, entity);
        }
        return false;
    }
}
