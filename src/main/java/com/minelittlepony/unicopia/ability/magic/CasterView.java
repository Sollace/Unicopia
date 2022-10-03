package com.minelittlepony.unicopia.ability.magic;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;

public interface CasterView {
    EntityView getWorld();

    default <S extends Spell> Stream<Map.Entry<Caster<?>, S>> findAllSpellsInRange(BlockPos pos, double radius, SpellPredicate<S> type) {
        return findAllCastersInRange(pos, radius).flatMap(caster -> {
            return caster.getSpellSlot().stream(type, false).map(spell -> {
                return Map.entry(caster, spell);
            });
        });
    }

    default Stream<Caster<?>> findAllCastersInRange(BlockPos pos, double radius) {
        return findAllCastersInRange(pos, radius, null);
    }

    default Stream<Caster<?>> findAllCastersInRange(BlockPos pos, double radius, @Nullable Predicate<Entity> test) {
        return Caster.stream(findAllEntitiesInRange(pos, radius, test == null ? EquinePredicates.IS_CASTER : EquinePredicates.IS_CASTER.and(test)));
    }

    default Stream<Entity> findAllEntitiesInRange(BlockPos pos, double radius, @Nullable Predicate<Entity> test) {
        return VecHelper.findInRange(null, getWorld(), Vec3d.ofCenter(pos), radius, test).stream();
    }

    default Stream<Entity> findAllEntitiesInRange(BlockPos pos, double radius) {
        return findAllEntitiesInRange(pos, radius, null);
    }
}
