package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Map;
import java.util.UUID;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.AttributeContainer;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
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

    private static final SpellAttribute<Integer> SOAPINESS = SpellAttribute.create(SpellAttributes.SOAPINESS, AttributeFormat.REGULAR, Trait.POWER, power -> (int)(power * 2));

    static final TooltipFactory TOOLTIP = TooltipFactory.of(TimedSpell.TIME, SOAPINESS);

    private final Timer timer;

    private float prevRadius;
    private DataTracker.Entry<Float> radius;
    private DataTracker.Entry<Integer> struggles;

    protected BubbleSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer(TIME.get(getTraits()));
        radius = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
        struggles = dataTracker.startTracking(TrackableDataType.INT, SOAPINESS.get(getTraits()));
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    public float getRadius(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevRadius, radius.get());
    }

    @Override
    public boolean apply(Caster<?> source) {

        if (source.getSpellSlot().removeWhere(getType())) {
            return false;
        }

        Entity entity = source.asEntity();

        if (source instanceof AttributeContainer l) {
            l.applyAttributeModifiers(MODIFIERS, false, true);
        }
        radius.set(Math.max(entity.getHeight(), entity.getWidth()) * 1.2F);
        source.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1);
        entity.addVelocity(0, 0.2F * source.getPhysics().getGravitySignum(), 0);
        Living.updateVelocity(entity);

        return super.apply(source);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            source.spawnParticles(UParticles.BUBBLE, 2);
            return true;
        }

        timer.tick();

        boolean done = timer.getTicksRemaining() <= 0;

        source.spawnParticles(source.getOriginVector().add(0, 1, 0), new Sphere(true, radius.get() * (done ? 0.25F : 0.5F)), done ? 13 : 1, pos -> {
            source.addParticle(done ? ParticleTypes.BUBBLE_POP : UParticles.BUBBLE, pos, Vec3d.ZERO);
        });

        if (done) {
            return false;
        }

        source.asEntity().addVelocity(
                MathHelper.sin(source.asEntity().age / 6F) / 50F,
                MathHelper.sin(source.asEntity().age / 6F) / 50F,
                MathHelper.sin(source.asEntity().age / 6F) / 50F
        );

        source.asEntity().fallDistance = 0;

        prevRadius = radius.get();

        if (source instanceof Pony pony && pony.sneakingChanged() && pony.asEntity().isSneaking()) {
            radius.set(radius.get() + 0.5F);
            source.playSound(USounds.SPELL_BUBBLE_DISTURB, 1);
            int s = struggles.get() - 1;
            struggles.set(s);
            if (s <= 0) {
                setDead();
                return false;
            }
        }

        return !isDead();
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        super.onDestroyed(source);
        if (source instanceof AttributeContainer l) {
            l.applyAttributeModifiers(MODIFIERS, false, false);
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
        compound.putInt("struggles", struggles.get());
        compound.putFloat("radius", radius.get());
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        struggles.set(compound.getInt("struggles"));
        radius.set(compound.getFloat("radius"));
        timer.fromNBT(compound);
    }
}
