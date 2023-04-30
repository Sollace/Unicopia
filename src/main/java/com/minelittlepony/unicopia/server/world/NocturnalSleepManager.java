package com.minelittlepony.unicopia.server.world;

import java.util.List;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.world.GameRules;

public class NocturnalSleepManager extends SleepManager {
    public static final long DAY_LENGTH = 24000L;

    private final ServerWorld world;

    public NocturnalSleepManager(ServerWorld world) {
        this.world = world;
    }

    public String getTimeSkippingMessage(String nightSkippingMessage) {
        if (world.getGameRules().getBoolean(UGameRules.DO_NOCTURNAL_BAT_PONIES) && world.isDay()) {
            return "sleep.skipping_day";
        }

        return nightSkippingMessage;
    }

    public void skipTime() {
        if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            world.setTimeOfDay(world.getLevelProperties().getTimeOfDay() - DAY_LENGTH + 13500);
        }
    }

    public List<ServerPlayerEntity> getApplicablePlayer() {
        return filterPlayers(world.getPlayers());
    }

    public List<ServerPlayerEntity> filterPlayers(List<ServerPlayerEntity> players) {
        if (!world.getGameRules().getBoolean(UGameRules.DO_NOCTURNAL_BAT_PONIES)) {
            return players;
        }

        return players.stream().filter(player -> {
            Pony pony = Pony.of(player);
            return (pony.getActualSpecies().isNocturnal() == world.isDay());
        }).toList();
    }

    public static void bootstrap() {
        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, pos, isDay) -> Pony.of(player).canSleepNow());
    }

    public interface Source {
        NocturnalSleepManager getNocturnalSleepManager();
    }
}
