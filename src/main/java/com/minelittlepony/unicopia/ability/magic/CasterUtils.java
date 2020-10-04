package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class CasterUtils {
    /**
     * Finds all surrounding spells within range from the given caster.
     */
    static Stream<Caster<?>> findInRange(Caster<?> source, double radius) {
        return VecHelper.findInRange(source.getEntity(), source.getWorld(), source.getOrigin(), radius, EquinePredicates.IS_CASTER)
                .stream()
                .map(e -> toCaster(e).filter(o -> o != source))
                .filter(Optional::isPresent)
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
