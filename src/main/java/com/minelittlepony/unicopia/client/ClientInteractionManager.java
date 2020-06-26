package com.minelittlepony.unicopia.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.util.dummy.DummyClientPlayerEntity;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ClientInteractionManager extends InteractionManager {

    @Override
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        if (observer.world instanceof ClientWorld) {
            return new DummyClientPlayerEntity((ClientWorld)observer.world, profile);
        }
        return super.createPlayer(observer, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        if (MinecraftClient.getInstance().player == player) {
            return true;
        }

        if (MinecraftClient.getInstance().player == null || player == null) {
            return false;
        }

        return Pony.equal(MinecraftClient.getInstance().player, player);
    }

    @Override
    public Race getPreferredRace() {
        if (!Unicopia.getConfig().ignoresMineLittlePony()
                && MinecraftClient.getInstance().player != null) {
            Race race = MineLPConnector.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }

        return Unicopia.getConfig().getPrefferedRace();
    }

    @Override
    public int getViewMode() {
        return MinecraftClient.getInstance().options.perspective;
    }
}
