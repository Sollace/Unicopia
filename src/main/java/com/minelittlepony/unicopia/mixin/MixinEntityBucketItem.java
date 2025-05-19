package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.EntityType;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.sound.SoundEvent;

@Mixin(EntityBucketItem.class)
public interface MixinEntityBucketItem {
    @Accessor
    EntityType<?> getEntityType();
    @Accessor
    SoundEvent getEmptyingSound();
}
