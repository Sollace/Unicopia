package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 50)
            .with(Trait.AIR, 9)
            .build();

    protected final ParticleHandle particlEffect = new ParticleHandle();

    private final TargetSelecter targetSelecter = new TargetSelecter(this);

    protected ShieldSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public Affinity getAffinity() {
        return getTraits().get(Trait.DARKNESS) > 0 ? Affinity.BAD : Affinity.GOOD;
    }

    protected void generateParticles(Caster<?> source) {
        float radius = (float)getDrawDropOffRange(source);
        Vec3d origin = getOrigin(source);

        source.spawnParticles(origin, new Sphere(true, radius), (int)(radius * 6), pos -> {
            source.addParticle(new MagicParticleEffect(getType().getColor()), pos, Vec3d.ZERO);
        });

        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.SPHERE, getType().getColor(), 0.3F, radius), origin, Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(0, radius);
            p.setAttribute(1, getType().getColor());
        });
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        if (situation == Situation.PROJECTILE) {
            applyEntities(source);
            return true;
        }

        float knowledge = getTraits().get(Trait.KNOWLEDGE, -6, 6);
        if (knowledge == 0) {
            knowledge = 1;
        }

        long costMultiplier = applyEntities(source);
        if (costMultiplier > 0) {
            double cost = 2 + source.getLevel().get();

            cost *= costMultiplier / ((1 + source.getLevel().get()) * 3F);
            cost /= 2.725D;
            cost /= knowledge;

            if (!source.subtractEnergyCost(cost)) {
                setDead();
            }
        }

        return !isDead();
    }

    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getDrawDropOffRange(Caster<?> source) {
        float multiplier = source.getMaster().isSneaking() ? 1 : 2;
        float min = 4 + getTraits().get(Trait.POWER);
        return (min + (source.getLevel().get() * 2)) / multiplier;
    }

    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return (entity instanceof LivingEntity
                || entity instanceof TntEntity
                || entity instanceof FallingBlockEntity
                || entity instanceof EyeOfEnderEntity
                || entity instanceof BoatEntity
                || ProjectileUtil.isFlyingProjectile(entity)
                || entity instanceof AbstractMinecartEntity)
            && !(entity instanceof ArmorStandEntity);
    }

    protected long applyEntities(Caster<?> source) {
        double radius = getDrawDropOffRange(source);

        Vec3d origin = getOrigin(source);

        targetSelecter.getEntities(source, radius, this::isValidTarget).forEach(i -> {
            try {
                applyRadialEffect(source, i, i.getPos().distanceTo(origin), radius);
            } catch (Throwable e) {
                Unicopia.LOGGER.error("Error updating radial effect", e);
            }
        });

        return targetSelecter.getTotalDamaged();
    }

    protected Vec3d getOrigin(Caster<?> source) {
        return source.getOriginVector();
    }

    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = getOrigin(source);

        if (ProjectileUtil.isFlyingProjectile(target)) {
            if (!ProjectileUtil.isProjectileThrownBy(target, source.getMaster())) {
                if (distance < 1) {
                    target.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1F, 1);
                    target.remove(RemovalReason.DISCARDED);
                } else {
                    ProjectileUtil.ricochet(target, pos, 0.9F);
                }
            }
        } else if (target instanceof LivingEntity) {
            double force = Math.max(0.1, radius / 4);

            if (isFriendlyTogether(source) && target instanceof PlayerEntity) {
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

}
