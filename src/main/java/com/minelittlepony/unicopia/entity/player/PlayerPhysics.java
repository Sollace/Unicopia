package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.FlightPredicate;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PlayerPhysics extends EntityPhysics<Pony> implements Tickable, Motion, NbtSerialisable {

    private int ticksInAir;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

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
    public void tick() {
        PlayerEntity entity = pony.getOwner();

        if (isGravityNegative() && !entity.isSneaking() && entity.isInSneakingPose()) {
            float currentHeight = entity.getDimensions(entity.getPose()).height;
            float sneakingHeight = entity.getDimensions(EntityPose.STANDING).height;

            entity.setPos(entity.getX(), entity.getY() + currentHeight - sneakingHeight, entity.getZ());
            entity.setPose(EntityPose.STANDING);
        }

        MutableVector velocity = new MutableVector(entity.getVelocity());

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

        if (!creative && !entity.isFallFlying() && isFlyingSurvival && !entity.hasVehicle()) {

            entity.fallDistance = 0;

            if (ticksInAir > 100) {
                Bar mana = pony.getMagicalReserves().getMana();

                mana.add((int)(-getHorizontalMotion(entity) * 100));

                if (mana.getPercentFill() < 0.2) {
                    pony.getMagicalReserves().getExertion().add(2);
                    pony.getMagicalReserves().getEnergy().add(2);

                    if (mana.getPercentFill() < 0.1 && ticksInAir % 10 == 0) {
                        float exhaustion = (0.3F * ticksInAir) / 70;
                        if (entity.isSprinting()) {
                            exhaustion *= 3.11F;
                        }

                        entity.addExhaustion(exhaustion);
                    }
                }
            }

            moveFlying(entity, velocity);

            if (ticksInAir++ > 0 && ticksInAir % 30 == 0) {
                entity.playSound(getWingSound(), 0.5F, 1);
            }
        } else {
            ticksInAir = 0;
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

    protected void moveFlying(Entity player, MutableVector velocity) {

        float forward = 0.000015F * (float)Math.sqrt(getHorizontalMotion(player));
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

    public void updateFlightStat(boolean flying) {
        PlayerEntity entity = pony.getOwner();

        entity.abilities.allowFlying = checkCanFly();

        if (entity.abilities.allowFlying) {
            entity.abilities.flying |= flying;

            isFlyingSurvival = entity.abilities.flying;
        } else {
            entity.abilities.flying = false;
            isFlyingSurvival = false;
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        isFlyingSurvival = compound.getBoolean("isFlying");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        ticksInAir = compound.getInt("ticksInAir");

        pony.getOwner().calculateDimensions();
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival;
    }
}
