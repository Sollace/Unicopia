package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface AllSpells extends SpellPredicate<Spell> {
    AllSpells INSTANCE = spell -> true;
}
