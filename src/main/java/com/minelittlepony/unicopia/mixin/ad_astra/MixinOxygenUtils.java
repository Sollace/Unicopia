package com.minelittlepony.unicopia.mixin.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Pseudo
@Mixin(
        targets = { "earth.terrarium.ad_astra.common.util.OxygenUtils" },
        remap = false
)
public interface MixinOxygenUtils {
    @Invoker("entityHasOxygen")
    static boolean entityHasOxygen(World world, LivingEntity entity) {
        return true;
    }
}
