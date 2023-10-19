package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class DispellEvilSpell extends AbstractSpell implements ProjectileDelegate.HitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.POWER, 1)
            .build();

    protected DispellEvilSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return !isDead();
        }

        source.findAllEntitiesInRange(getTraits().get(Trait.POWER) * 10, e -> e.getType() == EntityType.PHANTOM).forEach(entity -> {
            entity.damage(entity.getDamageSources().magic(), 50);
            if (entity instanceof LivingEntity l) {
                double d = source.getOriginVector().getX() - entity.getX();
                double e = source.getOriginVector().getZ() - entity.getZ();
                while (d * d + e * e < 1.0E-4) {
                    d = (Math.random() - Math.random()) * 0.01;
                    e = (Math.random() - Math.random()) * 0.01;
                }
                l.takeKnockback(1, d, e);
            }

            source.addParticle(LightningBoltParticleEffect.DEFAULT, entity.getPos(), Vec3d.ZERO);
        });
        source.subtractEnergyCost(1000);

        return false;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        tick(projectile, Situation.GROUND);
    }
}
