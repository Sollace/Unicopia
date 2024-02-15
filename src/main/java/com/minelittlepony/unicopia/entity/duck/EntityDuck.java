package com.minelittlepony.unicopia.entity.duck;

import java.util.Set;

import com.minelittlepony.unicopia.compat.pehkui.PehkuiEntityExtensions;
import com.minelittlepony.unicopia.entity.behaviour.Guest;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public interface EntityDuck extends LavaAffine, PehkuiEntityExtensions, Guest {

    Set<TagKey<Fluid>> getSubmergedFluidTags();

    void setRemovalReason(RemovalReason reason);

    void setVehicle(Entity vehicle);

    float getNextStepSoundDistance();

    @Override
    default void setLavaAffine(boolean lavaAffine) {

    }
}
