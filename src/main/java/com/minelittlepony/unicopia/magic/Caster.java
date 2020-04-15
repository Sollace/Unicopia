package com.minelittlepony.unicopia.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.Owned;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.particles.ParticleSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast and persist spells.
 */
public interface Caster<E extends LivingEntity> extends Owned<E>, Levelled, Affine, IMagicals, ParticleSource {

    void setEffect(@Nullable MagicEffect effect);

    /**
     * Gets the active effect for this caster.
     */
    @Nullable
    default MagicEffect getEffect(boolean update) {
        return getEffect(null, update);
    }

    /**
     * Gets the active effect for the matching given type.
     * Returns null if no such effect exists for this caster.
     */
    @Nullable
    <T extends MagicEffect> T getEffect(@Nullable Class<T> type, boolean update);

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    @Nullable
    default MagicEffect getEffect() {
        return getEffect(true);
    }

    @SuppressWarnings("unchecked")
    default <T extends MagicEffect> Optional<T> getEffect(Class<T> type) {
        MagicEffect effect = getEffect();

        if (effect == null || effect.isDead() || !type.isAssignableFrom(effect.getClass())) {
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
    @Override
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
    @Override
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
    @Override
    default Vec3d getOriginVector() {
        return getEntity().getPos();
    }

    default boolean subtractEnergyCost(double amount) {
        getOwner().damage(DamageSource.MAGIC, (int)amount/2);

        return getOwner().getHealth() > 0;
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius) {
        return CasterUtils.findAllSpellsInRange(this, radius);
    }

    default Stream<Caster<?>> findAllSpellsInRange(Box bb) {
        return CasterUtils.findAllSpellsInRange(this, bb);
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }
}
