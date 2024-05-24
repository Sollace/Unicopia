package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * An area-effect spell that disperses illusions.
 */
public class DisperseIllusionSpell extends AbstractAreaEffectSpell {
    public static void appendTooltip(CustomisedSpellType<? extends DisperseIllusionSpell> type, List<Text> tooltip) {
        tooltip.add(SpellAttributes.of(SpellAttributes.RANGE, 15 + type.traits().get(Trait.POWER)));
        tooltip.add(SpellAttributes.ofTime(SpellAttributes.DURATION, (1 + (long)type.traits().get(Trait.STRENGTH)) * 100));
    }

    protected DisperseIllusionSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        float range = Math.max(0, 15 + getAdditionalRange());

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
                spell.onSuppressed(source, 100 * (1 + getTraits().get(Trait.STRENGTH)));
                e.playSound(USounds.SPELL_ILLUSION_DISPERSE, 0.2F, 0.5F);
            });
        });

        return true;
    }
}