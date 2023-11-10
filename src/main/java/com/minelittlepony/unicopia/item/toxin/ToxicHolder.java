package com.minelittlepony.unicopia.item.toxin;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;

public interface ToxicHolder {
    Toxic getToxic(ItemStack stack, @Nullable LivingEntity entity);
}
