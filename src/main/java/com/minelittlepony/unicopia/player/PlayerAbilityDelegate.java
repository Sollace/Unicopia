package com.minelittlepony.unicopia.player;

import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.power.IData;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.unicopia.power.PowersRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

class PlayerAbilityDelegate implements IAbilityReceiver, IUpdatable, InbtSerialisable {

    private final IPlayer player;

    private boolean abilityTriggered = false;

    private int warmup = 0;

    private int cooldown = 0;

    private IPower<?> activeAbility = null;

    public PlayerAbilityDelegate(IPlayer player) {
        this.player = player;
    }

    boolean canSwitchStates() {
        return abilityTriggered && cooldown <= 0;
    }

    @Override
    public void tryUseAbility(IPower<?> power) {
        if (canSwitchStates() || activeAbility != power) {
            abilityTriggered = false;
            activeAbility = power;
            warmup = 0;
            cooldown = power.getCooldownTime(player);
        }
    }

    @Override
    public void tryClearAbility() {
        if (canSwitchStates() || activeAbility != null) {
            abilityTriggered = false;
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
    public void onUpdate(EntityPlayer entity) {
        if (activeAbility != null && activeAbility.canUse(player.getPlayerSpecies())) {
            if (!abilityTriggered) {
                if (warmup < activeAbility.getWarmupTime(player)) {
                    activeAbility.preApply(entity);
                    warmup++;
                } else if (player.isClientPlayer()) {
                    if (activeAbility.canActivate(entity.getEntityWorld(), player)) {
                        abilityTriggered = activateAbility(entity);
                        if (!abilityTriggered) {
                            activeAbility = null;
                            cooldown = 0;
                        }
                    } else {
                        activeAbility = null;
                        cooldown = 0;
                    }
                }
            } else if (cooldown > 0) {
                activeAbility.postApply(entity);
                cooldown--;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("triggered", abilityTriggered);
        compound.setInteger("warmup", warmup);
        compound.setInteger("cooldown", cooldown);

        if (activeAbility != null) {
            compound.setString("activeAbility", activeAbility.getKeyName());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        activeAbility = null;
        abilityTriggered = compound.getBoolean("triggered");
        warmup = compound.getInteger("warmup");
        cooldown = compound.getInteger("cooldown");

        if (compound.hasKey("activeAbility")) {
            PowersRegistry.instance().getPowerFromName(compound.getString("activeAbility")).ifPresent(p -> {
                activeAbility = p;
            });
        }
    }

    protected boolean activateAbility(EntityPlayer entity) {
        IData data = activeAbility.tryActivate(entity, entity.getEntityWorld());

        if (data != null) {
            Unicopia.channel.send(new MsgPlayerAbility(activeAbility, data), Target.SERVER);
        }

        return data != null;
    }
}
