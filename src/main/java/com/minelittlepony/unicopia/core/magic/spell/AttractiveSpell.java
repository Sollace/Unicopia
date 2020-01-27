package com.minelittlepony.unicopia.core.magic.spell;

import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.util.MagicalDamageSource;
import com.minelittlepony.unicopia.core.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell {

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
            source.addParticle(UParticles.UNICORN_MAGIC, p, p.subtract(pos)); // getTint()
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

        double x = MathHelper.clamp(vel.x, -maxVel, maxVel);
        double y = MathHelper.clamp(vel.y, -maxVel, maxVel);
        double z = MathHelper.clamp(vel.z, -maxVel, maxVel);

        if (distance < 0.5) {
            z += maxVel * 2;
        }

        target.setVelocity(x, y, z);
    }
}
