package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

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

    public Optional<VertexConsumer> getItemBuffer(VertexConsumerProvider vertexConsumers, Identifier texture) {
        return Optional.empty();
    }
}
