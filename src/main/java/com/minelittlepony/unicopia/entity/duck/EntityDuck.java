package com.minelittlepony.unicopia.entity.duck;

import java.util.Set;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.compat.pehkui.PehkuiEntityExtensions;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.network.track.Trackable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.entity.EntityLike;

public interface EntityDuck extends EntityLike, EntityConvertable<Entity>, LavaAffine, PehkuiEntityExtensions, Guest, Trackable, Equine.Container {

    Set<TagKey<Fluid>> getSubmergedFluidTags();

    void setRemovalReason(RemovalReason reason);

    void setVehicle(Entity vehicle);

    @Override
    default void setLavaAffine(boolean lavaAffine) {

    }
}
