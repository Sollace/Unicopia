package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 50)
            .with(Trait.AIR, 9)
            .build();

    private final TargetSelecter targetSelecter = new TargetSelecter(this).setFilter(this::isValidTarget);

    private float prevRadius;
    private float radius;

    private float rangeMultiplier;
    private float targetRangeMultiplier;

    private int prevTicksDying;
    private int ticksDying;

    protected ShieldSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF || getTraits().get(Trait.GENEROSITY) > 0 ? toPlaceable() : this;
    }

    @Override
    public Affinity getAffinity() {
        return getTraits().get(Trait.DARKNESS) > 0 ? Affinity.BAD : Affinity.GOOD;
    }

    protected void generateParticles(Caster<?> source) {
        Vec3d origin = getOrigin(source);

        source.spawnParticles(origin, new Sphere(true, radius), (int)(radius * 6), pos -> {
            source.addParticle(new MagicParticleEffect(getType().getColor()), pos, Vec3d.ZERO);

            if (source.asWorld().random.nextInt(10) == 0 && source.asWorld().random.nextFloat() < source.getCorruption().getScaled(1)) {
                ParticleUtils.spawnParticle(source.asWorld(), new LightningBoltParticleEffect(true, 3, 2, 0.1F, Optional.empty()), pos, Vec3d.ZERO);
            }
        });
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        prevRadius = radius;
        radius = (float)getDrawDropOffRange(source);

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
            consumeManage(source, costMultiplier, knowledge);
        }

        return !isDead();
    }

    @Override
    public void tickDying(Caster<?> caster) {
        prevTicksDying = ticksDying;
        if (ticksDying++ > 25) {
            super.tickDying(caster);
        }
    }

    protected void consumeManage(Caster<?> source, long costMultiplier, float knowledge) {
        double cost = 2 - source.getLevel().getScaled(2);

        cost *= costMultiplier / ((1 + source.getLevel().get()) * 3F);
        cost /= knowledge;
        cost += radius / 10F;

        if (!source.subtractEnergyCost(cost)) {
            setDead();
        }
    }

    public float getRadius(float tickDelta) {
        float base = MathHelper.lerp(tickDelta, prevRadius, radius);
        float scale = MathHelper.clamp(MathHelper.lerp(tickDelta, prevTicksDying, ticksDying), 0, 1);
        return base * scale;
    }

    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getDrawDropOffRange(Caster<?> source) {
        targetRangeMultiplier = source instanceof Pony pony && pony.asEntity().isSneaking() ? 1 : 2;
        if (rangeMultiplier < targetRangeMultiplier - 0.1F) {
            rangeMultiplier += 0.1F;
        } else if (rangeMultiplier > targetRangeMultiplier + 0.1) {
            rangeMultiplier -= 0.1F;
        } else {
            rangeMultiplier = targetRangeMultiplier;
        }

        float min = (source instanceof Pony ? 4 : 6) + getTraits().get(Trait.POWER);
        double range = (min + (source.getLevel().getScaled(source instanceof Pony ? 4 : 40) * (source instanceof Pony ? 2 : 10))) / rangeMultiplier;

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
        Vec3d origin = getOrigin(source);
        targetSelecter.getEntities(source, radius).forEach(i -> {
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
