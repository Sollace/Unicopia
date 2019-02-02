package com.minelittlepony.unicopia.player;

import javax.annotation.Nullable;

import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.power.IData;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.unicopia.power.PowersRegistry;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

class PlayerAbilityDelegate implements IAbilityReceiver, IUpdatable<EntityPlayer>, InbtSerialisable {

    private final IPlayer player;

    private int warmup = 0;

    private int cooldown = 0;

    @Nullable
    private IPower<?> activeAbility = null;

    public PlayerAbilityDelegate(IPlayer player) {
        this.player = player;
    }

    boolean canSwitchStates() {
        return (warmup == 0 && cooldown == 0) || activeAbility == null;
    }

    @Override
    public synchronized void tryUseAbility(IPower<?> power) {
        if (canSwitchStates() || activeAbility != power) {
            activeAbility = power;
            warmup = power.getWarmupTime(player);
            cooldown = 0;
        }
    }

    @Override
    public synchronized void tryClearAbility() {
        if (canSwitchStates()) {
            activeAbility = null;
            warmup = 0;
            cooldown = 0;
        }
    }

    @Override
    public int getRemainingCooldown() {
        return cooldown;
    }

    @Override
    public synchronized void onUpdate(EntityPlayer entity) {
        if (activeAbility != null && activeAbility.canUse(player.getPlayerSpecies())) {
            if (warmup > 0) {
                warmup--;
                activeAbility.preApply(player);
            } else if (player.isClientPlayer()) {
                if (activateAbility()) {
                    cooldown = activeAbility.getCooldownTime(player);
                } else {
                    cooldown = 0;
                }
            }

            if (cooldown > 0 && activeAbility != null) {
                cooldown--;
                activeAbility.postApply(player);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("warmup", warmup);
        compound.setInteger("cooldown", cooldown);

        if (activeAbility != null) {
            compound.setString("activeAbility", activeAbility.getKeyName());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        warmup = compound.getInteger("warmup");
        cooldown = compound.getInteger("cooldown");
        activeAbility = null;

        if (compound.hasKey("activeAbility")) {
            PowersRegistry.instance().getPowerFromName(compound.getString("activeAbility")).ifPresent(p -> {
                activeAbility = p;
            });
        }
    }

    protected boolean activateAbility() {
        if (activeAbility == null || !activeAbility.canActivate(player.getWorld(), player)) {
            return false;
        }

        IData data = activeAbility.tryActivate(player.getOwner(), player.getWorld());

        if (data != null) {
            Unicopia.channel.send(new MsgPlayerAbility(player.getOwner(), activeAbility, data), Target.SERVER);
        }

        return data != null;
    }
}
