package com.minelittlepony.unicopia.client.minelittlepony;

import com.minelittlepony.unicopia.Race;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class MineLPDelegate {
    static MineLPDelegate INSTANCE = new MineLPDelegate();

    public static MineLPDelegate getInstance() {
        return INSTANCE;
    }

    public final Race getPlayerPonyRace() {
        if (MinecraftClient.getInstance().player == null) {
            return Race.HUMAN;
        }
        return getPlayerPonyRace(MinecraftClient.getInstance().player);
    }

    public Race getPlayerPonyRace(PlayerEntity player) {
        return Race.HUMAN;
    }

    public float getPonyHeight(Entity entity) {
        return entity.getHeight();
    }

    public Race getRace(Entity entity) {
        return Race.HUMAN;
    }
}
