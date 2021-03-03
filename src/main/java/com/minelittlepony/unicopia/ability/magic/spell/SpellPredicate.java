package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.Suppressable;
import com.minelittlepony.unicopia.ability.magic.Thrown;

public interface SpellPredicate<T> extends Predicate<Spell> {
    SpellPredicate<Thrown> IS_THROWN = s -> s instanceof Thrown;
    SpellPredicate<Attached> IS_ATTACHED = s -> s instanceof Attached;
    SpellPredicate<Suppressable> IS_SUPPRESSABLE = s -> s instanceof Suppressable;
}