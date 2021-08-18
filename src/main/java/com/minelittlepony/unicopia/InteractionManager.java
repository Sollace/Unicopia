package com.minelittlepony.unicopia;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.dummy.DummyPlayerEntity;
import com.minelittlepony.unicopia.entity.player.dummy.DummyServerPlayerEntity;
import com.minelittlepony.unicopia.network.handler.ClientNetworkHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class InteractionManager {
    public static final int SOUND_EARS_RINGING = 0;
    public static final int SOUND_CHANGELING_BUZZ = 1;
    public static final int SOUND_BEE = 2;
    public static final int SOUND_MINECART = 3;
    public static final int SOUND_GLIDING = 4;

    public static InteractionManager INSTANCE = new InteractionManager();

    public static InteractionManager instance() {
        return INSTANCE;
    }

    public MinecraftSessionService getSessionService(World world) {
        if (world instanceof ServerWorld) {
            return ((ServerWorld)world).getServer().getSessionService();
        }

        throw new NullPointerException("Cannot get session service");
    }

    /**
     * Returns the client network handler, or throws if called on the server.
     */
    public ClientNetworkHandler getClientNetworkHandler() {
        throw new NullPointerException("Client network handler called by the server");
    }

    /**
     * Plays a custom sound instance
     */
    public void playLoopingSound(Entity source, int type) {

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
    @NotNull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        return createPlayer(observer.world, profile);
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @NotNull
    public PlayerEntity createPlayer(World world, GameProfile profile) {
        if (world instanceof ServerWorld) {
            return new DummyServerPlayerEntity((ServerWorld)world, profile);
        }
        return new DummyPlayerEntity(world, profile);
    }
}
