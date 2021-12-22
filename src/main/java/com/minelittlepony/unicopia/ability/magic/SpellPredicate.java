package com.minelittlepony.unicopia.ability.magic;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface SpellPredicate<T extends Spell> extends Predicate<Spell> {
    SpellPredicate<Suppressable> CAN_SUPPRESS = s -> s instanceof Suppressable;

    SpellPredicate<ProjectileSpell> HAS_PROJECTILE_EVENTS = s -> s instanceof ProjectileSpell;

    SpellPredicate<AbstractDisguiseSpell> IS_DISGUISE = s -> s instanceof AbstractDisguiseSpell;

    default boolean isOn(Caster<?> caster) {
        return caster.getSpellSlot().get(this, false).isPresent();
    }
}