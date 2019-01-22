package com.minelittlepony.unicopia.spell;

import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICaster<E extends EntityLivingBase> extends IOwned<E>, ILevelled {

    void setEffect(IMagicEffect effect);

    IMagicEffect getEffect();

    SpellAffinity getAffinity();

    default boolean hasEffect() {
        return getEffect() != null;
    }

    /**
     * Gets the entity directly responsible for casting.
     */
    default Entity getEntity() {
        return getOwner();
    }

    default World getWorld() {
        return getEntity().getEntityWorld();
    }

    default BlockPos getOrigin() {
        return getEntity().getPosition();
    }

    default void spawnParticles(IShape area, int count, Consumer<Vec3d> particleSpawner) {
        Random rand = getWorld().rand;

        int x = getOrigin().getX();
        int y = getOrigin().getY();
        int z = getOrigin().getZ();

        for (int i = 0; i < count; i++) {
            particleSpawner.accept(area.computePoint(rand).add(x, y, z));
        }
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }
}
