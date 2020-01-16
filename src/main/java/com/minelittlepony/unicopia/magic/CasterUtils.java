package com.minelittlepony.unicopia.magic;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.ducks.IRaceContainerHolder;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CasterUtils {

    /**
     * Finds all surrounding spells withing range from the given caster.
     */
    public static Stream<ICaster<?>> findAllSpellsInRange(ICaster<?> source, double radius) {

        BlockPos origin = source.getOrigin();

        BlockPos begin = origin.add(-radius, -radius, -radius);
        BlockPos end = origin.add(radius, radius, radius);

        Box bb = new Box(begin, end);

        return source.getWorld().getEntities(source.getEntity(), bb, e ->
            !e.removed && (e instanceof ICaster || e instanceof PlayerEntity)
        ).stream().filter(e -> {
                double dist = e.squaredDistanceTo(origin.getX(), origin.getY(), origin.getZ());
                double dist2 = e.squaredDistanceTo(origin.getX(), origin.getY() - e.getStandingEyeHeight(), origin.getZ());

                return dist <= radius || dist2 <= radius;
            })
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    /**
     * Finds all magically capabable entities in the world.
     */
    @Deprecated
    static Stream<ICaster<?>> findAllSpells(ICaster<?> source) {
        // TODO:
        return source.getWorld().getEntities(LivingEntity.class, new Box(0, 0, 0, 0, 0, 0), e -> {
            return e instanceof ICaster || e instanceof PlayerEntity;
        }).stream()
                .map(CasterUtils::toCaster)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    static Stream<ICaster<?>> findAllSpellsInRange(ICaster<?> source, Box bb) {
        return source.getWorld().getEntities(source.getEntity(), bb, e -> !e.removed && (e instanceof ICaster || Predicates.MAGI.test(e))).stream()
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    static <T extends IMagicEffect> Optional<T> toMagicEffect(Class<T> type, @Nullable Entity entity) {
        return toCaster(entity)
                .filter(ICaster::hasEffect)
                .map(caster -> caster.getEffect(type, false))
                .filter(e -> !e.getDead());
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
    public static Optional<ICaster<?>> toCaster(@Nullable Entity entity) {
        if (entity instanceof ICaster<?>) {
            return Optional.of((ICaster<?>)entity);
        }

        if (entity instanceof LivingEntity && !(entity instanceof IMagicals)) {
            return SpeciesList.instance().getForEntity(entity)
                    .map(IRaceContainerHolder::getCaster);
        }

        return Optional.empty();
    }
}
