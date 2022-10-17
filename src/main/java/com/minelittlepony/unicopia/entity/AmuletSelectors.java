package com.minelittlepony.unicopia.entity;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.entity.LivingEntity;

public interface AmuletSelectors {
    Predicate<LivingEntity> ALICORN_AMULET = UItems.ALICORN_AMULET::isApplicable;
}
