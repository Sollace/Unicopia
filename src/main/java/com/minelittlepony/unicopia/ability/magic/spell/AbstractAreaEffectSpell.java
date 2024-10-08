package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import net.minecraft.util.math.MathHelper;

public abstract class AbstractAreaEffectSpell extends AbstractSpell {
    protected static final SpellAttribute<Float> RANGE = SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> MathHelper.clamp(4 + power, 0, 32));
    public static final TooltipFactory TOOLTIP = RANGE;

    protected AbstractAreaEffectSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }
}
