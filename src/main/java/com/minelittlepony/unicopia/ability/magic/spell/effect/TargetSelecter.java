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
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import net.minecraft.entity.Entity;

public class TargetSelecter {

    private final Map<UUID, Target> targets = new TreeMap<>();

    private final Spell spell;

    public TargetSelecter(Spell spell) {
        this.spell = spell;
    }

    public Stream<Entity> getEntities(Caster<?> source, double radius, BiPredicate<Caster<?>, Entity> filter) {
        targets.values().removeIf(Target::tick);
        return source.findAllEntitiesInRange(radius)
            .filter(entity -> entity.isAlive() && !entity.isRemoved() && notOwnerOrFriend(spell, source, entity))
            .filter(EquinePredicates.EXCEPT_MAGIC_IMMUNE)
            .filter(e -> filter.test(source, e))
            .map(i -> {
                targets.computeIfAbsent(i.getUuid(), Target::new);
                return i;
            });
    }

    public long getTotalDamaged() {
        return targets.values().stream().filter(Target::canHurt).count();
    }

    public static <T extends Entity> Predicate<T> notOwnerOrFriend(Affine affine, Caster<?> source) {
        return target -> notOwnerOrFriend(affine, source, target);
    }

    public static <T extends Entity> Predicate<T> isOwnerOrFriend(Affine affine, Caster<?> source) {
        return target -> isOwnerOrFriend(affine, source, target);
    }

    public static <T extends Entity> boolean notOwnerOrFriend(Affine affine, Caster<?> source, Entity target) {
        return !isOwnerOrFriend(affine, source, target);
    }

    public static <T extends Entity> boolean isOwnerOrFriend(Affine affine, Caster<?> source, Entity target) {
        Entity owner = source.getMaster();

        var equine = Pony.of(target);
        if (equine.isPresent() && !affine.isFriendlyTogether(equine.get())) {
            return false;
        }

        if (affine.isEnemy(source)) {
            return FriendshipBraceletItem.isComrade(source, target);
        }

        return FriendshipBraceletItem.isComrade(source, target)
            || (owner != null && (Pony.equal(target, owner) || owner.isConnectedThroughVehicle(target)));
    }

    static final class Target {
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
