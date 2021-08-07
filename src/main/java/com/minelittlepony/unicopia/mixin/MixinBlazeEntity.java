package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.mob.BlazeEntity;

@Mixin(BlazeEntity.class)
public interface MixinBlazeEntity {
    @Invoker
    void invokeSetFireActive(boolean active);
}
