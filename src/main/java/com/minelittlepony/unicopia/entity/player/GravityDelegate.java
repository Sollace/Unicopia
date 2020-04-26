package com.minelittlepony.unicopia.entity.player;

import java.util.Random;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.FlightPredicate;
import com.minelittlepony.unicopia.entity.FlightControl;
import com.minelittlepony.unicopia.entity.Updatable;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GravityDelegate implements Updatable, FlightControl, NbtSerialisable, FlightPredicate {

    final Pony player;

    private static final float MAXIMUM_FLIGHT_EXPERIENCE = 1500;

    public int ticksNextLevel = 0;
    public float flightExperience = 0;

    public boolean isFlying = false;
    public boolean isRainbooming = false;

    private double lastTickPosX = 0;
    private double lastTickPosZ = 0;

    private float gravity = 0;

    private final PlayerDimensionsDelegate dimensions;

    public GravityDelegate(Pony player) {
        this.player = player;
        this.dimensions = new PlayerDimensionsDelegate(this);
    }

    @Override
    public boolean checkCanFly(Pony player) {
        if (player.getOwner().abilities.creativeMode) {
            return true;
        }

        if (player.hasEffect()) {
            MagicEffect effect = player.getEffect();
            if (!effect.isDead() && effect instanceof FlightPredicate) {
                return ((FlightPredicate)effect).checkCanFly(player);
            }
        }

        return player.getSpecies().canFly();
    }

    protected boolean isRainboom() {
        return Math.sqrt(getHorizontalMotion(player.getOwner())) > 0.4F;
    }

    public PlayerDimensionsDelegate getDimensions() {
        return dimensions;
    }

    public void setGraviationConstant(float constant) {
        gravity = constant;
    }

    public float getGravitationConstant() {
        return gravity;
    }

    @Override
    public boolean isExperienceCritical() {
        return isRainbooming || flightExperience > MAXIMUM_FLIGHT_EXPERIENCE * 0.8;
    }

    @Override
    public void onUpdate() {
        PlayerEntity entity = player.getOwner();

        MutableVector velocity = new MutableVector(entity.getVelocity());

        if (isExperienceCritical() && player.isClient()) {
            Random rnd = player.getWorld().random;

            for (int i = 0; i < 360 + getHorizontalMotion(entity); i += 10) {
                Vec3d pos = player.getOriginVector().add(
                        rnd.nextGaussian() * entity.getWidth(),
                        rnd.nextGaussian() * entity.getHeight()/2,
                        rnd.nextGaussian() * entity.getWidth()
                );

                player.addParticle(MagicParticleEffect.UNICORN, pos, velocity.toImmutable());
            }
        }

        entity.abilities.allowFlying = checkCanFly(player);

        if (!entity.abilities.creativeMode) {
            entity.abilities.flying |= entity.abilities.allowFlying && isFlying && !entity.onGround && !entity.isTouchingWater();
        }

        isFlying = entity.abilities.flying && !entity.abilities.creativeMode;

        if (gravity != 0) {
            if (!entity.abilities.flying) {
                velocity.y += 0.038;
                velocity.y -= gravity;
            }

            if (gravity < 0) {
                entity.onGround = !entity.world.isAir(new BlockPos(entity.getX(), entity.getY() + entity.getHeight() + 0.5F, entity.getZ()));

                if (entity.onGround) {
                    entity.abilities.flying = false;
                    isFlying = false;
                }
            }
        }

        if (dimensions.update()) {
            player.getOwner().calculateDimensions();
        }

        if (!entity.abilities.creativeMode && !entity.isFallFlying()) {
            if (isFlying && !entity.hasVehicle()) {

                if (!isRainbooming && getHorizontalMotion(entity) > 0.2 && flightExperience < MAXIMUM_FLIGHT_EXPERIENCE) {
                    flightExperience++;
                }

                entity.fallDistance = 0;

                if (player.getSpecies() != Race.CHANGELING && entity.world.random.nextInt(100) == 0) {
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
                    addFlightExperience(entity, 1);
                    entity.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, 1, 1);
                }

                moveFlying(entity, velocity);

                if (isExperienceCritical()) {

                    if (player.getMagicalReserves().getEnergy() <= 0.25F) {
                        player.getMagicalReserves().addEnergy(2);
                    }

                    if (isRainbooming || (entity.isSneaking() && isRainboom())) {
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
                    addFlightExperience(entity, -0.39991342F);
                } else {
                    addFlightExperience(entity, -0.019991342F);
                }

                if (flightExperience < 0.02) {
                    isRainbooming = false;
                }
            }
        }

        lastTickPosX = entity.getX();
        lastTickPosZ = entity.getZ();

        entity.setVelocity(velocity.toImmutable());
    }

    public SoundEvent getWingSound() {
        return player.getSpecies() == Race.CHANGELING ? USounds.CHANGELING_BUZZ : USounds.WING_FLAP;
    }

    protected void moveFlying(Entity player, MutableVector velocity) {

        float forward = 0.000015F * flightExperience * (float)Math.sqrt(getHorizontalMotion(player));
        int factor = gravity < 0 ? -1 : 1;
        boolean sneak = !player.isSneaking();

        // vertical drop due to gravity
        if (sneak) {
            velocity.y -= (0.005F - getHorizontalMotion(player) / 100) * factor;
        } else {
            forward += 0.005F;
            velocity.y -= 0.0001F * factor;
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

            //player.knockBack(player, forward, 1, 1);
            velocity.x += - forward * MathHelper.sin((player.yaw + glance) * 0.017453292F);
            velocity.z += forward * MathHelper.cos((player.yaw + glance) * 0.017453292F);
        }
    }

    protected double getHorizontalMotion(Entity e) {
        double motionX = e.getX() - lastTickPosX;
        double motionZ = e.getZ() - lastTickPosZ;

        return (motionX * motionX)
               + (motionZ * motionZ);
    }

    protected SoundEvent getFallSound(int distance) {
        return distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }

    private void addFlightExperience(PlayerEntity entity, float factor) {
        float maximumGain = MAXIMUM_FLIGHT_EXPERIENCE - flightExperience;
        float gainSteps = 20;

        flightExperience = Math.max(0, flightExperience + factor * maximumGain / gainSteps);
    }

    public void updateFlightStat(PlayerEntity entity, boolean flying) {
        entity.abilities.allowFlying = checkCanFly(Pony.of(entity));

        if (entity.abilities.allowFlying) {
            entity.abilities.flying |= flying;

            isFlying = entity.abilities.flying;

            if (isFlying) {
                ticksNextLevel = 0;
            }
        } else {
            entity.abilities.flying = false;
            isFlying = false;
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putInt("flightDuration", ticksNextLevel);
        compound.putFloat("flightExperience", flightExperience);
        compound.putBoolean("isFlying", isFlying);
        compound.putBoolean("isRainbooming", isRainbooming);

        if (gravity != 0) {
            compound.putFloat("gravity", gravity);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        ticksNextLevel = compound.getInt("flightDuration");
        flightExperience = compound.getFloat("flightExperience");
        isFlying = compound.getBoolean("isFlying");
        isRainbooming = compound.getBoolean("isRainbooming");

        if (compound.contains("gravity")) {
            gravity = compound.getFloat("gravity");
        } else {
            gravity = 0;
        }
    }

    @Override
    public boolean isFlying() {
        return isFlying;
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
