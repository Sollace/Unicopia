package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Map;
import java.util.UUID;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BubbleSpell extends AbstractSpell implements TimedSpell,
        ProjectileDelegate.EntityHitListener {
    private static final EntityAttributeModifier GRAVITY_MODIFIER =
            new EntityAttributeModifier(UUID.fromString("9dc7818b-927b-46e0-acbe-48d31a28128f"), "Bubble Floating", 0.02D - 1D, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier SPEED_MODIFIER =
            new EntityAttributeModifier(UUID.fromString("9dc7818b-927b-46e0-acbe-48d31a28128f"), "Bubble Floating", 0.01D - 1D, Operation.MULTIPLY_TOTAL);

    private static final Map<EntityAttribute, EntityAttributeModifier> MODIFIERS = Map.of(
            UEntityAttributes.ENTITY_GRAVITY_MODIFIER, GRAVITY_MODIFIER,
            UEntityAttributes.EXTENDED_REACH_DISTANCE, GRAVITY_MODIFIER,
            UEntityAttributes.EXTENDED_ATTACK_DISTANCE, GRAVITY_MODIFIER,
            EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_MODIFIER,
            EntityAttributes.GENERIC_FLYING_SPEED, SPEED_MODIFIER
    );

    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 6)
            .with(Trait.POWER, 1)
            .build();

    protected final ParticleHandle particlEffect = new ParticleHandle();

    private final Timer timer;

    private int struggles;
    private float radius;

    protected BubbleSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer((120 + (int)(getTraits().get(Trait.FOCUS, 0, 160) * 19)) * 20);
        struggles = (int)(getTraits().get(Trait.POWER) * 2);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean apply(Caster<?> source) {

        if (getType().isOn(source)) {
            source.getSpellSlot().removeWhere(getType(), true);
            return false;
        }

        Entity entity = source.asEntity();

        if (entity instanceof LivingEntity l) {
            MODIFIERS.forEach((attribute, modifier) -> {
                if (l.getAttributes().hasAttribute(attribute)) {
                    l.getAttributeInstance(attribute).addPersistentModifier(modifier);
                }
            });
        }
        radius = Math.max(entity.getHeight(), entity.getWidth()) * 1.2F;
        source.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1);
        entity.addVelocity(0, 0.2F * source.getPhysics().getGravitySignum(), 0);
        Living.updateVelocity(entity);

        return super.apply(source);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE) {

            source.spawnParticles(ParticleTypes.BUBBLE, 2);
            return true;
        }

        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        setDirty();

        source.asEntity().addVelocity(
                MathHelper.sin(source.asEntity().age / 6F) / 50F,
                MathHelper.sin(source.asEntity().age / 6F) / 50F,
                MathHelper.sin(source.asEntity().age / 6F) / 50F
        );

        source.asEntity().fallDistance = 0;

        Vec3d origin = source.getOriginVector();

        if (source instanceof Pony pony && pony.sneakingChanged() && pony.asEntity().isSneaking()) {
            setDirty();
            radius += 0.5F;
            source.playSound(USounds.SPELL_BUBBLE_DISTURB, 1);
            if (struggles-- <= 0) {
                setDead();
                return false;
            }
        }

        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.SPHERE, 0xFFFFFF, 0.3F, 0, new Vec3d(0, radius / 2F, 0)), origin, Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(Attachment.ATTR_RADIUS, radius);
        });

        return !isDead();
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        particlEffect.destroy();
        if (source.asEntity() instanceof LivingEntity l) {
            MODIFIERS.forEach((attribute, modifier) -> {
                if (l.getAttributes().hasAttribute(attribute)) {
                    l.getAttributeInstance(attribute).tryRemoveModifier(modifier.getId());
                }
            });
        }
        source.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1);
    }


    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(hit.getEntity()).ifPresent(caster -> {
            getTypeAndTraits().apply(caster, CastingMethod.INDIRECT);
        });
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("struggles", struggles);
        compound.putFloat("radius", radius);
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        struggles = compound.getInt("struggles");
        radius = compound.getFloat("radius");
        timer.fromNBT(compound);
    }
}
