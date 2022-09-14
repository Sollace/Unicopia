package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/**
 * Interface for any magically capable entities that can cast or persist spells.
 */
public interface Caster<E extends LivingEntity> extends Owned<E>, Levelled, Affine, ParticleSource {

    Physics getPhysics();

    SpellContainer getSpellSlot();

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
    default World getReferenceWorld() {
        return getEntity().getEntityWorld();
    }

    /**
     * Returns true if we're executing on the client.
     */
    default boolean isClient() {
        return getReferenceWorld().isClient();
    }

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return getEntity().getBlockPos();
    }

    default boolean canModifyAt(BlockPos pos) {

        if (!canCastAt(Vec3d.ofCenter(pos))) {
            return false;
        }

        if (getMaster() instanceof PlayerEntity) {
            return getReferenceWorld().canPlayerModifyAt((PlayerEntity)getMaster(), pos);
        }
        return getReferenceWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    default void playSound(SoundEvent sound, float volume, float pitch) {
        getReferenceWorld().playSound(null, getEntity().getX(), getEntity().getY(), getEntity().getZ(), sound, getEntity().getSoundCategory(), volume, pitch);
    }

    /**
     * Removes the desired amount of mana or health from this caster in exchange for a spell's benefits.
     * <p>
     * @return False if the transaction has depleted the caster's reserves.
     */
    boolean subtractEnergyCost(double amount);

    default Stream<Caster<?>> findAllSpellsInRange(double radius) {
        return findAllSpellsInRange(radius, null);
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius, @Nullable Predicate<Entity> test) {
        return stream(findAllEntitiesInRange(radius, test == null ? EquinePredicates.IS_CASTER : EquinePredicates.IS_CASTER.and(test)));
    }

    default Stream<Entity> findAllEntitiesInRange(double radius, @Nullable Predicate<Entity> test) {
        return VecHelper.findInRange(getEntity(), getReferenceWorld(), getOriginVector(), radius, test).stream();
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return findAllEntitiesInRange(radius, null);
    }

    default boolean canCast() {
        return canCastAt(getOriginVector());
    }

    default boolean canCastAt(Vec3d pos) {
        return findAllSpellsInRange(500, SpellType.ARCANE_PROTECTION::isOn).noneMatch(caster -> caster
                .getSpellSlot().get(SpellType.ARCANE_PROTECTION, false)
                .filter(spell -> spell.blocksMagicFor(caster, this, pos))
                .isPresent()
        );
    }

    static Stream<Caster<?>> stream(Stream<Entity> entities) {
        return entities.map(Caster::of).flatMap(Optional::stream);
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
