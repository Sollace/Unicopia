package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.ability.magic.spell.trait.ItemWithTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import net.minecraft.item.Item;

public class TraitItem extends Item implements ItemWithTraits {
    private final SpellTraits traits;

    public TraitItem(Trait trait, Settings settings) {
        super(settings);
        this.traits = new SpellTraits.Builder().with(trait, 1).build();
    }

    @Override
    public SpellTraits getDefaultTraits() {
        return traits;
    }
}
