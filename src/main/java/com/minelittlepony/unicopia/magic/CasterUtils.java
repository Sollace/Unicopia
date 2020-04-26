package com.minelittlepony.unicopia.magic;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ducks.PonyContainer;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CasterUtils {
    /**
     * Finds all surrounding spells withing range from the given caster.
     */
    public static Stream<Caster<?>> findAllSpellsInRange(Caster<?> source, double radius) {

        BlockPos origin = source.getOrigin();

        BlockPos begin = origin.add(-radius, -radius, -radius);
        BlockPos end = origin.add(radius, radius, radius);

        Box bb = new Box(begin, end);

        return source.getWorld().getEntities(source.getEntity(), bb, e ->
            !e.removed && (e instanceof Caster || e instanceof PlayerEntity)
        ).stream().filter(e -> {
                double dist = e.squaredDistanceTo(origin.getX(), origin.getY(), origin.getZ());
                double dist2 = e.squaredDistanceTo(origin.getX(), origin.getY() - e.getStandingEyeHeight(), origin.getZ());

                return dist <= radius || dist2 <= radius;
            })
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    static Stream<Caster<?>> findAllSpellsInRange(Caster<?> source, Box bb) {
        return source.getWorld().getEntities(source.getEntity(), bb, e -> !e.removed && (e instanceof Caster || EquinePredicates.PLAYER_UNICORN.test(e))).stream()
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    static <T extends MagicEffect> Optional<T> toMagicEffect(Class<T> type, @Nullable Entity entity) {
        return toCaster(entity)
                .filter(Caster::hasEffect)
                .map(caster -> caster.getEffect(type, false))
                .filter(e -> !e.isDead());
    }

    /**
     * Determines if the passed in entity is holding the named effect.
     * By holding that meant the effect must be attached to the caster associated with the entity.
     */
    public static boolean isHoldingEffect(String effectName, Entity entity) {
        return Streams.stream(entity.getArmorItems())
            .map(SpellRegistry::getKeyFromStack)
            .anyMatch(s -> s.equals(effectName));
    }

    /**
     * Attempts to convert the passed entity into a caster using all the known methods.
     */
    public static Optional<Caster<?>> toCaster(@Nullable Entity entity) {
        if (entity instanceof Caster<?>) {
            return Optional.of((Caster<?>)entity);
        }

        if (entity instanceof LivingEntity && !(entity instanceof IMagicals)) {
            return PonyContainer.of(entity).map(PonyContainer::getCaster);
        }

        return Optional.empty();
    }
}
