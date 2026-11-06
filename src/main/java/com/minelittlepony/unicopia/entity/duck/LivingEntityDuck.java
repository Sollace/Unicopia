package com.minelittlepony.unicopia.entity.duck;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public interface LivingEntityDuck extends EntityDuck, Equine.Container<Living<?>> {
    void updateItemUsage(Hand hand, ItemStack stack, int time);

    boolean isJumping();

    float getLeaningPitch();

    void setLeaningPitch(float pitch);

    float getLastLeaningPitch();

    void setLastLeaningPitch(float pitch);

    default void copyLeaningAnglesFrom(LivingEntityDuck other) {
        setLeaningPitch(other.getLeaningPitch());
        setLastLeaningPitch(other.getLastLeaningPitch());
    }

    static Vec3d serverPos(LivingEntity entity) {
        return entity.getTrackedPosition().getPos();
    }
}
