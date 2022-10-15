package com.minelittlepony.unicopia.entity.duck;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.PonyContainer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface LivingEntityDuck extends PonyContainer<Equine<?>> {
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
}
