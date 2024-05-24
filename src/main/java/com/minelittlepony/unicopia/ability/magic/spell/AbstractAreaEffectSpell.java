package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import net.minecraft.text.Text;

public abstract class AbstractAreaEffectSpell extends AbstractSpell {

    public static void appendRangeTooltip(CustomisedSpellType<?> type, List<Text> tooltip) {
        float addedRange = type.traits().get(Trait.POWER);
        if (addedRange != 0) {
            tooltip.add(SpellAttributes.ofRelative(SpellAttributes.RANGE, addedRange));
        }
    }

    protected AbstractAreaEffectSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }

    protected final float getAdditionalRange() {
        return (int)getTraits().get(Trait.POWER);
    }
}
