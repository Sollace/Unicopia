package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;

public class TargetSelecter {
    private final Map<UUID, Target> targets = new TreeMap<>();

    private final Spell spell;

    private BiPredicate<Caster<?>, Entity> filter = (a, b) -> true;

    private boolean targetOwner;
    private boolean targetAllies;

    public TargetSelecter(Spell spell) {
        this.spell = spell;
    }

    public TargetSelecter setFilter(BiPredicate<Caster<?>, Entity> filter) {
        this.filter = filter;
        return this;
    }

    public TargetSelecter setTargetowner(boolean targetOwner) {
        this.targetOwner = targetOwner;
        return this;
    }

    public TargetSelecter setTargetAllies(boolean targetAllies) {
        this.targetAllies = targetAllies;
        return this;
    }

    public Stream<Entity> getEntities(Caster<?> source, double radius) {
        targets.values().removeIf(Target::tick);
        return source.findAllEntitiesInRange(radius)
            .filter(EntityPredicates.VALID_ENTITY)
            .filter(EquinePredicates.EXCEPT_MAGIC_IMMUNE)
            .filter(entity -> entity != source.asEntity() && checkAlliegance(spell, source, entity) && filter.test(source, entity))
            .map(i -> {
                targets.computeIfAbsent(i.getUuid(), Target::new);
                return i;
            });
    }

    private boolean checkAlliegance(Affine affine, Caster<?> source, Entity target) {
        boolean isOwner = !targetOwner && source.isOwnerOrVehicle(target);
        boolean isFriend = !targetAllies && affine.applyInversion(source, source.isFriend(target));
        return !(isOwner || isFriend);
    }

    public long getTotalDamaged() {
        return targets.values().stream().filter(Target::canHurt).count();
    }

    public static <T extends Entity> Predicate<T> validTarget(Affine affine, Caster<?> source) {
        return target -> validTarget(affine, source, target);
    }

    public static boolean validTarget(Affine affine, Caster<?> source, Entity target) {
        return !isOwnerOrFriend(affine, source, target);
    }

    public static boolean isOwnerOrFriend(Affine affine, Caster<?> source, Entity target) {
        return affine.applyInversion(source, source.isOwnerOrFriend(target));
    }

    private static final class Target {
        private int cooldown = 20;

        Target(UUID id) { }

        boolean tick() {
            return --cooldown < 0;
        }

        boolean canHurt() {
            return cooldown == 20;
        }
    }
}
