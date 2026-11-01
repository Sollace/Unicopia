package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgConfigurationChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public final class AllowList {
    public static final AllowList INSTANCE = new AllowList();

    private AllowList() { }

    public boolean disable(@Nullable MinecraftServer server) {
        if (!isEnabled()) {
            return false;
        }
        update(server, Set.of());
        return true;
    }

    public boolean isEnabled() {
        return !entries().isEmpty();
    }

    public boolean add(@Nullable MinecraftServer server, Race race) {
        if (race.isUnset() || race.isHuman()) {
            return false;
        }
        Set<Identifier> values = new HashSet<>(Unicopia.getConfig().speciesWhiteList.get());
        boolean added = values.add(race.getId());
        update(server, values);
        return added;
    }

    public boolean remove(@Nullable MinecraftServer server, Race race) {
        Set<Identifier> values = new HashSet<>(Unicopia.getConfig().speciesWhiteList.get());
        if (values.isEmpty()) {
            for (Race r : Race.REGISTRY) {
                if (!r.isUnset() && r != race) {
                    values.add(r.getId());
                }
            }

            update(server, values);
            return true;
        }
        boolean removed = values.remove(race.getId());
        update(server, values);
        return removed;
    }

    public boolean permits(Race race) {
        return isAlwaysPermitted(race)
                || !isEnabled()
                || entries().contains(race.getId());
    }

    public static boolean isAlwaysPermitted(Race race) {
        return race.isUnset() || race.isHuman();
    }

    private void update(@Nullable MinecraftServer server, Set<Identifier> values) {
        // Filters out entries that shouldn't be there and remove unknown ids
        values = values.stream().filter(r -> Race.REGISTRY.getOptionalValue(r).filter(o -> !isAlwaysPermitted(o)).isPresent()).collect(Collectors.toUnmodifiableSet());
        if (getExcluded(getValues(values)) == 0) {
            values = Set.of();
        }

        if (!values.equals(Unicopia.getConfig().speciesWhiteList.get())) {
            Unicopia.getConfig().speciesWhiteList.set(values);
            Unicopia.getConfig().save();
        }
        if (server != null) {
            MsgConfigurationChange msg = new MsgConfigurationChange(Unicopia.getConfig().toSynced());
            InteractionManager.getInstance().setSyncedConfig(msg.config());
            server.getPlayerManager().getPlayerList().forEach(recipient -> {
                Channel.CONFIGURATION_CHANGE.sendToPlayer(msg, recipient);
            });
        }
    }

    private static Set<Race> getValues(Set<Identifier> permitted) {
        return permitted.stream().flatMap(id -> Race.REGISTRY.getOptionalValue(id).stream()).collect(Collectors.toUnmodifiableSet());
    }

    private static long getExcluded(Set<Race> permitted) {
        return Race.REGISTRY.stream().filter(r -> !isAlwaysPermitted(r) && !permitted.contains(r)).count();
    }

    public Set<Identifier> entries() {
        return InteractionManager.getInstance().getSyncedConfig().speciesWhiteList();
    }
}
