package com.minelittlepony.unicopia.spell;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CasterUtils {

    public static Stream<ICaster<?>> findAllSpellsInRange(ICaster<?> source, double radius) {

        BlockPos origin = source.getOrigin();

        BlockPos begin = origin.add(-radius, -radius, -radius);
        BlockPos end = origin.add(radius, radius, radius);

        AxisAlignedBB bb = new AxisAlignedBB(begin, end);

        return source.getWorld().getEntitiesInAABBexcluding(source.getEntity(), bb, e ->
            !e.isDead && (e instanceof ICaster || e instanceof EntityPlayer)
        ).stream().filter(e -> {
                double dist = e.getDistance(origin.getX(), origin.getY(), origin.getZ());
                double dist2 = e.getDistance(origin.getX(), origin.getY() - e.getEyeHeight(), origin.getZ());

                return dist <= radius || dist2 <= radius;
            })
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    static Stream<ICaster<?>> findAllSpellsInRange(ICaster<?> source, AxisAlignedBB bb) {
        return source.getWorld().getEntitiesInAABBexcluding(source.getEntity(), bb, e ->
            !e.isDead && (e instanceof ICaster || Predicates.MAGI.test(e))
        ).stream()
            .map(CasterUtils::toCaster)
            .filter(o -> o.isPresent() && o.get() != source)
            .map(Optional::get);
    }

    @SuppressWarnings("unchecked")
    static <T extends IMagicEffect> Optional<T> toMagicEffect(Class<T> type, @Nullable Entity entity) {
        return toCaster(entity)
                .filter(ICaster::hasEffect)
                .map(caster -> caster.getEffect(type, false))
                .filter(e -> !e.getDead());
    }

    public static boolean isHoldingEffect(String effectName, Entity entity) {
        return Streams.stream(entity.getEquipmentAndArmor())
            .map(SpellRegistry::getKeyFromStack)
            .anyMatch(s -> s.equals(effectName));
    }

    public static Optional<ICaster<?>> toCaster(@Nullable Entity entity) {

        if (entity instanceof ICaster<?>) {
            return Optional.of((ICaster<?>)entity);
        }

        if (entity instanceof EntityPlayer) {
            return Optional.of(PlayerSpeciesList.instance().getPlayer((EntityPlayer)entity));
        }

        return Optional.empty();
    }
}
