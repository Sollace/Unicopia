package com.minelittlepony.unicopia.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.IOwned;
import com.minelittlepony.util.VecHelper;
import com.minelittlepony.util.shape.IShape;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast and persist spells.
 */
public interface ICaster<E extends LivingEntity> extends IOwned<E>, ILevelled, IAffine, IMagicals {

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

    @SuppressWarnings("unchecked")
    default <T extends IMagicEffect> Optional<T> getEffect(Class<T> type) {
        IMagicEffect effect = getEffect();

        if (effect == null || effect.getDead() || !type.isAssignableFrom(effect.getClass())) {
            return Optional.empty();
        }

        return Optional.of((T)effect);
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
        return getEntity().getUuid();
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
    default boolean isClient() {
        return getWorld().isClient();
    }

    /**
     * Returns true if we're executing on the server.
     */
    default boolean isLocal() {
        return !isClient();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return getEntity().getBlockPos();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default Vec3d getOriginVector() {
        return getEntity().getPos();
    }

    default void spawnParticles(ParticleEffect particleId, int count) {
        // TODO:
        // ParticleTypeRegistry.getTnstance().getSpawner().spawnParticles(particleId, getEntity(), count);
    }

    default void spawnParticles(IShape area, int count, Consumer<Vec3d> particleSpawner) {
        Vec3d pos = getOriginVector();

        area.randomPoints(count, getWorld().random).stream()
            .map(point -> point.add(pos))
            .forEach(particleSpawner);
    }

    default boolean subtractEnergyCost(double amount) {
        getOwner().damage(DamageSource.MAGIC, (int)amount/2);

        return getOwner().getHealth() > 0;
    }

    default Stream<ICaster<?>> findAllSpells() {
        return CasterUtils.findAllSpells(this);
    }

    default Stream<ICaster<?>> findAllSpellsInRange(double radius) {
        return CasterUtils.findAllSpellsInRange(this, radius);
    }

    default Stream<ICaster<?>> findAllSpellsInRange(Box bb) {
        return CasterUtils.findAllSpellsInRange(this, bb);
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }
}
