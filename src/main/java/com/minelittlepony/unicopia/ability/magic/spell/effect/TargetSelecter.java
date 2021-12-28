package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
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

        Entity owner = source.getMaster();

        boolean ownerIsValid = spell.isFriendlyTogether(source) && (EquinePredicates.PLAYER_UNICORN.test(owner));

        return source.findAllEntitiesInRange(radius)
            .filter(entity -> !entity.isRemoved())
            .filter(entity -> {
                boolean hasShield = SpellPredicate.IS_SHIELD_LIKE.isOn(entity);
                boolean isOwnerOrFriend = Pony.equal(entity, owner) || owner.isConnectedThroughVehicle(entity) || FriendshipBraceletItem.isComrade(source, entity);

                if (!ownerIsValid && isOwnerOrFriend) {
                    return true;
                }

                return !hasShield && (!ownerIsValid || !isOwnerOrFriend);
            })
            .filter(e -> filter.test(source, e))
            .map(i -> {
                targets.computeIfAbsent(i.getUuid(), Target::new);
                return i;
            });
    }

    public long getTotalDamaged() {
        return targets.values().stream().filter(Target::canHurt).count();
    }

    public static Predicate<Entity> notOwnerOrFriend(Spell spell, Caster<?> source) {
        Entity owner = source.getMaster();

        boolean ownerIsValid = spell.isFriendlyTogether(source) && (EquinePredicates.PLAYER_UNICORN.test(owner));

        if (!ownerIsValid) {
            return e -> true;
        }

        return entity -> {
            return !ownerIsValid || !(Pony.equal(entity, owner) || owner.isConnectedThroughVehicle(entity) || FriendshipBraceletItem.isComrade(source, entity));
        };
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
