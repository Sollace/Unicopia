package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.InbtSerialisable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

class PlayerGravityDelegate implements IUpdatable<EntityPlayer>, InbtSerialisable {

    private final IPlayer player;

    private static final float MAXIMUM_FLIGHT_EXPERIENCE = 500;

    private int ticksInAir = 0;
    private float flightExperience = 0;

    public boolean isFlying = false;

    public PlayerGravityDelegate(IPlayer player) {
        this.player = player;
    }

    @Override
    public void onUpdate(EntityPlayer entity) {
        entity.capabilities.allowFlying = entity.capabilities.isCreativeMode || player.getPlayerSpecies().canFly();

        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.isFlying |= entity.capabilities.allowFlying && isFlying && !entity.onGround;
        }

        isFlying = entity.capabilities.isFlying;

        if (!entity.capabilities.isCreativeMode && !entity.isElytraFlying()) {
            if (isFlying && !entity.isRiding()) {

                entity.fallDistance = 0;

                float exhaustion = (0.2F * ticksInAir++) / 100;
                if (entity.isSprinting()) {
                    exhaustion *= 3.11F;
                }

                entity.addExhaustion(exhaustion * (1 - flightExperience));

                if (ticksInAir > 2000) {
                    ticksInAir = 1;
                    addFlightExperience(entity);
                    entity.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, 1, 1);
                }

                float forward = 0.00015F * flightExperience;

                entity.motionX += - forward * MathHelper.sin(entity.rotationYaw * 0.017453292F);
                entity.motionY -= 0.05F - ((entity.motionX * entity.motionX) + (entity.motionZ + entity.motionZ)) / 100;
                entity.motionZ += forward * MathHelper.cos(entity.rotationYaw * 0.017453292F);
            } else {
                ticksInAir = 0;
            }
        }
    }

    public void landHard(EntityPlayer player, float distance, float damageMultiplier) {
        if (distance <= 0) {
            return;
        }

        PotionEffect potioneffect = player.getActivePotionEffect(MobEffects.JUMP_BOOST);
        float potion = potioneffect != null ? potioneffect.getAmplifier() + 1 : 0;
        int i = MathHelper.ceil((distance - 8.0F - potion) * damageMultiplier);

        if (i > 0) {
            int j = MathHelper.floor(player.posX);
            int k = MathHelper.floor(player.posY - 0.20000000298023224D);
            int l = MathHelper.floor(player.posZ);

            BlockPos pos = new BlockPos(j, k, l);

            IBlockState state = player.world.getBlockState(pos);
            Block block = state.getBlock();

            if (state.getMaterial() != Material.AIR) {

                player.playSound(getFallSound(i), 1, 1);
                player.attackEntityFrom(DamageSource.FALL, i);

                SoundType soundtype = block.getSoundType(state, player.getEntityWorld(), pos, player);

                player.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.5f, soundtype.getPitch() * 0.75f);
            }
        }
    }

    protected SoundEvent getFallSound(int distance) {
        return distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }

    private void addFlightExperience(EntityPlayer entity) {
        entity.addExperience(1);

        flightExperience += (MAXIMUM_FLIGHT_EXPERIENCE - flightExperience) / 20;
    }

    public void updateFlightStat(EntityPlayer entity, boolean flying) {
        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.allowFlying = player.getPlayerSpecies().canFly();

            if (entity.capabilities.allowFlying) {
                entity.capabilities.isFlying |= flying;

                isFlying = entity.capabilities.isFlying;

                if (isFlying) {
                    ticksInAir = 0;
                }

            } else {
                entity.capabilities.isFlying = false;
                isFlying = false;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("flightDuration", ticksInAir);
        compound.setFloat("flightExperience", flightExperience);
        compound.setBoolean("isFlying", isFlying);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ticksInAir = compound.getInteger("flightDuration");
        flightExperience = compound.getFloat("flightExperience");
        isFlying = compound.getBoolean("isFlying");
    }
}
