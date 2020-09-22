package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.PonyContainer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CasterUtils {
    /**
     * Finds all surrounding spells withing range from the given caster.
     */
    public static Stream<Caster<?>> findInRange(Caster<?> source, double radius) {

        BlockPos origin = source.getOrigin();

        BlockPos begin = origin.add(-radius, -radius, -radius);
        BlockPos end = origin.add(radius, radius, radius);

        Box bb = new Box(begin, end);

        return source.getWorld().getOtherEntities(source.getEntity(), bb, e ->
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

    static Stream<Caster<?>> findInRange(Caster<?> source, Box bb) {
        return source.getWorld().getOtherEntities(source.getEntity(), bb, e -> !e.removed && (e instanceof Caster || EquinePredicates.PLAYER_UNICORN.test(e))).stream()
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    /**
     * Attempts to convert the passed entity into a caster using all the known methods.
     */
    public static Optional<Caster<?>> toCaster(@Nullable Entity entity) {
        if (entity instanceof Caster<?>) {
            return Optional.of((Caster<?>)entity);
        }

        if (entity instanceof LivingEntity && !(entity instanceof Magical)) {
            return PonyContainer.of(entity).map(PonyContainer::getCaster);
        }

        return Optional.empty();
    }
}
