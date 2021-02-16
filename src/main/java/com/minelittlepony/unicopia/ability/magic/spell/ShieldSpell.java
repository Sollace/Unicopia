package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractRangedAreaSpell implements Attached {

    private final ParticleHandle particlEffect = new ParticleHandle();

    private final Map<UUID, Target> targets = new TreeMap<>();

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
        long costMultiplier = applyEntities(source);
        if (costMultiplier > 0) {
            double cost = 2 + source.getLevel().get();

            cost *= costMultiplier / ((1 + source.getLevel().get()) * 3F);
            cost /= 2.725D;

            if (!source.subtractEnergyCost(cost)) {
                onDestroyed(source);
            }
        }

        return !isDead();
    }

    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getDrawDropOffRange(Caster<?> source) {
        float multiplier = source.getMaster().isSneaking() ? 1 : 2;
        return (4 + (source.getLevel().get() * 2)) / multiplier;
    }

    @Override
    public boolean update(Caster<?> source) {
        applyEntities(source);
        return true;
    }

    protected List<Entity> getTargets(Caster<?> source, double radius) {

        Entity owner = source.getMaster();

        boolean ownerIsValid = source.getAffinity() != Affinity.BAD && (EquinePredicates.PLAYER_UNICORN.test(owner) && owner.isSneaking());

        return source.findAllEntitiesInRange(radius)
            .filter(entity -> {
                return
                        !FriendshipBraceletItem.isComrade(source, entity)
                        && (entity instanceof LivingEntity
                        || entity instanceof TntEntity
                        || entity instanceof FallingBlockEntity
                        || entity instanceof EyeOfEnderEntity
                        || entity instanceof BoatEntity
                        || ProjectileUtil.isFlyingProjectile(entity)
                        || entity instanceof AbstractMinecartEntity)
                        && !(entity instanceof ArmorStandEntity)
                        && !(ownerIsValid && (Pony.equal(entity, owner) || owner.isConnectedThroughVehicle(entity)));
            })
            .collect(Collectors.toList());
    }

    protected long applyEntities(Caster<?> source) {
        double radius = getDrawDropOffRange(source);

        Vec3d origin = source.getOriginVector();

        this.targets.values().removeIf(Target::tick);

        List<Entity> targets = getTargets(source, radius);
        targets.forEach(i -> {
            try {
                this.targets.computeIfAbsent(i.getUuid(), Target::new);
                double dist = i.getPos().distanceTo(origin);

                applyRadialEffect(source, i, dist, radius);
            } catch (Throwable e) {
                Unicopia.LOGGER.error("Error updating shield effect", e);
            }
        });

        return this.targets.values().stream().filter(Target::canHurt).count();
    }

    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = source.getOriginVector();

        if (ProjectileUtil.isFlyingProjectile(target)) {
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

        if (target instanceof LivingEntity) {
            pos = pos.multiply(1 / (1 + EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, (LivingEntity)target)));
        }

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

    class Target {

        int cooldown = 20;

        Target(UUID id) {
        }

        boolean tick() {
            return --cooldown < 0;
        }

        boolean canHurt() {
            return cooldown == 20;
        }
    }
}
