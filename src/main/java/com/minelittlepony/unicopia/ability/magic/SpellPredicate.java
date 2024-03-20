package com.minelittlepony.unicopia.ability.magic;

import java.util.UUID;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.effect.MimicSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.ShieldSpell;

import net.minecraft.entity.Entity;

public interface SpellPredicate<T extends Spell> extends Predicate<Spell> {
    SpellPredicate<IllusionarySpell> CAN_SUPPRESS = s -> s instanceof IllusionarySpell;
    SpellPredicate<PlaceableSpell> IS_PLACED = s -> s instanceof PlaceableSpell;
    SpellPredicate<AbstractDisguiseSpell> IS_DISGUISE = s -> s instanceof AbstractDisguiseSpell;
    SpellPredicate<MimicSpell> IS_MIMIC = s -> s instanceof MimicSpell;
    SpellPredicate<ShieldSpell> IS_SHIELD_LIKE = spell -> spell instanceof ShieldSpell;
    SpellPredicate<TimedSpell> IS_TIMED = spell -> spell instanceof TimedSpell;
    SpellPredicate<OrientedSpell> IS_ORIENTED = spell -> spell instanceof OrientedSpell;

    SpellPredicate<?> IS_NOT_PLACED = IS_PLACED.negate();
    SpellPredicate<?> IS_VISIBLE = spell -> spell != null && !spell.isHidden();

    SpellPredicate<?> IS_CORRUPTING = spell -> spell.getAffinity() == Affinity.BAD;

    default <Q extends Spell> SpellPredicate<Q> and(SpellPredicate<Q> predicate) {
        SpellPredicate<T> self = this;
        return s -> self.test(s) && predicate.test(s);
    }

    default <Q extends Spell> SpellPredicate<? extends Spell> or(SpellPredicate<Q> predicate) {
        SpellPredicate<T> self = this;
        return s -> self.test(s) || predicate.test(s);
    }

    @Override
    default SpellPredicate<?> negate() {
        SpellPredicate<T> self = this;
        return s -> !self.test(s);
    }

    default boolean isOn(Caster<?> caster) {
        return caster.getSpellSlot().contains(this);
    }

    default boolean isOn(Entity entity) {
        return Caster.of(entity).filter(this::isOn).isPresent();
    }

    default SpellPredicate<T> withId(UUID uuid) {
        return and(spell -> spell.getUuid().equals(uuid));
    }
}