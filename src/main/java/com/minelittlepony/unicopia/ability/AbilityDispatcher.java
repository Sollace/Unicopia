package com.minelittlepony.unicopia.ability;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class AbilityDispatcher implements Tickable, NbtSerialisable {

    private final Pony player;

    private final Map<AbilitySlot, Stat> stats = new EnumMap<>(AbilitySlot.class);

    @Nullable
    private Race prevRace;
    private long maxPage = -1;

    public AbilityDispatcher(Pony player) {
        this.player = player;
    }

    public void clear(AbilitySlot slot, ActivationType pressType, long page) {
        Stat stat = getStat(slot);

        if (stat.canSwitchStates()) {
            if (pressType == ActivationType.NONE || stat.getAbility(page).filter(ability -> !triggerQuickAction(ability, pressType)).isEmpty()) {
                stat.setActiveAbility(null);
            }
        }
    }

    private boolean triggerQuickAction(Ability<?> ability, ActivationType pressType) {
        if (ability.onQuickAction(player, pressType)) {
            Channel.CLIENT_PLAYER_ABILITY.send(new MsgPlayerAbility<>(ability, null, pressType));
            return true;
        }
        return false;
    }

    public Optional<Ability<?>> activate(AbilitySlot slot, long page) {
        Stat stat = getStat(slot);
        if (stat.canSwitchStates()) {
            return stat.getAbility(page).flatMap(stat::setActiveAbility);
        }
        return Optional.empty();
    }

    public Collection<Stat> getStats() {
        return stats.values();
    }

    public Stat getStat(AbilitySlot slot) {
        return stats.computeIfAbsent(slot, Stat::new);
    }

    public long getMaxPage() {
        if (maxPage < 0 || prevRace != player.getSpecies()) {
            prevRace = player.getSpecies();
            maxPage = 0;
            for (AbilitySlot slot : AbilitySlot.values()) {
                maxPage = Math.max(maxPage, getStat(slot).getMaxPage() - 1);
            }
        }
        return maxPage;
    }

    @Override
    public void tick() {
        stats.values().forEach(Stat::tick);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        if (compound.contains("stats")) {
            stats.clear();
            NbtCompound li = compound.getCompound("stats");
            li.getKeys().forEach(key -> {
                getStat(AbilitySlot.valueOf(key)).fromNBT(li.getCompound(key));
            });
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        NbtCompound li = new NbtCompound();
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

        public double getCost(long page) {
            if (warmup <= 0) {
                return 0;
            }
            return getAbility(page).map(ability -> ability.getCostEstimate(player)).orElse(0D);
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
                        Channel.CLIENT_PLAYER_ABILITY.send(new MsgPlayerAbility<>(ability, data, ActivationType.NONE));
                    } else {
                        player.getEntity().playSound(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
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
            List<Ability<?>> found = Abilities.BY_SLOT.computeIfAbsent(slot, c -> Collections.emptySet())
                    .stream()
                    .filter(a -> a.canUse(race))
                    .collect(Collectors.toList());
            if (found.isEmpty()) {
                return Optional.empty();
            }
            page = Math.min(found.size() - 1, page);

            return Optional.ofNullable(found.get((int)page));
        }

        public long getMaxPage() {
            Race race = player.getSpecies();
            return Abilities.BY_SLOT.computeIfAbsent(slot, c -> Collections.emptySet())
                    .stream()
                    .filter(a -> a.canUse(race))
                    .count();
        }

        protected synchronized Optional<Ability<?>> setActiveAbility(@Nullable Ability<?> power) {
            if (activeAbility.orElse(null) != power) {
                triggered = false;
                activeAbility = Optional.ofNullable(power);
                setWarmup(activeAbility.map(p -> p.getWarmupTime(player)).orElse(0));
                setCooldown(0);
                return activeAbility;
            }
            return Optional.empty();
        }

        protected synchronized Optional<Ability<?>> getActiveAbility() {
            return activeAbility.filter(ability -> {
                return (!(ability == null || (triggered && warmup == 0 && cooldown == 0)) && ability.canUse(player.getSpecies()));
            });
        }

        @Override
        public void toNBT(NbtCompound compound) {
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
        public void fromNBT(NbtCompound compound) {
            warmup = compound.getInt("warmup");
            cooldown = compound.getInt("cooldown");
            maxWarmup = compound.getInt("maxWarmup");
            maxCooldown = compound.getInt("maxCooldown");
            triggered = compound.getBoolean("triggered");
            activeAbility = Abilities.REGISTRY.getOrEmpty(new Identifier(compound.getString("activeAbility")));
        }
    }
}
