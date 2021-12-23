package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 50)
            .with(Trait.AIR, 9)
            .build();

    protected AttractiveSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        int range = 4 + (source.getLevel().get() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            source.addParticle(new MagicParticleEffect(getType().getColor()), p, p.subtract(pos));
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return 10 + (caster.getLevel().get() * 2);
    }

    @Override
    protected List<Entity> getTargets(Caster<?> source, double radius) {

        if (getTraits().get(Trait.FOCUS) > 10) {
            return VecHelper.findInRange(source.getEntity(), source.getWorld(), source.getOriginVector(), radius, i -> i instanceof ItemEntity);
        }

        return super.getTargets(source, radius);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        double force = 2.5F * distance;

        boolean isGood = isFriendlyTogether(source);

        if (isGood && target instanceof PlayerEntity) {
            force *= calculateAdjustedForce(Pony.of((PlayerEntity)target));
        }

        if (!isGood && source.getWorld().random.nextInt(4500) == 0) {
            source.getEntity().damage(MagicalDamageSource.create("vortex"), 4);
        }

        applyForce(source.getOriginVector(), target, -force, 0);

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
