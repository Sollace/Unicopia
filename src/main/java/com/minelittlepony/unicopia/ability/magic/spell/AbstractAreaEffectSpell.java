package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

public abstract class AbstractAreaEffectSpell extends AbstractSpell {
    protected static final SpellAttribute<Float> RANGE = SpellAttribute.create(SpellAttributes.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> Math.max(0, 4 + power));
    public static final TooltipFactory TOOLTIP = RANGE;

    protected AbstractAreaEffectSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }
}
