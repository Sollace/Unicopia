package com.minelittlepony.unicopia.server.world;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class OfflinePlayerCache {
    private static final LoadingCache<Key, Optional<ServerPlayerEntity>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(CacheLoader.from(key -> {
                ServerPlayerEntity offlinePlayer = FakePlayer.get(key.world(), new GameProfile(key.playerId(), "[Offline Player]"));

                if (key.world().getServer().getPlayerManager().loadPlayerData(offlinePlayer) != null) {
                    return Optional.of(offlinePlayer);
                }

                return Optional.empty();
            }));

    @Nullable
    public static ServerPlayerEntity getOfflinePlayer(ServerWorld world, UUID playerId) {
        ServerPlayerEntity player = (ServerPlayerEntity)world.getPlayerByUuid(playerId);
        if (player == null) {
            player = world.getServer().getPlayerManager().getPlayer(playerId);
        }
        if (player == null) {
            return CACHE.getUnchecked(new Key(world, playerId)).orElse(null);
        }
        return player;
    }

    record Key (ServerWorld world, UUID playerId) {}
}
