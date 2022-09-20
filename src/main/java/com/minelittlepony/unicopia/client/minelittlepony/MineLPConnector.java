package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.Optional;

import com.minelittlepony.client.MineLittlePony;
import com.minelittlepony.client.render.LevitatingItemRenderer;
import com.minelittlepony.unicopia.Race;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class MineLPConnector {
    public static Race getPlayerPonyRace() {
        return getPlayerPonyRace(MinecraftClient.getInstance().player);
    }

    public static Race getPlayerPonyRace(PlayerEntity player) {
        if (!FabricLoader.getInstance().isModLoaded("minelp") || player == null) {
            return Race.HUMAN;
        }

        switch (MineLittlePony.getInstance().getManager().getPony(player).getRace(false)) {
            case ALICORN:
                return Race.ALICORN;
            case CHANGELING:
            case CHANGEDLING:
                return Race.CHANGELING;
            case ZEBRA:
            case EARTH:
                return Race.EARTH;
            case GRYPHON:
            case HIPPOGRIFF:
            case PEGASUS:
                return Race.PEGASUS;
            case BATPONY:
                return Race.BAT;
            case SEAPONY:
            case UNICORN:
                return Race.UNICORN;
            default:
                return Race.HUMAN;
        }
    }

    public static Optional<VertexConsumer> getItemBuffer(VertexConsumerProvider vertexConsumers, Identifier texture) {
        if (!FabricLoader.getInstance().isModLoaded("minelp")) {
            return Optional.empty();
        }

        if (LevitatingItemRenderer.isEnabled()) {
            return Optional.of(vertexConsumers.getBuffer(LevitatingItemRenderer.getRenderLayer(texture)));
        }

        return Optional.empty();
    }
}
