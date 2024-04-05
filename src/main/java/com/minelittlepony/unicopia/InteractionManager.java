package com.minelittlepony.unicopia;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.dummy.DummyPlayerEntity;
import com.minelittlepony.unicopia.particle.ParticleSpawner;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class InteractionManager {
    public static final int SOUND_EARS_RINGING = 0;
    public static final int SOUND_CHANGELING_BUZZ = 1;
    public static final int SOUND_BEE = 2;
    public static final int SOUND_MINECART = 3;
    public static final int SOUND_GLIDING = 4;
    public static final int SOUND_MAGIC_BEAM = 5;
    public static final int SOUND_HEART_BEAT = 6;
    public static final int SOUND_KIRIN_RAGE = 7;

    public static final int SCREEN_DISPELL_ABILITY = 0;

    public static InteractionManager INSTANCE = new InteractionManager();

    public static InteractionManager instance() {
        return INSTANCE;
    }

    public ParticleSpawner createBoundParticle(UUID id) {
        return ParticleSpawner.EMPTY;
    }

    public Map<Identifier, ?> readChapters(PacketByteBuf buf) {
        throw new RuntimeException("Method not supported");
    }

    /**
     * Plays a custom sound instance
     */
    public void playLoopingSound(Entity source, int type, long seed) {

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

    public void openScreen(int type) {
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @NotNull
    public final PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        return createPlayer(observer.getWorld(), profile);
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @NotNull
    public PlayerEntity createPlayer(World world, GameProfile profile) {
        return new DummyPlayerEntity(world, profile);
    }

    public void sendPlayerLookAngles(PlayerEntity player) {

    }

    public void addBlockBreakingParticles(BlockPos pos, Direction direction) {

    }
}
