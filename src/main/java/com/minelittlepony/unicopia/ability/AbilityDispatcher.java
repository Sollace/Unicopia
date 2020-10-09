package com.minelittlepony.unicopia.ability;

import java.util.Collections;
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

    @Nullable
    private Race prevRace;
    private long maxPage;

    public AbilityDispatcher(Pony player) {
        this.player = player;
    }

    public void clear(AbilitySlot slot) {
        Stat stat = getStat(slot);

        if (stat.canSwitchStates()) {
            stat.setActiveAbility(null);
        }
    }

    public void activate(AbilitySlot slot, long page) {
        Stat stat = getStat(slot);
        if (stat.canSwitchStates()) {
            stat.getAbility(page).ifPresent(stat::setActiveAbility);
        }
    }

    public Stat getStat(AbilitySlot slot) {
        return stats.computeIfAbsent(slot, Stat::new);
    }

    public long getMaxPage() {
        if (prevRace != player.getSpecies()) {
            prevRace = player.getSpecies();
            maxPage = Math.max(0, stats.values().stream().mapToLong(Stat::getMaxPage).reduce(0, Math::max) - 1);
        }
        return maxPage;
    }

    @Override
    public void tick() {
        stats.values().forEach(Stat::tick);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        if (compound.contains("stats")) {
            stats.clear();
            CompoundTag li = compound.getCompound("stats");
            li.getKeys().forEach(key -> {
                getStat(AbilitySlot.valueOf(key)).fromNBT(li.getCompound(key));
            });
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        CompoundTag li = new CompoundTag();
        stats.forEach((key, value) -> li.put(key.name(), value.toNBT()));
        compound.put("stats", li);
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

        /**
         * True once the current ability has been triggered.
         */
        private boolean triggered;

        private Optional<Ability<?>> activeAbility = Optional.empty();

        private Stat(AbilitySlot slot) {
            this.slot = slot;
        }

        /**
         * Returns true if the current ability can we swapped out.
         */
        boolean canSwitchStates() {
            return !activeAbility.isPresent() || (warmup != 0) || (triggered && cooldown == 0);
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

        public void tick() {
            getActiveAbility().ifPresent(this::activate);
        }

        private <T extends Hit> void activate(Ability<T> ability) {
            if (warmup > 0) {
                warmup--;
                ability.preApply(player, slot);
                return;
            }

            if (cooldown > 0 && cooldown-- > 0) {
                ability.postApply(player, slot);

                if (cooldown <= 0) {
                    setActiveAbility(null);
                }
                return;
            }

            if (triggered) {
                return;
            }

            if (ability.canActivate(player.getWorld(), player)) {
                triggered = true;
                setCooldown(ability.getCooldownTime(player));

                if (player.isClientPlayer()) {
                    T data = ability.tryActivate(player);

                    if (data != null) {
                        Channel.CLIENT_PLAYER_ABILITY.send(new MsgPlayerAbility<>(ability, data));
                    } else {
                        setCooldown(0);
                    }
                }
            }

            if (cooldown <= 0) {
                setActiveAbility(null);
            }
        }

        public Optional<Ability<?>> getAbility(long page) {
            Race race = player.getSpecies();
            return Abilities.BY_SLOT.computeIfAbsent(slot, c -> Collections.emptySet())
                    .stream()
                    .filter(a -> a.canUse(race))
                    .skip(page)
                    .findFirst();
        }

        public long getMaxPage() {
            Race race = player.getSpecies();
            return Abilities.BY_SLOT.computeIfAbsent(slot, c -> Collections.emptySet())
                    .stream()
                    .filter(a -> a.canUse(race))
                    .count();
        }

        protected synchronized void setActiveAbility(Ability<?> power) {
            if (activeAbility.orElse(null) != power) {
                triggered = false;
                activeAbility = Optional.ofNullable(power);
                setWarmup(activeAbility.map(p -> p.getWarmupTime(player)).orElse(0));
                setCooldown(0);
            }
        }

        protected synchronized Optional<Ability<?>> getActiveAbility() {
            return activeAbility.filter(ability -> {
                return (!(ability == null || (triggered && warmup == 0 && cooldown == 0)) && ability.canUse(player.getSpecies()));
            });
        }

        @Override
        public void toNBT(CompoundTag compound) {
            compound.putInt("warmup", warmup);
            compound.putInt("cooldown", cooldown);
            compound.putInt("maxWarmup", maxWarmup);
            compound.putInt("maxCooldown", maxCooldown);
            compound.putBoolean("triggered", triggered);
            getActiveAbility().ifPresent(ability -> {
                compound.putString("activeAbility", Abilities.REGISTRY.getId(ability).toString());
            });
        }

        @Override
        public void fromNBT(CompoundTag compound) {
            warmup = compound.getInt("warmup");
            cooldown = compound.getInt("cooldown");
            maxWarmup = compound.getInt("maxWarmup");
            maxCooldown = compound.getInt("maxCooldown");
            triggered = compound.getBoolean("triggered");
            activeAbility = Abilities.REGISTRY.getOrEmpty(new Identifier(compound.getString("activeAbility")));
        }
    }
}
