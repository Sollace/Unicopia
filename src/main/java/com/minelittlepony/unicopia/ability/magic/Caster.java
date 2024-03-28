package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.damage.UDamageSources;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.server.world.OfflinePlayerCache;
import com.minelittlepony.unicopia.util.SoundEmitter;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

/**
 * Interface for any magically capable entities that can cast or persist spells.
 */
public interface Caster<E extends Entity> extends
        Owned<LivingEntity>,
        Levelled,
        Affine,
        ParticleSource<E>,
        SoundEmitter<E>,
        EntityConvertable<E>,
        UDamageSources {

    Physics getPhysics();

    SpellContainer getSpellSlot();

    /**
     * Removes the desired amount of mana or health from this caster in exchange for a spell's benefits.
     * <p>
     * @return False if the transaction has depleted the caster's reserves.
     */
    boolean subtractEnergyCost(double amount);

    /**
     * Gets the original caster responsible for this spell.
     * If none is found, will return itself.
     */
    default Caster<?> getOriginatingCaster() {
        return of(getMaster()).orElse(this);
    }

    default boolean canModifyAt(BlockPos pos) {
        return canModifyAt(pos, ModificationType.EITHER);
    }

    default boolean canModifyAt(BlockPos pos, ModificationType mod) {

        if (mod.checkPhysical()) {
            if (asWorld().getBlockState(pos).getHardness(asWorld(), pos) < 0) {
                return false;
            }

            if (getMaster() instanceof PlayerEntity player) {
                if (!player.canModifyBlocks() || !asWorld().canPlayerModifyAt(player, pos)) {
                    return false;
                }
            } else {
                if (asWorld() instanceof ServerWorld sw) {
                    @Nullable
                    PlayerEntity player = OfflinePlayerCache.getOfflinePlayer(sw, getMasterId().orElse(null));
                    if (player != null && !player.canModifyBlocks() || !sw.canPlayerModifyAt(player, pos)) {
                        return false;
                    }
                }

                if (!asWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    return false;
                }
            }
        }

        return !mod.checkMagical() || canCastAt(Vec3d.ofCenter(pos));
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius) {
        return findAllSpellsInRange(radius, null);
    }

    default Stream<Caster<?>> findAllSpellsInRange(double radius, @Nullable Predicate<Entity> test) {
        return stream(findAllEntitiesInRange(radius, test == null ? EquinePredicates.IS_CASTER : EquinePredicates.IS_CASTER.and(test)));
    }

    default Stream<Entity> findAllEntitiesInRange(double radius, @Nullable Predicate<Entity> test) {
        return VecHelper.findInRange(asEntity(), asWorld(), getOriginVector(), radius, test).stream();
    }

    default Stream<Entity> findAllEntitiesInRange(double radius) {
        return findAllEntitiesInRange(radius, null);
    }

    default boolean canCast() {
        return canCastAt(getOriginVector());
    }

    default boolean canCastAt(Vec3d pos) {
        return !Ether.get(asWorld()).anyMatch(SpellType.ARCANE_PROTECTION, (spell, caster) -> spell.blocksMagicFor(caster, this, pos));
    }

    default boolean canUse(Ability<?> ability) {
        return false;
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

        return Equine.<Entity, Caster<?>>of(entity, c -> c instanceof Caster<?>);
    }
}
