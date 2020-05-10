package com.minelittlepony.unicopia.ability;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;

public class AbilityDispatcher implements Tickable, NbtSerialisable {

    private final Pony player;

    private final Map<AbilitySlot, Stat> stats = new EnumMap<>(AbilitySlot.class);

    /**
     * True once the current ability has been triggered.
     */
    private boolean triggered;

    private Optional<Ability<?>> activeAbility = Optional.empty();

    private AbilitySlot activeSlot = AbilitySlot.NONE;

    public AbilityDispatcher(Pony player) {
        this.player = player;
    }

    /**
     * Returns true if the current ability can we swapped out.
     */
    boolean canSwitchStates() {
        return !activeAbility.isPresent() || getStat(getActiveSlot()).canSwitchStates();
    }

    public AbilitySlot getActiveSlot() {
        return activeSlot;
    }

    public void cancelAbility(AbilitySlot slot) {
        if (getActiveSlot() == slot && canSwitchStates()) {
            setActiveAbility(slot, null);
        }
    }

    public void activate(AbilitySlot slot) {
        if (canSwitchStates()) {
            getAbility(slot).ifPresent(ability -> setActiveAbility(slot, ability));
        }
    }

    public Stat getStat(AbilitySlot slot) {
        return stats.computeIfAbsent(slot, Stat::new);
    }

    public Optional<Ability<?>> getAbility(AbilitySlot slot) {
        Race race = player.getSpecies();
        return Abilities.BY_SLOT.get(slot).stream().filter(a -> a.canUse(race)).findFirst();
    }

    protected synchronized void setActiveAbility(AbilitySlot slot, Ability<?> power) {
        if (activeAbility.orElse(null) != power) {
            activeSlot = slot;
            triggered = false;
            activeAbility = Optional.ofNullable(power);
            Stat stat = getStat(slot);
            stat.setWarmup(activeAbility.map(p -> p.getWarmupTime(player)).orElse(0));
            stat.setCooldown(0);
        }
    }

    @Nullable
    protected synchronized Optional<Ability<?>> getActiveAbility() {
        Stat stat = getStat(getActiveSlot());
        return activeAbility.filter(ability -> {
            return (!(ability == null || (triggered && stat.warmup == 0 && stat.cooldown == 0)) && ability.canUse(player.getSpecies()));
        });
    }

    @Override
    public void tick() {
        getActiveAbility().ifPresent(this::activate);
    }

    private <T extends Hit> void activate(Ability<T> ability) {
        Stat stat = getStat(getActiveSlot());

        stats.values().forEach(s -> {
            if (s != stat) {
                s.idle();
            }
        });

        if (stat.warmup > 0) {
            stat.warmup--;
            System.out.println("warming up");
            ability.preApply(player);
            return;
        }

        if (stat.tickInactive()) {
            System.out.println("cooling down");
            ability.postApply(player);

            if (stat.cooldown <= 0) {
                setActiveAbility(AbilitySlot.NONE, null);
            }
            return;
        }

        if (triggered) {
            return;
        }

        if (ability.canActivate(player.getWorld(), player)) {
            triggered = true;
            stat.setCooldown(ability.getCooldownTime(player));

            if (player.isClientPlayer()) {
                T data = ability.tryActivate(player);

                if (data != null) {
                    Channel.PLAYER_ABILITY.send(new MsgPlayerAbility<>(ability, data));
                } else {
                    stat.setCooldown(0);
                }
            }
        }

        if (stat.cooldown <= 0) {
            setActiveAbility(AbilitySlot.NONE, null);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putBoolean("triggered", triggered);
        if (compound.contains("stats")) {
            stats.clear();
            CompoundTag li = compound.getCompound("stats");
            li.getKeys().forEach(key -> {
                getStat(AbilitySlot.valueOf(key)).fromNBT(li.getCompound(key));
            });
        }
        compound.putInt("activeSlot", activeSlot.ordinal());
        getActiveAbility().ifPresent(ability -> {
            compound.putString("activeAbility", Abilities.REGISTRY.getId(ability).toString());
        });
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        triggered = compound.getBoolean("triggered");
        CompoundTag li = new CompoundTag();
        stats.forEach((key, value) -> li.put(key.name(), value.toNBT()));
        compound.put("stats", li);
        activeSlot = compound.contains("activeSlot") ? AbilitySlot.values()[compound.getInt("activeSlot")] : activeSlot;
        activeAbility = Abilities.REGISTRY.getOrEmpty(new Identifier(compound.getString("activeAbility")));
    }

    public class Stat implements NbtSerialisable {

        /**
         * Ticks of warmup before an ability is triggered.
         */
        private int warmup;
        private int maxWarmup;

        /**
         * Ticks of cooldown after an ability has been triggered.
         */
        private int cooldown;
        private int maxCooldown;

        public final AbilitySlot slot;

        private Stat(AbilitySlot slot) {
            this.slot = slot;
        }

        /**
         * Returns true if the current ability can we swapped out.
         */
        boolean canSwitchStates() {
            return (warmup != 0) || (triggered && cooldown == 0);
        }

        public int getRemainingCooldown() {
            return cooldown;
        }

        public float getFillProgress() {
            float cooldown = getWarmup();
            if (cooldown <= 0 || cooldown >= 1) {
                return getCooldown();
            }
            return 1 - cooldown;
        }

        public float getCooldown() {
            return maxCooldown <= 0 ? 0 : ((float)cooldown / (float)maxCooldown);
        }

        public void setCooldown(int value) {
            cooldown = value;
            maxCooldown = value;
        }

        public float getWarmup() {
            return maxWarmup <= 0 ? 0 : ((float)warmup / (float)maxWarmup);
        }

        public void setWarmup(int value) {
            maxWarmup = value;
            warmup = value;
        }

        public void idle() {
            if (warmup > 0) {
                warmup--;
            }
            if (cooldown > 0) {
                cooldown--;
            }
        }

        public boolean tickInactive() {
            return cooldown > 0 && cooldown-- > 0;
        }

        @Override
        public void toNBT(CompoundTag compound) {
            compound.putInt("warmup", warmup);
            compound.putInt("cooldown", cooldown);
            compound.putInt("maxWarmup", maxWarmup);
            compound.putInt("maxCooldown", maxCooldown);
        }

        @Override
        public void fromNBT(CompoundTag compound) {
            warmup = compound.getInt("warmup");
            cooldown = compound.getInt("cooldown");
            maxWarmup = compound.getInt("maxWarmup");
            maxCooldown = compound.getInt("maxCooldown");
        }
    }
}
