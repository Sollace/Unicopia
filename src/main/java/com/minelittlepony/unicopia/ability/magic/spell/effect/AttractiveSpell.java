package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell implements HomingSpell, TimedSpell, ProjectileDelegate.EntityHitListener {

    private final EntityReference<Entity> target = new EntityReference<>();

    private final Timer timer;

    protected AttractiveSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer((120 + (int)(getTraits().get(Trait.FOCUS, 0, 160) * 19)) * 20);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (getType() != SpellType.DARK_VORTEX) {
            timer.tick();

            if (timer.getTicksRemaining() <= 0) {
                return false;
            }
        }

        setDirty();
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
        double range = getDrawDropOffRange(source) + 10;

        source.spawnParticles(getOrigin(source), new Sphere(false, range), 7, p -> {
            source.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, source.asEntity(), 0.4F)
                        .withChild(new MagicParticleEffect(getType().getColor())),
                    p,
                    Vec3d.ZERO
            );
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return 10 + (caster.getLevel().getScaled(8) * 2);
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        if (target.referenceEquals(entity)) {
            return true;
        }
        return getTraits().get(Trait.KNOWLEDGE) > 10 ? entity instanceof ItemEntity : super.isValidTarget(source, entity);
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
        if (getTraits().get(Trait.ORDER) >= 20) {
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
        if (!isDead() && getTraits().get(Trait.CHAOS) > 0) {
            setDead();
            Caster.of(hit.getEntity()).ifPresent(caster -> getTypeAndTraits().apply(caster, CastingMethod.INDIRECT));
        }
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("target", target.toNBT());
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        target.fromNBT(compound.getCompound("target"));
        timer.fromNBT(compound);
    }
}
