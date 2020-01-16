package com.minelittlepony.unicopia.magic.spells;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public void render(ICaster<?> source) {
        int range = 4 + (source.getCurrentLevel() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            ParticleTypeRegistry.getTnstance().spawnParticle(UParticles.UNICORN_MAGIC, false, p, p.subtract(pos), getTint());
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

        if (source.getAffinity() != Affinity.BAD && target instanceof PlayerEntity) {
            force *= calculateAdjustedForce(SpeciesList.instance().getPlayer((PlayerEntity)target));
        }

        if (source.getAffinity() == Affinity.BAD && source.getWorld().random.nextInt(4500) == 0) {
            source.getEntity().damage(MagicalDamageSource.create("vortex"), 4);
        }

        applyForce(pos, target, -force, 0);

        float maxVel = source.getAffinity() == Affinity.BAD ? 1 : 1.6f;

        Vec3d vel = target.getVelocity();

        if (vel.x > maxVel) vel.x = maxVel;
        if (vel.x < -maxVel) vel.x = -maxVel;
        if (vel.y > maxVel) vel.y = maxVel;
        if (vel.y < -maxVel) vel.y = -maxVel;
        if (vel.z > maxVel) vel.z = maxVel;
        if (vel.z < -maxVel) vel.z = -maxVel;

        if (distance < 0.5) {
            vel.z += maxVel * 2;
        }
    }
}
