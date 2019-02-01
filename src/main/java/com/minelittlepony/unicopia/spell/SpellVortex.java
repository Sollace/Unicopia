package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
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
    protected void spawnParticles(ICaster<?> source, int strength) {
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, strength), strength * 9, p -> {
            Particles.instance().spawnParticle(UParticles.MAGIC_PARTICLE, false, p, p.subtract(pos));
        });
    }

    @Override
    protected void applyRadialEffect(ICaster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        double force = 4 / distance;

        if (source.getAffinity() != SpellAffinity.BAD && target instanceof EntityPlayer) {
            force *= calculateAdjustedForce(PlayerSpeciesList.instance().getPlayer((EntityPlayer)target));
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
