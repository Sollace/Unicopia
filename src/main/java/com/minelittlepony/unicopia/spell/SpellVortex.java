package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class SpellVortex extends SpellShield {

    @Override
    public String getName() {
        return "vortex";
    }

    @Override
    public int getTint() {
        return 0x4CDEE7;
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public void render(ICaster<?> source) {
        int range = 4 + (source.getCurrentLevel() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false, p, p.subtract(pos));
        });
    }

    @Override
    public double getDrawDropOffRange(ICaster<?> caster) {
        return 10 + (caster.getCurrentLevel() * 2);
    }

    @Override
    protected void applyRadialEffect(ICaster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        double force = 2.5F / distance;

        if (source.getAffinity() != SpellAffinity.BAD && target instanceof EntityPlayer) {
            force *= calculateAdjustedForce(PlayerSpeciesList.instance().getPlayer((EntityPlayer)target));
        }

        if (source.getAffinity() == SpellAffinity.BAD && source.getWorld().rand.nextInt(4500) == 0) {
            source.getEntity().attackEntityFrom(MagicalDamageSource.create("vortex"), 4);
        }

        applyForce(pos, target, -force, 0);

        float maxVel = source.getAffinity() == SpellAffinity.BAD ? 1 : 1.6f;

        if (target.motionX > maxVel) target.motionX = maxVel;
        if (target.motionX < -maxVel) target.motionX = -maxVel;
        if (target.motionY > maxVel) target.motionY = maxVel;
        if (target.motionY < -maxVel) target.motionY = -maxVel;
        if (target.motionZ > maxVel) target.motionZ = maxVel;
        if (target.motionZ < -maxVel) target.motionZ = -maxVel;

        if (distance < 0.5) {
            target.motionZ += maxVel * 2;
        }
    }
}
