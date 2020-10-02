package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast or persist spells.
 */
public interface Caster<E extends LivingEntity> extends Owned<E>, Levelled, Affine, Magical, ParticleSource {

    EffectSync getPrimarySpellSlot();

    default void setSpell(@Nullable Spell spell) {
        getPrimarySpellSlot().set(spell);
    }

    /**
     * Gets the active effect for this caster.
     */
    @Nullable
    default Spell getSpell(boolean update) {
        return getSpell(null, update);
    }

    /**
     * Gets the active effect for the matching given type.
     * Returns null if no such effect exists for this caster.
     */
    @Nullable
    default <T extends Spell> T getSpell(@Nullable Class<T> type, boolean update) {
        return getPrimarySpellSlot().get(type, update);
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> getSpellOrEmpty(Class<T> type, boolean update) {
        return getPrimarySpellSlot().getOrEmpty(type, update);
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> getSpellOrEmpty(Class<T> type) {
        return getSpellOrEmpty(type, true);
    }

    /**
     * Returns true if this caster has an active effect attached to it.
     */
    default boolean hasSpell() {
        return getPrimarySpellSlot().has();
    }

    /**
     * Gets the entity directly responsible for casting.
     */
    @Override
    default Entity getEntity() {
        return getOwner();
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

    default boolean subtractEnergyCost(double amount) {
        getOwner().damage(DamageSource.MAGIC, (int)amount/2);
        return getOwner().getHealth() > 0;
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius) {
        return CasterUtils.findInRange(this, radius);
    }

    default Stream<Caster<?>> findAllSpellsInRange(Box bb) {
        return CasterUtils.findInRange(this, bb);
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return VecHelper.findAllEntitiesInRange(getEntity(), getWorld(), getOrigin(), radius);
    }
}
