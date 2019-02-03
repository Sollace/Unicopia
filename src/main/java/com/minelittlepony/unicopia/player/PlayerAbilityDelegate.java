package com.minelittlepony.unicopia.player;

import javax.annotation.Nonnull;
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

    /**
     * Ticks of warmup before an ability is triggered.
     */
    private int warmup;

    /**
     * Ticks of cooldown after an ability has been triggered.
     */
    private int cooldown;

    /**
     * True once the current ability has been triggered.
     */
    private boolean triggered;

    @Nullable
    private IPower<?> activeAbility = null;

    public PlayerAbilityDelegate(IPlayer player) {
        this.player = player;
    }

    /**
     * Returns true if the currrent ability can we swapped out.
     */
    boolean canSwitchStates() {
        return activeAbility == null || (warmup != 0) || (triggered && cooldown == 0);
    }

    @Override
    public void tryUseAbility(IPower<?> power) {
        if (canSwitchStates()) {
            setAbility(power);
        }
    }

    @Override
    public void tryClearAbility() {
        if (canSwitchStates()) {
            setAbility(null);
        }
    }

    protected synchronized void setAbility(@Nullable IPower<?> power) {
        if (activeAbility != power) {
            triggered = false;
            activeAbility = power;
            warmup = power == null ? 0 : power.getWarmupTime(player);
            cooldown = 0;
        }
    }

    @Nullable
    protected synchronized IPower<?> getUsableAbility() {
        if (!(activeAbility == null || (triggered && warmup == 0 && cooldown == 0)) && activeAbility.canUse(player.getPlayerSpecies())) {
            return activeAbility;
        }
        return null;
    }

    @Override
    public int getRemainingCooldown() {
        return cooldown;
    }

    @Override
    public void onUpdate(EntityPlayer entity) {
        IPower<?> ability = getUsableAbility();

        if (ability == null) {
            return;
        }

        if (warmup > 0) {
            warmup--;
            ability.preApply(player);
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            ability.postApply(player);
            return;
        }

        if (triggered) {
            return;
        }

        if (ability.canActivate(player.getWorld(), player)) {
            triggered = true;
            cooldown = ability.getCooldownTime(player);

            if (player.isClientPlayer()) {
                activateAbility(ability);
            }
        }

        if (cooldown <= 0) {
            setAbility(null);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("triggered", triggered);
        compound.setInteger("warmup", warmup);
        compound.setInteger("cooldown", cooldown);

        IPower<?> ability = getUsableAbility();

        if (ability != null) {
            compound.setString("activeAbility", ability.getKeyName());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        activeAbility = null;

        triggered = compound.getBoolean("triggered");
        warmup = compound.getInteger("warmup");
        cooldown = compound.getInteger("cooldown");

        if (compound.hasKey("activeAbility")) {
            PowersRegistry.instance()
                .getPowerFromName(compound.getString("activeAbility"))
                .ifPresent(p -> activeAbility = p);
        }
    }

    /**
     * Attempts to activate the current stored ability.
     * Returns true if the ability suceeded, otherwise false.
     */
    protected boolean activateAbility(@Nonnull IPower<?> ability) {
        IData data = ability.tryActivate(player);

        if (data != null) {
            Unicopia.channel.send(new MsgPlayerAbility(player.getOwner(), ability, data), Target.SERVER);
        }

        return data != null;
    }
}
