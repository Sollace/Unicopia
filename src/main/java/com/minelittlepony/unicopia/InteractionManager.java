package com.minelittlepony.unicopia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.player.dummy.DummyPlayerEntity;
import com.minelittlepony.unicopia.entity.player.dummy.DummyServerPlayerEntity;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InteractionManager {
    public static InteractionManager INSTANCE = new InteractionManager();

    public static InteractionManager instance() {
        return INSTANCE;
    }

    /**
     * Returns true on the client if the passed in player entity is the client's player.
     * Always returns false on the server.
     */
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        return false;
    }

    /**
     * The player's camera mode. Always 0 on the server.
     */
    public int getViewMode() {
        return 0;
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        if (observer.world instanceof ServerWorld) {
            return new DummyServerPlayerEntity((ServerWorld)observer.world, profile);
        }
        return new DummyPlayerEntity(observer.world, profile);
    }
}
