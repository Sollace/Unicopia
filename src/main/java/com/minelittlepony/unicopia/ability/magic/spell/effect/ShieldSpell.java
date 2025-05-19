package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.CastOn;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.Lerp;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ShieldSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 50)
            .with(Trait.AIR, 9)
            .build();

    static final SpellAttribute<Float> RANGE = SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> Math.max(0, 4 + power));
    protected static final SpellAttribute<CastOn> CAST_ON = SpellAttribute.createEnumerated(SpellAttributeType.CAST_ON, Trait.GENEROSITY, generosity -> generosity > 0 ? CastOn.LOCATION : CastOn.SELF);

    static final SpellAttribute<Boolean> TARGET_ITEMS = SpellAttribute.createConditional(SpellAttributeType.PERMIT_ITEMS, Trait.KNOWLEDGE, knowledge -> knowledge > 10);
    static final SpellAttribute<Boolean> PERMIT_PASSIVE = SpellAttribute.createConditional(SpellAttributeType.PERMIT_PASSIVE, Trait.LIFE, l -> l > 0);
    static final SpellAttribute<Boolean> PERMIT_HOSTILE = SpellAttribute.createConditional(SpellAttributeType.PERMIT_HOSTILE, Trait.BLOOD, l -> l > 0);
    static final SpellAttribute<Boolean> PERMIT_PLAYER = SpellAttribute.createConditional(SpellAttributeType.PERMIT_PLAYER, Trait.ICE, l -> l > 0);

    static final TooltipFactory PERMIT_ENTITY = TooltipFactory.of(PERMIT_PASSIVE, PERMIT_HOSTILE, PERMIT_PLAYER);
    static final TooltipFactory TARGET = (type, tooltip) -> (TARGET_ITEMS.get(type.traits()) ? TARGET_ITEMS : PERMIT_ENTITY).appendTooltip(type, tooltip);

    static final TooltipFactory TOOLTIP = TooltipFactory.of(RANGE, TARGET, CAST_ON);

    protected final TargetSelecter targetSelecter = new TargetSelecter(this).setFilter(this::isValidTarget);

    private final Lerp radius = new Lerp(0);
    private final Lerp rangeMultiplier = new Lerp(1);

    private int prevTicksDying;
    private int ticksDying;

    protected ShieldSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF || CAST_ON.get(getTraits()) == CastOn.LOCATION ? toPlaceable() : this;
    }

    @Override
    public Affinity getAffinity() {
        return getTraits().get(Trait.DARKNESS) > 0 ? Affinity.BAD : Affinity.GOOD;
    }

    protected void generateParticles(Caster<?> source) {
        Vec3d origin = getOrigin(source);

        source.spawnParticles(origin, new Sphere(true, radius.getValue()), (int)(radius.getValue() * 2), pos -> {
            int hornColor = MineLPDelegate.getInstance().getMagicColor(source.getOriginatingCaster().asEntity());
            source.addParticle(new MagicParticleEffect(Argb.lerp(0.6F, getType().getColor(), hornColor)), pos, Vec3d.ZERO);

            if (source.asWorld().random.nextInt(10) == 0 && source.asWorld().random.nextFloat() < source.getCorruption().getScaled(1)) {
                ParticleUtils.spawnParticle(source.asWorld(), new LightningBoltParticleEffect(true, 3, 2, 0.1F, Optional.empty()), pos, Vec3d.ZERO);
            }
        });

        if (source.asWorld().random.nextInt(20) == 0 || !rangeMultiplier.isFinished() || !radius.isFinished()) {
            source.asEntity().playSound(USounds.SPELL_CAST_SUCCESS, 0.05F, 1.5F);
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        rangeMultiplier.update(source instanceof Pony pony && pony.asEntity().isSneaking() ? 1 : 2, 500L);
        radius.update((float)getDrawDropOffRange(source), 200L);

        if (source.isClient()) {
            generateParticles(source);
        } else {
            Ether.get(source.asWorld()).getOrCreate(this, source).setRadius(radius.getValue());
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
        rangeMultiplier.update(caster instanceof Pony pony && pony.asEntity().isSneaking() ? 1 : 2, 10L);
        radius.update((float)getDrawDropOffRange(caster), 10L);
        prevTicksDying = ticksDying;
        if (ticksDying++ > 25) {
            super.tickDying(caster);
        }
    }

    protected void consumeManage(Caster<?> source, long costMultiplier, float knowledge) {
        double cost = 2 - source.getLevel().getScaled(2);

        cost *= costMultiplier / ((1 + source.getLevel().get()) * 3F);
        cost /= knowledge;
        cost += radius.getValue() / 10F;

        if (!source.subtractEnergyCost(cost)) {
            setDead();
        }
    }

    public float getRadius(float tickDelta) {
        float base = radius.getValue();
        float scale = 1 - MathHelper.clamp(MathHelper.lerp(tickDelta, (float)prevTicksDying, ticksDying) / 25F, 0, 1);
        return base * scale;
    }

    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getDrawDropOffRange(Caster<?> source) {
        float min = (source instanceof Pony ? 0 : 2) + RANGE.get(getTraits());
        double range = (min + (source.getLevel().getScaled(source instanceof Pony ? 4 : 40) * (source instanceof Pony ? 2 : 10))) / rangeMultiplier.getValue();

        return range;
    }

    protected boolean isValidTarget(Caster<?> source, Entity entity) {

        if (TARGET_ITEMS.get(getTraits())) {
            return entity instanceof ItemEntity;
        }

        boolean valid = (entity instanceof LivingEntity
                || entity instanceof TntEntity
                || entity instanceof FallingBlockEntity
                || ProjectileUtil.isFlyingProjectile(entity)
                || entity instanceof AbstractMinecartEntity)
            && !(  entity instanceof ArmorStandEntity
                || entity instanceof EyeOfEnderEntity
                || entity instanceof BoatEntity
        );

        if (PERMIT_PASSIVE.get(getTraits())) {
            valid &= !(entity instanceof PassiveEntity);
        }
        if (PERMIT_HOSTILE.get(getTraits())) {
            valid &= !(entity instanceof HostileEntity);
        }
        if (PERMIT_PLAYER.get(getTraits())) {
            valid &= !(entity instanceof PlayerEntity);
        }
        return valid;
    }

    protected long applyEntities(Caster<?> source) {
        Vec3d origin = getOrigin(source);
        targetSelecter.getEntities(source, radius.getValue()).forEach(i -> {
            try {
                applyRadialEffect(source, i, i.getPos().distanceTo(origin), radius.getValue());
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
