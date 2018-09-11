package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.InbtSerialisable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

class PlayerGravityDelegate implements IUpdatable, InbtSerialisable {

    private final IPlayer player;

    private int ticksSinceLanding = 0;

    public boolean isFlying = false;

    public PlayerGravityDelegate(IPlayer player) {
        this.player = player;
    }

    @Override
    public void onUpdate(EntityPlayer entity) {
        if (!entity.capabilities.isCreativeMode) {
            if (player.getPlayerSpecies().canFly()) {
                if (ticksSinceLanding < 2) {
                    ticksSinceLanding++;
                }

                entity.capabilities.allowFlying = entity.capabilities.isFlying = false;
            }
        }

        if (entity.capabilities.isFlying) {
            entity.fallDistance = 0;
        }
    }

    public void updateFlightStat(EntityPlayer entity, boolean flying) {
        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.allowFlying = player.getPlayerSpecies().canFly();

            if (entity.capabilities.allowFlying) {
                entity.capabilities.isFlying |= flying;

                isFlying = entity.capabilities.isFlying;

                if (isFlying) {
                    ticksSinceLanding = 0;
                }

            } else {
                entity.capabilities.isFlying = false;
                isFlying = false;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("ticksOnGround", ticksSinceLanding);
        compound.setBoolean("isFlying", isFlying);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ticksSinceLanding = compound.getInteger("ticksOnGround");
        isFlying = compound.getBoolean("isFlying");
    }
}
