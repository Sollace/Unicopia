package com.minelittlepony.unicopia.spell;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICaster<E extends EntityLivingBase> extends IOwned<E>, ILevelled, IAligned {

    void setEffect(@Nullable IMagicEffect effect);

    @Nullable
    default IMagicEffect getEffect(boolean update) {
        return getEffect(null, update);
    }

    @Nullable
    <T extends IMagicEffect> T getEffect(@Nullable Class<T> type, boolean update);

    @Nullable
    default IMagicEffect getEffect() {
        return getEffect(true);
    }

    boolean hasEffect();

    /**
     * Gets the entity directly responsible for casting.
     */
    default Entity getEntity() {
        return getOwner();
    }

    /**
     * Gets the unique id associated with this caste.
     */
    default UUID getUniqueId() {
        return getEntity().getUniqueID();
    }

    /**
     * gets the minecraft world
     */
    default World getWorld() {
        return getEntity().getEntityWorld();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return getEntity().getPosition();
    }

    default Vec3d getOriginVector() {
        return getEntity().getPositionVector();
    }

    default void spawnParticles(int particleId, int count, int...args) {
        IPower.spawnParticles(particleId, getEntity(), count, args);
    }

    default void spawnParticles(IShape area, int count, Consumer<Vec3d> particleSpawner) {
        Random rand = getWorld().rand;

        Vec3d pos = getOriginVector();

        for (int i = 0; i < count; i++) {
            particleSpawner.accept(area.computePoint(rand).add(pos));
        }
    }

    default Stream<ICaster<?>> findAllSpells() {
        return CasterUtils.findAllSpells(this);
    }

    default Stream<ICaster<?>> findAllSpellsInRange(double radius) {
        return CasterUtils.findAllSpellsInRange(this, radius);
    }

    default Stream<ICaster<?>> findAllSpellsInRange(AxisAlignedBB bb) {
        return CasterUtils.findAllSpellsInRange(this, bb);
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }


}
