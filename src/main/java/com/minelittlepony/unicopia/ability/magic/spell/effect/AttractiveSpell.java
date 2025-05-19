package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell implements HomingSpell, TimedSpell, ProjectileDelegate.EntityHitListener {
    static final SpellAttribute<Boolean> TARGET_FOCUSED_ENTITY = SpellAttribute.createConditional(SpellAttributeType.FOCUSED_ENTITY, Trait.ORDER, order -> order >= 20);
    static final SpellAttribute<Boolean> STICK_TO_TARGET = SpellAttribute.createConditional(SpellAttributeType.STICK_TO_TARGET, Trait.CHAOS, chaos -> chaos > 0);
    static final TooltipFactory TARGET = (type, tooltip) -> (TARGET_FOCUSED_ENTITY.get(type.traits()) ? TARGET_FOCUSED_ENTITY : ShieldSpell.TARGET).appendTooltip(type, tooltip);
    static final TooltipFactory TOOLTIP = TooltipFactory.of(TIME, RANGE, TARGET, STICK_TO_TARGET, CAST_ON);

    private final EntityReference<Entity> target = dataTracker.startTracking(new EntityReference<>());

    private final Timer timer = new Timer(TIME.get(getTraits()));

    protected AttractiveSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        target.getOrEmpty(caster.asWorld())
            .filter(entity -> entity.distanceTo(caster.asEntity()) > getDrawDropOffRange(caster))
            .ifPresent(entity -> {
                Vec3d pos = caster.getOriginVector();
                entity.requestTeleport(pos.x, pos.y, pos.z);
            });

        return super.tick(caster, situation);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        double range = getDrawDropOffRange(source);
        Vec3d origin = getOrigin(source);

        source.spawnParticles(origin, new Sphere(false, range), 7, p -> {
            source.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, origin, 0.4F)
                        .withChild(ParticleTypes.EFFECT),
                    p,
                    Vec3d.ZERO
            );
        });
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return target.referenceEquals(entity) || super.isValidTarget(source, entity);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        double force = 2.5F * distance;

        boolean isGood = isFriendlyTogether(source);

        if (isGood) {
            force *= AttractionUtils.getForceAdjustment(target);
        }

        if (!isGood && source.asWorld().random.nextInt(4500) == 0) {
            source.asEntity().damage(source.damageOf(UDamageTypes.GAVITY_WELL_RECOIL, source), 4);
        }

        AttractionUtils.applyForce(getOrigin(source), target, -force, 0, false);

        float maxVel = !isFriendlyTogether(source) ? 1 : 1.6f;

        Vec3d vel = target.getVelocity();

        double x = MathHelper.clamp(vel.x, -maxVel, maxVel);
        double y = MathHelper.clamp(vel.y, -maxVel, maxVel);
        double z = MathHelper.clamp(vel.z, -maxVel, maxVel);

        if (distance < 0.5) {
            z += maxVel * 2;
        }
        if (distance < 2) {
            x = 0;
            z = 0;
        }

        if (this.target.referenceEquals(target)) {
            target.fallDistance = 0;

            if (target.isOnGround()) {
                target.setPosition(target.getPos().add(0, 0.3, 0));
                target.setOnGround(false);
            }
        }
        target.setVelocity(x, y, z);
        Living.updateVelocity(target);
    }

    @Override
    public boolean setTarget(Entity target) {
        if (TARGET_FOCUSED_ENTITY.get(getTraits())) {
            this.target.set(target);
            target.setGlowing(true);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        target.getOrEmpty(caster.asWorld()).ifPresent(target -> target.setGlowing(false));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!isDead() && STICK_TO_TARGET.get(getTraits())) {
            setDead();
            Caster.of(hit.getEntity()).ifPresent(caster -> getTypeAndTraits().apply(caster, CastingMethod.INDIRECT));
        }
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.put("target", target.toNBT(lookup));
        timer.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        target.fromNBT(compound.getCompound("target"), lookup);
        timer.fromNBT(compound, lookup);
    }
}
