package com.minelittlepony.unicopia;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;

public interface EquineContext {
    EquineContext ABSENT = () -> Race.UNSET;

    Race getSpecies();

    default Race.Composite getCompositeRace() {
        return getSpecies().composite();
    }

    default float getCloudWalkingStrength() {
        return 0;
    }

    static EquineContext of(ShapeContext context) {
        if (context == ShapeContext.absent()) {
            return Unicopia.SIDE.getPony().map(EquineContext.class::cast).orElse(ABSENT);
        }
        return context instanceof EquineContext c ? c : ABSENT;
    }

    static EquineContext of(ItemUsageContext context) {
        return MoreObjects.firstNonNull(Pony.of(context.getPlayer()), ABSENT);
    }

    static EquineContext of(Entity entity) {
        if (entity instanceof EquineContext c) {
            return c;
        }
        return MoreObjects.firstNonNull(Equine.of(entity).orElse(null), ABSENT);
    }
}
