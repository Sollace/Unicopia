package com.minelittlepony.unicopia.item;

import net.minecraft.entity.damage.DamageSource;

public interface DamageChecker {
    boolean takesDamageFrom(DamageSource source);
}
