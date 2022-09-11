package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell implements ProjectileSpell, HomingSpell {

    private final EntityReference<Entity> target = new EntityReference<>();

    private int age;
    private int duration;

    protected AttractiveSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
        duration = 120 + (int)(traits.get(Trait.FOCUS, 0, 160) * 19);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        age++;

        if (age % 20 == 0) {
            duration--;
        }

        if (duration <= 0) {
            return false;
        }

        Vec3d pos = caster.getOriginVector();
        if (target.isPresent(caster.getReferenceWorld()) && target.get(caster.getReferenceWorld()).distanceTo(caster.getEntity()) > getDrawDropOffRange(caster)) {
            target.get(caster.getReferenceWorld()).requestTeleport(pos.x, pos.y, pos.z);
        }

        return super.tick(caster, situation);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        double range = getDrawDropOffRange(source) + 10;

        source.spawnParticles(getOrigin(source), new Sphere(false, range), 7, p -> {
            source.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, source.getEntity(), 0.4F)
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
        if (target.isPresent(entity.world)) {
            return target.get(entity.world) == entity;
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

        if (!isGood && source.getReferenceWorld().random.nextInt(4500) == 0) {
            source.getEntity().damage(MagicalDamageSource.create("vortex"), 4);
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

        if (this.target.get(target.world) == target) {
            target.fallDistance = 0;

            if (target.isOnGround()) {
                target.setPosition(target.getPos().add(0, 0.3, 0));
                target.setOnGround(false);
            }
        }
        target.setVelocity(x, y, z);
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
    public void onDestroyed(Caster<?> caster) {
        target.getOrEmpty(caster.getReferenceWorld()).ifPresent(target -> target.setGlowing(false));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        if (!isDead() && getTraits().get(Trait.CHAOS) > 0) {
            setDead();
            Caster.of(entity).ifPresent(getType().create(getTraits())::apply);
        }
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("target", target.toNBT());
        compound.putInt("age", age);
        compound.putInt("duration", duration);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        target.fromNBT(compound.getCompound("target"));
        age = compound.getInt("age");
        duration = compound.getInt("duration");
    }
}
