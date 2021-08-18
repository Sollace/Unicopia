package com.minelittlepony.unicopia.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.sound.LoopingSoundInstance;
import com.minelittlepony.unicopia.client.sound.MotionBasedSoundInstance;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.player.PlayerPhysics;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.dummy.DummyClientPlayerEntity;
import com.minelittlepony.unicopia.network.handler.ClientNetworkHandler;
import com.minelittlepony.unicopia.network.handler.ClientNetworkHandlerImpl;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ClientInteractionManager extends InteractionManager {

    private final ClientNetworkHandler handler = new ClientNetworkHandlerImpl();
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public MinecraftSessionService getSessionService(World world) {
        return MinecraftClient.getInstance().getSessionService();
    }

    @Override
    @Nullable
    public ClientNetworkHandler getClientNetworkHandler() {
        return handler;
    }

    @Override
    public void playLoopingSound(Entity source, int type) {
        SoundManager soundManager = client.getSoundManager();

        if (type == SOUND_EARS_RINGING && source instanceof LivingEntity) {
            soundManager.play(new LoopingSoundInstance<>((LivingEntity)source, e -> e.hasStatusEffect(SunBlindnessStatusEffect.INSTANCE), USounds.ENTITY_PLAYER_EARS_RINGING, 1F, 1F));
        } else if (type == SOUND_BEE && source instanceof BeeEntity) {
            soundManager.playNextTick(
                    ((BeeEntity)source).hasAngerTime()
                        ? new AggressiveBeeSoundInstance(((BeeEntity)source))
                        : new PassiveBeeSoundInstance(((BeeEntity)source))
            );
        } else if (type == SOUND_MINECART && source instanceof AbstractMinecartEntity) {
            soundManager.play(new MovingMinecartSoundInstance((AbstractMinecartEntity)source));
        } else if (type == SOUND_CHANGELING_BUZZ && source instanceof PlayerEntity) {
            soundManager.play(new LoopingSoundInstance<>((PlayerEntity)source, e -> {
                PlayerPhysics physics = Pony.of(e).getPhysics();
                return physics.isFlying() && physics.getFlightType() == FlightType.INSECTOID;
            }, USounds.ENTITY_PLAYER_CHANGELING_BUZZ, 1F, 1F));
        } else if (type == SOUND_GLIDING && source instanceof PlayerEntity) {
            soundManager.play(new MotionBasedSoundInstance(SoundEvents.ITEM_ELYTRA_FLYING, (PlayerEntity)source));
        }
    }

    @Override
    @NotNull
    public PlayerEntity createPlayer(World world, GameProfile profile) {
        if (world instanceof ClientWorld) {
            return new DummyClientPlayerEntity((ClientWorld)world, profile);
        }
        return super.createPlayer(world, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        return (client.player != null && player != null)
             && (client.player == player
                 || Pony.equal(client.player, player));
    }

    @Override
    public int getViewMode() {
        return client.options.getPerspective().ordinal();
    }
}
