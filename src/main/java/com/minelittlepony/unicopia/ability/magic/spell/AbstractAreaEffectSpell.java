package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractAreaEffectSpell extends AbstractSpell {

    public static final int MIN_RANGE = 30;
    public static final int MID_RANGE = 15;
    public static final int MAX_RANGE = 33;

    protected static final SpellAttribute<Float> RANGE = range(4);
    public static final TooltipFactory TOOLTIP = RANGE;

    public static SpellAttribute<Float> range(int base) {
        return SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> MathHelper.clamp(base + power, MIN_RANGE, MAX_RANGE));
    }

    public static Stream<BlockPos> randomBlockPositions(Caster<?> source, boolean hollow, float radius) {
        var shape = new Sphere(hollow, radius).translate(source.getOrigin());
        if (radius > MID_RANGE) {
            // limit updates to 15 blocks per tick if the range is larger than 15 blocks.
            return shape.randomBlockPositions(source.asWorld().getRandom()).limit(15);
        }
        return shape.getBlockPositions();
    }

    protected AbstractAreaEffectSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }
}
