package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell implements ProjectileSpell {
    protected AttractiveSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        double range = getDrawDropOffRange(source) + 10;

        source.spawnParticles(getOrigin(source), new Sphere(false, range), 7, p -> {
            source.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, source.getEntity(), 0.4F)
                        .withChild(new MagicParticleEffect(getType().getColor())),
                    p,
                    Vec3d.ZERO
            );
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return 10 + (caster.getLevel().get() * 2);
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return getTraits().get(Trait.FOCUS) > 10 ? entity instanceof ItemEntity : super.isValidTarget(source, entity);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        double force = 2.5F * distance;

        boolean isGood = isFriendlyTogether(source);

        if (isGood) {
            force *= AttractionUtils.getForceAdjustment(target);
        }

        if (!isGood && source.getWorld().random.nextInt(4500) == 0) {
            source.getEntity().damage(MagicalDamageSource.create("vortex"), 4);
        }

        AttractionUtils.applyForce(getOrigin(source), target, -force, 0, false);

        float maxVel = !isFriendlyTogether(source) ? 1 : 1.6f;

        Vec3d vel = target.getVelocity();

        double x = MathHelper.clamp(vel.x, -maxVel, maxVel);
        double y = MathHelper.clamp(vel.y, -maxVel, maxVel);
        double z = MathHelper.clamp(vel.z, -maxVel, maxVel);

        if (distance < 0.5) {
            z += maxVel * 2;
        }

        target.setVelocity(x, y, z);
    }
}
