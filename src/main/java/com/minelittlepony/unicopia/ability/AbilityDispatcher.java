package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Updatable;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class AbilityDispatcher implements Updatable, NbtSerialisable {

    private final Pony player;

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

    private Optional<Ability<?>> activeAbility = Optional.empty();

    public AbilityDispatcher(Pony player) {
        this.player = player;
    }

    /**
     * Returns true if the currrent ability can we swapped out.
     */
    boolean canSwitchStates() {
        return !activeAbility.isPresent() || (warmup != 0) || (triggered && cooldown == 0);
    }

    public void tryUseAbility(Ability<?> power) {
        if (canSwitchStates()) {
            setAbility(power);
        }
    }

    public void tryClearAbility() {
        if (canSwitchStates()) {
            setAbility(null);
        }
    }

    protected synchronized void setAbility(Ability<?> power) {
        if (activeAbility.orElse(null) != power) {
            triggered = false;
            activeAbility = Optional.ofNullable(power);
            warmup = activeAbility.map(p -> p.getWarmupTime(player)).orElse(0);
            cooldown = 0;
        }
    }

    @Nullable
    protected synchronized Optional<Ability<?>> getUsableAbility() {
        return activeAbility.filter(ability -> {
            return (!(ability == null || (triggered && warmup == 0 && cooldown == 0)) && ability.canUse(player.getSpecies()));
        });
    }

    public int getRemainingCooldown() {
        return cooldown;
    }

    @Override
    public void onUpdate() {
        getUsableAbility().ifPresent(this::activate);
    }

    private <T extends Hit> void activate(Ability<T> ability) {
        if (warmup > 0) {
            warmup--;
            System.out.println("warming up");
            ability.preApply(player);
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            System.out.println("cooling down");
            ability.postApply(player);

            if (cooldown <= 0) {
                setAbility(null);
            }
            return;
        }

        if (triggered) {
            return;
        }

        if (ability.canActivate(player.getWorld(), player)) {
            triggered = true;
            cooldown = ability.getCooldownTime(player);

            if (player.isClientPlayer()) {
                T data = ability.tryActivate(player);

                if (data != null) {
                    Channel.PLAYER_ABILITY.send(new MsgPlayerAbility<>(ability, data));
                } else {
                    cooldown = 0;
                }
            }
        }

        if (cooldown <= 0) {
            setAbility(null);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putBoolean("triggered", triggered);
        compound.putInt("warmup", warmup);
        compound.putInt("cooldown", cooldown);
        getUsableAbility().ifPresent(ability -> {
            compound.putString("activeAbility", Abilities.REGISTRY.getId(ability).toString());
        });
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        triggered = compound.getBoolean("triggered");
        warmup = compound.getInt("warmup");
        cooldown = compound.getInt("cooldown");
        activeAbility = Abilities.REGISTRY.getOrEmpty(new Identifier(compound.getString("activeAbility")));
    }
}
