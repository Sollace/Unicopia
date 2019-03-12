package com.minelittlepony.unicopia.spell;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CasterUtils {

    /**
     * Finds all surrounding spells withing range from the given caster.
     */
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

    /**
     * Finds all magically capabable entities in the world.
     */
    static Stream<ICaster<?>> findAllSpells(ICaster<?> source) {
        return source.getWorld().getEntities(EntityLivingBase.class, e -> {
            return e instanceof ICaster || e instanceof EntityPlayer;
        }).stream()
                .map(CasterUtils::toCaster)
                .filter(Optional::isPresent)
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

    /**
     * Determines if the passed in entity is holding the named effect.
     * By holding that meant the effect must be attached to the caster associated with the entity.
     */
    public static boolean isHoldingEffect(String effectName, Entity entity) {
        return Streams.stream(entity.getEquipmentAndArmor())
            .map(SpellRegistry::getKeyFromStack)
            .anyMatch(s -> s.equals(effectName));
    }

    /**
     * Creates a new caster at the position of the given entity.
     * First attempts to convert the passed entity into a caster.
     */
    @Nonnull
    public static ICaster<?> near(@Nonnull Entity entity) {
        ICaster<?> result = toCasterRaw(entity);

        if (result != null) {
            return result;
        }

        EntitySpell caster = new EntitySpell(entity.world);
        caster.copyLocationAndAnglesFrom(entity);

        return caster;
    }

    /**
     * Attempts to convert the passed entity into a caster using all the known methods.
     */
    public static Optional<ICaster<?>> toCaster(@Nullable Entity entity) {
        return Optional.ofNullable(toCasterRaw(entity));
    }

    private static ICaster<?> toCasterRaw(Entity entity) {
        if (entity instanceof ICaster<?>) {
            return (ICaster<?>)entity;
        }

        if (entity instanceof EntityLivingBase && !(entity instanceof IMagicals)) {
            return PlayerSpeciesList.instance().getCaster((EntityLivingBase)entity);
        }

        return null;
    }
}
