package com.minelittlepony.unicopia;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;

public interface EquineContext {
    EquineContext ABSENT = () -> Race.UNSET;
    Container ABSENT_REF = () -> ABSENT;

    Race getSpecies();

    default Race.Composite getCompositeRace() {
        return getSpecies().composite();
    }

    default float getCloudWalkingStrength() {
        return 0;
    }

    default boolean collidesWithClouds() {
        return getCompositeRace().canInteractWithClouds() || getCloudWalkingStrength() >= 1;
    }

    default boolean hasFeatherTouch() {
        return false;
    }

    static EquineContext of(ShapeContext context) {
        if (context == ShapeContext.absent()) {
            return InteractionManager.getInstance().getEquineContext();
        }
        EquineContext result = context instanceof Container c ? c.get() : ABSENT;
        return result == null ? ABSENT : result;
    }

    static EquineContext of(ItemUsageContext context) {
        return MoreObjects.firstNonNull(Pony.of(context.getPlayer()), ABSENT);
    }

    static EquineContext of(@Nullable Entity entity) {
        if (entity instanceof EquineContext c) {
            return c;
        }
        return MoreObjects.firstNonNull(Equine.of(entity).orElse(null), ABSENT);
    }

    static Container lazy(@Nullable Entity entity) {
        if (entity instanceof EquineContext c) {
            return () -> c;
        }
        return MoreObjects.firstNonNull(Equine.cast(entity), ABSENT_REF);
    }

    interface Container {
        EquineContext get();
    }
}
