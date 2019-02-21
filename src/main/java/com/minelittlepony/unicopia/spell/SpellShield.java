package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.particle.ParticleConnection;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.ProjectileUtil;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class SpellShield extends AbstractSpell.RangedAreaSpell implements IAttachedEffect {

    private final ParticleConnection particlEffect = new ParticleConnection();

    @Override
    public String getName() {
        return "shield";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public int getTint() {
        return 0x66CDAA;
    }

    @Override
    public void render(ICaster<?> source) {
        float radius = 4 + (source.getCurrentLevel() * 2);

        source.spawnParticles(new Sphere(true, radius), (int)(radius * 6), pos -> {
            Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0, getTint());
        });

        particlEffect
            .ifMissing(source, () -> Particles.instance().spawnParticle(UParticles.SPHERE, true, source.getOriginVector(), 0, 0, 0, 1, getTint(), 10))
            .ifPresent(p -> p.setAttribute(0, radius));
    }

    @Override
    public boolean updateOnPerson(IPlayer source) {
        if (update(source)) {
            if (source.getEntity().getEntityWorld().getWorldTime() % 50 == 0) {
                double radius = 4 + (source.getCurrentLevel() * 2);
                if (!IPower.takeFromPlayer((EntityPlayer)source.getOwner(), radius/4)) {
                    setDead();
                }
            }
        }

        return !getDead();
    }

    public double getDrawDropOffRange(ICaster<?> source) {
        return 4 + (source.getCurrentLevel() * 2);
    }

    @Override
    public boolean update(ICaster<?> source) {
        double radius = getDrawDropOffRange(source);

        Entity owner = source.getOwner();

        boolean ownerIsValid = source.getAffinity() != SpellAffinity.BAD && Predicates.MAGI.test(owner);

        Vec3d origin = source.getOriginVector();

        source.findAllEntitiesInRange(radius)
            .filter(entity -> !(ownerIsValid && entity.equals(owner)))
            .forEach(i -> {
                try {
                    double dist = i.getPositionVector().distanceTo(origin);

                    applyRadialEffect(source, i, dist, radius);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });

        return true;
    }

    protected void applyRadialEffect(ICaster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        if (ProjectileUtil.isProjectile(target)) {
            if (!ProjectileUtil.isProjectileThrownBy(target, source.getOwner())) {
                if (distance < radius/2) {
                    target.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1F, 1);
                    target.setDead();
                } else {
                    ricochet(target, pos);
                }
            }
        } else if (target instanceof EntityLivingBase) {
            double force = Math.max(0.1, radius / 4);

            if (source.getAffinity() != SpellAffinity.BAD && target instanceof EntityPlayer) {
                force *= calculateAdjustedForce(PlayerSpeciesList.instance().getPlayer((EntityPlayer)target));
            } else {
                force *= 0.75;
            }

            applyForce(pos, target, force, distance);
        }
    }

    /**
     * Applies a force to the given entity based on distance from the source.
     */
    protected void applyForce(Vec3d pos, Entity target, double force, double distance) {
        pos = target.getPositionVector().subtract(pos).normalize().scale(force);

        target.addVelocity(
                pos.x,
                pos.y + (distance < 1 ? distance : 0),
                pos.z
        );
    }

    /**
     * Returns a force to apply based on the given player's given race.
     */
    protected double calculateAdjustedForce(IPlayer player) {
        double force = 0.75;

        if (player.getPlayerSpecies().canUseEarth()) {
            force /= 2;

            if (player.getOwner().isSneaking()) {
                force /= 6;
            }
        } else if (player.getPlayerSpecies().canFly()) {
            force *= 2;
        }

        return force;
    }

    /**
     * Reverses a projectiles direction to deflect it off the shield's surface.
     */
    protected void ricochet(Entity projectile, Vec3d pos) {
        Vec3d position = projectile.getPositionVector();
        Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);

        Vec3d normal = position.subtract(pos).normalize();
        Vec3d approach = motion.subtract(normal);

        if (approach.length() >= motion.length()) {
            ProjectileUtil.setThrowableHeading(projectile, normal, (float)motion.length(), 0);
        }
    }
}
