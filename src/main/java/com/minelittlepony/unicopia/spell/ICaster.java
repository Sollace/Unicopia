package com.minelittlepony.unicopia.spell;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast and persist spells.
 */
public interface ICaster<E extends EntityLivingBase> extends IOwned<E>, ILevelled, IAligned, IMagicals {

    void setEffect(@Nullable IMagicEffect effect);

    /**
     * Gets the active effect for this caster.
     */
    @Nullable
    default IMagicEffect getEffect(boolean update) {
        return getEffect(null, update);
    }

    /**
     * Gets the active effect for the matching given type.
     * Returns null if no such effect exists for this caster.
     */
    @Nullable
    <T extends IMagicEffect> T getEffect(@Nullable Class<T> type, boolean update);

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    @Nullable
    default IMagicEffect getEffect() {
        return getEffect(true);
    }

    /**
     * Returns true if this caster has an active effect attached to it.
     */
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
     * Returns true if we're executing on the client.
     */
    default boolean isRemote() {
        return getWorld().isRemote;
    }

    /**
     * Returns true if we're executing on the server.
     */
    default boolean isLocal() {
        return !isRemote();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return getEntity().getPosition();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default Vec3d getOriginVector() {
        return getEntity().getPositionVector();
    }

    /**
     * Returns a new caster at the given position.
     * This one is not altered, rather the method will return an entirely new caster with the same owner as this one.
     */
    default ICaster<?> at(BlockPos newOrigin) {
        EntitySpell spell = new EntitySpell(getWorld());
        spell.setPosition(newOrigin.getX(), newOrigin.getY(), newOrigin.getZ());
        spell.setOwner(getOwner());

        return spell;
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

    default boolean subtractEnergyCost(double amount) {
        getOwner().attackEntityFrom(DamageSource.MAGIC, (int)amount/2);

        return getOwner().getHealth() > 0;
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
