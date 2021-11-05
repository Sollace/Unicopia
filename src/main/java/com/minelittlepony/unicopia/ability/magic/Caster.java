package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast or persist spells.
 */
public interface Caster<E extends LivingEntity> extends Owned<E>, Levelled, Affine, ParticleSource {

    Physics getPhysics();

    SpellContainer getSpellSlot();

    default void setSpell(@Nullable Spell spell) {
        getSpellSlot().put(spell);
    }

    /**
     * Gets the entity directly responsible for casting.
     */
    @Override
    default Entity getEntity() {
        return getMaster();
    }

    /**
     * Gets the minecraft world
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
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return getEntity().getBlockPos();
    }

    /**
     * Removes the desired amount of mana or health from this caster in exchange for a spell's benefits.
     */
    boolean subtractEnergyCost(double amount);

    default Stream<Caster<?>> findAllSpellsInRange(double radius) {
        return findAllSpellsInRange(radius, null);
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius, @Nullable Predicate<Entity> test) {
        return stream(findAllEntitiesInRange(radius, test == null ? EquinePredicates.IS_CASTER : EquinePredicates.IS_CASTER.and(test)));
    }

    default Stream<Entity> findAllEntitiesInRange(double radius, @Nullable Predicate<Entity> test) {
        return VecHelper.findInRange(getEntity(), getWorld(), getOriginVector(), radius, test).stream();
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return findAllEntitiesInRange(radius, null);
    }

    static Stream<Caster<?>> stream(Stream<Entity> entities) {
        return entities
                .map(Caster::of)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Attempts to convert the passed entity into a caster using all the known methods.
     */
    static Optional<Caster<?>> of(@Nullable Entity entity) {
        if (entity instanceof Caster<?>) {
            return Optional.of((Caster<?>)entity);
        }

        return PonyContainer.of(entity)
                .map(PonyContainer::get)
                .filter(c -> c instanceof Caster<?>)
                .map(c -> (Caster<?>)c);
    }
}
