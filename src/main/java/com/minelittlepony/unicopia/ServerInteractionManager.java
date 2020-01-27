package com.minelittlepony.unicopia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.util.dummy.DummyPlayerEntity;
import com.minelittlepony.util.dummy.DummyServerPlayerEntity;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerInteractionManager implements InteractionManager {

    @Deprecated
    public static boolean isClientSide() {
        return false;
    }

    @Override
    @Nullable
    public PlayerEntity getClientPlayer() {
        return null;
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        return false;
    }

    @Override
    public int getViewMode() {
        return 0;
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @Override
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        if (observer.world instanceof ServerWorld) {
            return new DummyServerPlayerEntity((ServerWorld)observer.world, profile);
        }
        return new DummyPlayerEntity(observer.world, profile);
    }

    @Override
    public void postRenderEntity(Entity entity) {

    }

    @Override
    public boolean renderEntity(Entity entity, float renderPartialTicks) {
        return false;
    }

}
