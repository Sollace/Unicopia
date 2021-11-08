package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * A spell for revealing changelings.
 */
public class RevealingSpell extends AbstractSpell {
    private static final Shape AREA = new Sphere(false, 15);

    protected RevealingSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            MagicParticleEffect effect = new MagicParticleEffect(getType().getColor());

            source.spawnParticles(AREA, 5, pos -> {
                source.addParticle(effect, pos, Vec3d.ZERO);
            });
            source.spawnParticles(effect, 5);
        }

        source.findAllSpellsInRange(15).forEach(e -> {
            e.getSpellSlot().get(SpellPredicate.CAN_SUPPRESS, false)
                .filter(spell -> spell.isVulnerable(source, this))
                .ifPresent(spell -> {
                spell.onSuppressed(source);
                source.getWorld().playSound(null, e.getOrigin(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.2F, 0.5F);
            });
        });

        return true;
    }
}