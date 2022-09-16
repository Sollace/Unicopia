package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
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

    protected ShieldSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean apply(Caster<?> source) {
        if (getTraits().get(Trait.GENEROSITY) > 0) {
            return toPlaceable().apply(source);
        }
        return super.apply(source);
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
            p.setAttribute(Attachment.ATTR_RADIUS, radius);
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
            double cost = 2 - source.getLevel().getScaled(2);

            cost *= costMultiplier / ((1 + source.getLevel().get()) * 3F);
            cost /= knowledge;
            cost += getDrawDropOffRange(source) / 10F;

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
        float multiplier = source instanceof Pony pony && pony.getMaster().isSneaking() ? 1 : 2;
        float min = 4 + getTraits().get(Trait.POWER);
        double range = (min + (source.getLevel().getScaled(4) * 2)) / multiplier;
        if (source instanceof Pony && range >= 4) {
            range = Math.sqrt(range);
        }
        return range;
    }

    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        boolean valid = (entity instanceof LivingEntity
                || entity instanceof TntEntity
                || entity instanceof FallingBlockEntity
                || ProjectileUtil.isFlyingProjectile(entity)
                || entity instanceof AbstractMinecartEntity)
            && !(  entity instanceof ArmorStandEntity
                || entity instanceof EyeOfEnderEntity
                || entity instanceof BoatEntity
        );

        if (getTraits().get(Trait.LIFE) > 0) {
            valid &= !(entity instanceof PassiveEntity);
        }
        if (getTraits().get(Trait.BLOOD) > 0) {
            valid &= !(entity instanceof HostileEntity);
        }
        if (getTraits().get(Trait.ICE) > 0) {
            valid &= !(entity instanceof PlayerEntity);
        }
        return valid;
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
                    target.playSound(USounds.SPELL_SHIELD_BURN_PROJECTILE, 0.1F, 1);
                    target.remove(RemovalReason.DISCARDED);
                } else {
                    ProjectileUtil.ricochet(target, pos, 0.9F);
                }
            }
        } else if (target instanceof LivingEntity) {
            double force = Math.max(0.1, radius / 4);

            if (isFriendlyTogether(source)) {
                force *= AttractionUtils.getForceAdjustment(target);
            } else {
                force *= 0.75;
            }

            AttractionUtils.applyForce(pos, target, force, (distance < 1 ? distance : 0), false);
        }
    }
}
