package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.util.math.Vec3d;

/**
 * An area-effect spell that disperses illusions.
 */
public class DisperseIllusionSpell extends AbstractAreaEffectSpell {
    private static final SpellAttribute<Float> RANGE = SpellAttribute.create(SpellAttributes.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> Math.max(0, 15 + power));
    private static final SpellAttribute<Long> DURATION = SpellAttribute.create(SpellAttributes.DURATION, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.STRENGTH, strength -> (1 + (long)strength) * 100);
    static final TooltipFactory TOOLTIP = TooltipFactory.of(RANGE, DURATION);

    protected DisperseIllusionSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        float range = RANGE.get(getTraits());

        if (range == 0) {
            return false;
        }

        if (source.isClient()) {
            MagicParticleEffect effect = new MagicParticleEffect(getType().getColor());

            source.spawnParticles(new Sphere(false, range), 5, pos -> {
                source.addParticle(effect, pos, Vec3d.ZERO);
            });
            source.spawnParticles(effect, 5);
        }

        source.findAllSpellsInRange(range).forEach(e -> {
            e.getSpellSlot().get(SpellPredicate.CAN_SUPPRESS)
                .filter(spell -> spell.isVulnerable(source, this))
                .ifPresent(spell -> {
                spell.onSuppressed(source, DURATION.get(getTraits()));
                e.playSound(USounds.SPELL_ILLUSION_DISPERSE, 0.2F, 0.5F);
            });
        });

        return true;
    }
}