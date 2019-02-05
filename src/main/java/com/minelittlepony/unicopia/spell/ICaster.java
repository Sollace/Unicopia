package com.minelittlepony.unicopia.spell;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICaster<E extends EntityLivingBase> extends IOwned<E>, ILevelled, IAligned {

    void setEffect(@Nullable IMagicEffect effect);

    @Nullable
    IMagicEffect getEffect();

    default boolean hasEffect() {
        return getEffect() != null;
    }

    /**
     * Gets the entity directly responsible for casting.
     */
    default Entity getEntity() {
        return getOwner();
    }

    default UUID getUniqueId() {
        return getEntity().getUniqueID();
    }

    default World getWorld() {
        return getEntity().getEntityWorld();
    }

    default BlockPos getOrigin() {
        return getEntity().getPosition();
    }

    default Vec3d getOriginVector() {
        return getEntity().getPositionVector();
    }

    default void spawnParticles(IShape area, int count, Consumer<Vec3d> particleSpawner) {
        Random rand = getWorld().rand;

        Vec3d pos = getOriginVector();

        for (int i = 0; i < count; i++) {
            particleSpawner.accept(area.computePoint(rand).add(pos));
        }
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }
}
