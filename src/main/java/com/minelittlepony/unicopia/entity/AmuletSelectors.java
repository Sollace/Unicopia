package com.minelittlepony.unicopia.entity;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.entity.LivingEntity;

public interface AmuletSelectors {
    Predicate<LivingEntity> ALICORN_AMULET = UItems.ALICORN_AMULET::isApplicable;
    Predicate<LivingEntity> PEGASUS_AMULET = UItems.PEGASUS_AMULET::isApplicable;
    Predicate<LivingEntity> UNICORN_AMULET = UItems.UNICORN_AMULET::isApplicable;

    Predicate<LivingEntity> ALICORN_AMULET_AFTER_1_DAYS = ALICORN_AMULET.and(ItemTracker.wearing(UItems.ALICORN_AMULET, ItemTracker.after(ItemTracker.DAYS)));
    Predicate<LivingEntity> ALICORN_AMULET_AFTER_2_DAYS = ALICORN_AMULET.and(ItemTracker.wearing(UItems.ALICORN_AMULET, ItemTracker.after(2 * ItemTracker.DAYS)));
    Predicate<LivingEntity> ALICORN_AMULET_AFTER_3_DAYS = ALICORN_AMULET.and(ItemTracker.wearing(UItems.ALICORN_AMULET, ItemTracker.after(3 * ItemTracker.DAYS)));
    Predicate<LivingEntity> ALICORN_AMULET_AFTER_4_DAYS = ALICORN_AMULET.and(ItemTracker.wearing(UItems.ALICORN_AMULET, ItemTracker.after(3 * ItemTracker.DAYS)));
}
