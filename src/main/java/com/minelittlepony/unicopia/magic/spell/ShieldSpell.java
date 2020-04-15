package com.minelittlepony.unicopia.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.AbstractSpell;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.IAttachedEffect;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.util.particles.ParticleConnection;
import com.minelittlepony.unicopia.util.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractSpell.RangedAreaSpell implements IAttachedEffect {

    private final ParticleConnection particlEffect = new ParticleConnection();

    @Override
    public String getName() {
        return "shield";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public int getTint() {
        return 0x66CDAA;
    }

    @Override
    public void renderOnPerson(ICaster<?> source) {
        render(source);
    }

    @Override
    public void render(ICaster<?> source) {
        float radius = 4 + (source.getCurrentLevel() * 2);

        source.spawnParticles(new Sphere(true, radius), (int)(radius * 6), pos -> {
            source.addParticle(UParticles.UNICORN_MAGIC, pos, Vec3d.ZERO); // getTint()
        });

        particlEffect
            .ifMissing(source, () -> {
                source.addParticle(UParticles.SPHERE, source.getOriginVector(), Vec3d.ZERO);
                return null; // TODO: Attachables
            }) // 1, getTint(), 10
            .ifPresent(p -> p.setAttribute(0, radius));
    }

    @Override
    public boolean updateOnPerson(ICaster<?> source) {
        int costMultiplier = applyEntities(source);
        if (costMultiplier > 0) {
            if (source.getOwner().age % 20 == 0) {
                double cost = 4 + (source.getCurrentLevel() * 2);

                cost *= costMultiplier / 5F;
                System.out.println("Taking " + cost);

                if (!source.subtractEnergyCost(cost)) {
                    setDead();
                }
            }
        }

        return !isDead();
    }

    public double getDrawDropOffRange(ICaster<?> source) {
        return 4 + (source.getCurrentLevel() * 2);
    }

    @Override
    public boolean update(ICaster<?> source) {
        applyEntities(source);
        return true;
    }

    protected int applyEntities(ICaster<?> source) {
        double radius = getDrawDropOffRange(source);

        Entity owner = source.getOwner();

        boolean ownerIsValid = source.getAffinity() != Affinity.BAD && EquinePredicates.MAGI.test(owner);

        Vec3d origin = source.getOriginVector();

        List<Entity> targets = source.findAllEntitiesInRange(radius)
            .filter(entity -> !(ownerIsValid && entity.equals(owner)))
            .collect(Collectors.toList());

        targets.forEach(i -> {
            try {
                double dist = i.getPos().distanceTo(origin);

                applyRadialEffect(source, i, dist, radius);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        return targets.size();
    }

    protected void applyRadialEffect(ICaster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        if (ProjectileUtil.isProjectile(target)) {
            if (!ProjectileUtil.isProjectileThrownBy(target, source.getOwner())) {
                if (distance < 1) {
                    target.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1F, 1);
                    target.remove();
                } else {
                    ricochet(target, pos);
                }
            }
        } else if (target instanceof LivingEntity) {
            double force = Math.max(0.1, radius / 4);

            if (source.getAffinity() != Affinity.BAD && target instanceof PlayerEntity) {
                force *= calculateAdjustedForce(SpeciesList.instance().getPlayer((PlayerEntity)target));
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
        pos = target.getPos().subtract(pos).normalize().multiply(force);

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

        if (player.getSpecies().canUseEarth()) {
            force /= 2;

            if (player.getOwner().isSneaking()) {
                force /= 6;
            }
        } else if (player.getSpecies().canFly()) {
            force *= 2;
        }

        return force;
    }

    /**
     * Reverses a projectiles direction to deflect it off the shield's surface.
     */
    protected void ricochet(Entity projectile, Vec3d pos) {
        Vec3d position = projectile.getPos();
        Vec3d motion = projectile.getVelocity();

        Vec3d normal = position.subtract(pos).normalize();
        Vec3d approach = motion.subtract(normal);

        if (approach.length() >= motion.length()) {
            ProjectileUtil.setThrowableHeading(projectile, normal, (float)motion.length(), 0);
        }
    }
}
