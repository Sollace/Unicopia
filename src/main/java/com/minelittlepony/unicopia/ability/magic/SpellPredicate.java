package com.minelittlepony.unicopia.ability.magic;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.spell.ProjectileCapable;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface SpellPredicate<T extends Spell> extends Predicate<Spell> {
    SpellPredicate<ProjectileCapable> IS_THROWN = s -> s instanceof ProjectileCapable;
    SpellPredicate<Suppressable> IS_SUPPRESSABLE = s -> s instanceof Suppressable;

    default boolean isOn(Caster<?> caster) {
        return caster.getSpellSlot().get(this, false).isPresent();
    }
}