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
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
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

        Predicate<Entity> ownerCheck = isOwnerOrFriend(spell, source);

        return source.findAllEntitiesInRange(radius)
            .filter(entity -> entity.isAlive() && !entity.isRemoved() && !ownerCheck.test(entity) && !SpellPredicate.IS_SHIELD_LIKE.isOn(entity))
            .filter(entity -> !(entity instanceof SpellbookEntity))
            .filter(e -> filter.test(source, e))
            .map(i -> {
                targets.computeIfAbsent(i.getUuid(), Target::new);
                return i;
            });
    }

    public long getTotalDamaged() {
        return targets.values().stream().filter(Target::canHurt).count();
    }

    public static <T extends Entity> Predicate<T> notOwnerOrFriend(Affine spell, Caster<?> source) {
        return TargetSelecter.<T>isOwnerOrFriend(spell, source).negate();
    }

    public static <T extends Entity> Predicate<T> isOwnerOrFriend(Affine spell, Caster<?> source) {
        Entity owner = source.getMaster();

        if (!(spell.isFriendlyTogether(source) && EquinePredicates.PLAYER_UNICORN.test(owner))) {
            return e -> FriendshipBraceletItem.isComrade(source, e);
        }

        return entity -> {
            return FriendshipBraceletItem.isComrade(source, entity) || (owner != null && (Pony.equal(entity, owner) || owner.isConnectedThroughVehicle(entity)));
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
