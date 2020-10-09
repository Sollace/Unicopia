package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractRangedAreaSpell implements Attached {

    private final ParticleHandle particlEffect = new ParticleHandle();

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
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public void render(Caster<?> source) {
        float radius = (float)getDrawDropOffRange(source);

        source.spawnParticles(new Sphere(true, radius), (int)(radius * 6), pos -> {
            source.addParticle(new MagicParticleEffect(getTint()), pos, Vec3d.ZERO);
        });

        particlEffect.ifAbsent(source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(getTint(), 0.3F, radius), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.attach(source);
            p.setAttribute(0, radius);
            p.setAttribute(1, getTint());
        });
    }

    @Override
    public boolean updateOnPerson(Caster<?> source) {
        int costMultiplier = applyEntities(source);
        if (costMultiplier > 0) {
            if (source.getMaster().age % 20 == 0) {
                double cost = 4 + (source.getLevel().get() * 2);

                cost *= costMultiplier / 5F;

                if (!source.subtractEnergyCost(cost)) {
                    onDestroyed(source);
                }
            }
        }

        return !isDead();
    }

    public double getDrawDropOffRange(Caster<?> source) {
        float multiplier = (source.getMaster().isSneaking() ? 1 : 2);
        return (4 + (source.getLevel().get() * 2)) / multiplier;
    }

    @Override
    public boolean update(Caster<?> source) {
        applyEntities(source);
        return true;
    }

    protected List<Entity> getTargets(Caster<?> source, double radius) {

        Entity owner = source.getMaster();

        boolean ownerIsValid = source.getAffinity() != Affinity.BAD && EquinePredicates.PLAYER_UNICORN.test(owner);

        return source.findAllEntitiesInRange(radius)
            .filter(entity -> {
                if (!ownerIsValid) {
                    return true;
                }

                boolean ownerEquals = (
                        entity.equals(owner)
                    || (entity instanceof PlayerEntity && owner instanceof PlayerEntity && Pony.equal((PlayerEntity)entity, (PlayerEntity)owner)));

                if (!owner.isSneaking()) {
                    return ownerEquals;
                }

                return !ownerEquals;
            })
            .collect(Collectors.toList());
    }

    protected int applyEntities(Caster<?> source) {
        double radius = getDrawDropOffRange(source);

        Vec3d origin = source.getOriginVector();

        List<Entity> targets = getTargets(source, radius);
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

    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        if (ProjectileUtil.isProjectile(target)) {
            if (!ProjectileUtil.isProjectileThrownBy(target, source.getMaster())) {
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
                force *= calculateAdjustedForce(Pony.of((PlayerEntity)target));
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
    protected double calculateAdjustedForce(Pony player) {
        double force = 0.75;

        if (player.getSpecies().canUseEarth()) {
            force /= 2;

            if (player.getMaster().isSneaking()) {
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
