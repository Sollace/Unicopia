package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

public class CustomisedSpellType<T extends Spell> implements SpellPredicate<T> {
    private final SpellType<T> type;
    private final SpellTraits traits;

    public CustomisedSpellType(SpellType<T> type, SpellTraits traits) {
        this.type = type;
        this.traits = traits;
    }

    public boolean isEmpty() {
        return type.isEmpty();
    }

    public T create() {
        return type.create(traits);
    }

    @Nullable
    public T apply(Caster<?> caster) {
        return type.apply(caster, traits);
    }

    @Override
    public boolean test(Spell spell) {
        return type.test(spell);
    }
}
