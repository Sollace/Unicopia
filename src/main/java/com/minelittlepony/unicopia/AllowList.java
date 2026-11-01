package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgConfigurationChange;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;

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
        Set<RegistryKey<Race>> values = new HashSet<>(Unicopia.getConfig().speciesWhiteList.get());
        boolean added = values.add(race.getKey());
        update(server, values);
        return added;
    }

    public boolean remove(@Nullable MinecraftServer server, Race race) {
        Set<RegistryKey<Race>> values = new HashSet<>(Unicopia.getConfig().speciesWhiteList.get());
        if (values.isEmpty()) {
            for (Race r : Race.REGISTRY) {
                if (!isAlwaysPermitted(r) && r != race) {
                    values.add(r.getKey());
                }
            }

            update(server, values);
            return true;
        }
        boolean removed = values.remove(race.getKey());
        update(server, values);
        return removed;
    }

    public boolean permits(Race race) {
        return isAlwaysPermitted(race)
                || !isEnabled()
                || entries().contains(race.getKey());
    }

    public static boolean isAlwaysPermitted(Race race) {
        return race.isUnset() || race.isHuman();
    }

    private void update(@Nullable MinecraftServer server, Set<RegistryKey<Race>> values) {
        // Filters out entries that shouldn't be there and remove unknown ids
        values = values.stream().filter(r -> Race.REGISTRY.getOptionalValue(r).filter(o -> !isAlwaysPermitted(o)).isPresent()).collect(Collectors.toUnmodifiableSet());
        if (getExcluded(getValues(values)) == 0) {
            values = Set.of();
        }

        if (!values.equals(Unicopia.getConfig().speciesWhiteList.get())) {
            Unicopia.getConfig().speciesWhiteList.set(values);
            Unicopia.getConfig().save();
        }
        if (server != null && !server.isRemote()) {
            MsgConfigurationChange msg = new MsgConfigurationChange(Unicopia.getConfig().toSynced());
            InteractionManager.getInstance().setSyncedConfig(msg.config());
            server.getPlayerManager().getPlayerList().forEach(recipient -> {
                Channel.CONFIGURATION_CHANGE.sendToPlayer(msg, recipient);
            });
        }
    }

    private static Set<Race> getValues(Set<RegistryKey<Race>> permitted) {
        return permitted.stream().flatMap(id -> Race.REGISTRY.getOptionalValue(id).stream()).collect(Collectors.toUnmodifiableSet());
    }

    private static long getExcluded(Set<Race> permitted) {
        return Race.REGISTRY.stream().filter(r -> !isAlwaysPermitted(r) && !permitted.contains(r)).count();
    }

    public Set<RegistryKey<Race>> entries() {
        return InteractionManager.getInstance().getSyncedConfig().speciesWhiteList();
    }
}
