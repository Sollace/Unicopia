package com.minelittlepony.unicopia.entity.player;

import java.util.Random;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.FlightPredicate;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerPhysics extends EntityPhysics<Pony> implements Tickable, Motion, NbtSerialisable {

    private static final float MAXIMUM_FLIGHT_EXPERIENCE = 1500;

    public int ticksNextLevel = 0;
    public float flightExperience = 0;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;
    public boolean isRainbooming = false;

    private double lastTickPosX = 0;
    private double lastTickPosZ = 0;

    private final PlayerDimensions dimensions;

    public PlayerPhysics(Pony pony) {
        super(pony);
        dimensions = new PlayerDimensions(pony, this);
    }

    @Override
    public float getGravityModifier() {
        if (pony.getOwner().getAttributes() == null) {
            // may be null due to order of execution in the contructor.
            // Will have the default (1) here in any case, so it's safe to ignore the attribute a this point.
            return super.getGravityModifier();
        }
        return super.getGravityModifier() * (float)pony.getOwner().getAttributeValue(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    private boolean checkCanFly() {
        if (pony.getOwner().abilities.creativeMode || pony.getOwner().isSpectator()) {
            return true;
        }

        if (pony.hasSpell()) {
            Spell effect = pony.getSpell(true);
            if (!effect.isDead() && effect instanceof FlightPredicate) {
                return ((FlightPredicate)effect).checkCanFly(pony);
            }
        }

        return pony.getSpecies().canFly();
    }

    protected boolean isRainboom() {
        return Math.sqrt(getHorizontalMotion(pony.getOwner())) > 0.4F;
    }

    @Override
    public PlayerDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public boolean isExperienceCritical() {
        return isRainbooming || flightExperience > MAXIMUM_FLIGHT_EXPERIENCE * 0.8;
    }

    @Override
    public void tick() {
        PlayerEntity entity = pony.getOwner();

        MutableVector velocity = new MutableVector(entity.getVelocity());

        if (isExperienceCritical() && pony.isClient()) {
            Random rnd = pony.getWorld().random;

            for (int i = 0; i < 360 + getHorizontalMotion(entity); i += 10) {
                Vec3d pos = pony.getOriginVector().add(
                        rnd.nextGaussian() * entity.getWidth(),
                        rnd.nextGaussian() * entity.getHeight()/2,
                        rnd.nextGaussian() * entity.getWidth()
                );

                pony.addParticle(MagicParticleEffect.UNICORN, pos, velocity.toImmutable());
            }
        }

        boolean creative = entity.abilities.creativeMode || pony.getOwner().isSpectator();

        entity.abilities.allowFlying = checkCanFly();

        if (!creative) {
            entity.abilities.flying |= entity.abilities.allowFlying && isFlyingEither;

            if ((entity.isOnGround() && entity.isSneaking()) || entity.isTouchingWater()) {
                entity.abilities.flying = false;
            }
        }

        isFlyingSurvival = entity.abilities.flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.abilities.flying);

        if (!creative && !entity.isFallFlying()) {
            if (isFlyingSurvival && !entity.hasVehicle()) {

                if (!isRainbooming && getHorizontalMotion(entity) > 0.2 && flightExperience < MAXIMUM_FLIGHT_EXPERIENCE) {
                    flightExperience++;
                }

                entity.fallDistance = 0;

                if (pony.getSpecies() != Race.CHANGELING && entity.world.random.nextInt(100) == 0) {
                    float exhaustion = (0.3F * ticksNextLevel) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    exhaustion *= (1 - flightExperience / MAXIMUM_FLIGHT_EXPERIENCE);

                    entity.addExhaustion(exhaustion);
                }

                if (ticksNextLevel++ >= MAXIMUM_FLIGHT_EXPERIENCE) {
                    ticksNextLevel = 0;

                    entity.addExperience(1);
                    addFlightExperience(1);
                    entity.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, 1, 1);
                }

                moveFlying(entity, velocity);

                if (isExperienceCritical()) {

                    if (pony.getMagicalReserves().getEnergy() <= 0.25F) {
                        pony.getMagicalReserves().addEnergy(2);
                    }

                    if (isRainbooming || (entity.isSneaking() && isRainboom())) {
                        performRainboom(entity, velocity);
                    }
                }

                if (ticksNextLevel > 0 && ticksNextLevel % 30 == 0) {
                    entity.playSound(getWingSound(), 0.5F, 1);
                }
            } else {
                if (ticksNextLevel != 0) {
                    entity.playSound(getWingSound(), 0.4F, 1);
                }

                ticksNextLevel = 0;

                if (isExperienceCritical()) {
                    addFlightExperience(-0.39991342F);
                } else {
                    addFlightExperience(-0.019991342F);
                }

                if (flightExperience < 0.02) {
                    isRainbooming = false;
                }
            }
        }

        if (pony.getPhysics().isGravityNegative()) {
            entity.setOnGround(!entity.world.isAir(new BlockPos(entity.getX(), entity.getY() + entity.getHeight() + 0.5F, entity.getZ())));

            if (entity.isOnGround()) {
                entity.abilities.flying = false;
                isFlyingSurvival = false;
            }
        }

        lastTickPosX = entity.getX();
        lastTickPosZ = entity.getZ();

        entity.setVelocity(velocity.toImmutable());
    }

    public SoundEvent getWingSound() {
        return pony.getSpecies() == Race.CHANGELING ? USounds.CHANGELING_BUZZ : USounds.WING_FLAP;
    }

    protected void performRainboom(Entity entity, MutableVector velocity) {
        float forward = 0.5F * flightExperience / MAXIMUM_FLIGHT_EXPERIENCE;

        velocity.x += - forward * MathHelper.sin(entity.yaw * 0.017453292F);
        velocity.z +=   forward * MathHelper.cos(entity.yaw * 0.017453292F);
        velocity.y +=   forward * MathHelper.sin(entity.pitch * 0.017453292F);

        if (!isRainbooming || entity.world.random.nextInt(5) == 0) {
            entity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
        }

        if (flightExperience > 0) {
            flightExperience -= 13;
            isRainbooming = true;
        } else {
            isRainbooming = false;
        }
    }

    protected void moveFlying(Entity player, MutableVector velocity) {

        float forward = 0.000015F * flightExperience * (float)Math.sqrt(getHorizontalMotion(player));
        boolean sneak = !player.isSneaking();

        // vertical drop due to gravity
        if (sneak) {
            velocity.y -= (0.005F - getHorizontalMotion(player) / 100) * getGravitySignum();
        } else {
            forward += 0.005F;
            velocity.y -= 0.0001F * getGravitySignum();
        }

        velocity.x += - forward * MathHelper.sin(player.yaw * 0.017453292F);
        velocity.z += forward * MathHelper.cos(player.yaw * 0.017453292F);

        if (player.world.hasRain(player.getBlockPos())) {
            float glance = 360 * player.world.random.nextFloat();


            forward = 0.015F * player.world.random.nextFloat() *  player.world.getRainGradient(1);

            if (player.world.random.nextInt(30) == 0) {
                forward *= 10;
            }
            if (player.world.random.nextInt(30) == 0) {
                forward *= 10;
            }
            if (player.world.random.nextInt(40) == 0) {
                forward *= 100;
            }

            if (player.world.isThundering() && player.world.random.nextInt(60) == 0) {
                velocity.y += forward * 3;
            }

            if (forward >= 1) {
                player.world.playSound(null, player.getBlockPos(), USounds.WIND_RUSH, SoundCategory.AMBIENT, 3, 1);
            }

            if (forward > 4) {
                forward = 4;
            }

            velocity.x += - forward * MathHelper.sin((player.yaw + glance) * 0.017453292F);
            velocity.z += forward * MathHelper.cos((player.yaw + glance) * 0.017453292F);
        }
    }

    protected double getHorizontalMotion(Entity e) {
        double motionX = e.getX() - lastTickPosX;
        double motionZ = e.getZ() - lastTickPosZ;

        return (motionX * motionX) + (motionZ * motionZ);
    }

    protected SoundEvent getFallSound(int distance) {
        return distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }

    private void addFlightExperience(float factor) {
        float maximumGain = MAXIMUM_FLIGHT_EXPERIENCE - flightExperience;
        float gainSteps = 20;

        flightExperience = Math.max(0, flightExperience + factor * maximumGain / gainSteps);
    }

    public void updateFlightStat(boolean flying) {
        PlayerEntity entity = pony.getOwner();

        entity.abilities.allowFlying = checkCanFly();

        if (entity.abilities.allowFlying) {
            entity.abilities.flying |= flying;

            isFlyingSurvival = entity.abilities.flying;

            if (isFlyingSurvival) {
                ticksNextLevel = 0;
            }
        } else {
            entity.abilities.flying = false;
            isFlyingSurvival = false;
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putInt("flightDuration", ticksNextLevel);
        compound.putFloat("flightExperience", flightExperience);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putBoolean("isRainbooming", isRainbooming);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        ticksNextLevel = compound.getInt("flightDuration");
        flightExperience = compound.getFloat("flightExperience");
        isFlyingSurvival = compound.getBoolean("isFlying");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        isRainbooming = compound.getBoolean("isRainbooming");

        pony.getOwner().calculateDimensions();
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival;
    }

    @Override
    public float getFlightExperience() {
        return flightExperience / MAXIMUM_FLIGHT_EXPERIENCE;
    }

    @Override
    public float getFlightDuration() {
        return ticksNextLevel / MAXIMUM_FLIGHT_EXPERIENCE;
    }
}
