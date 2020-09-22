package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class Commands {
    public static void bootstrap() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SpeciesCommand.register(dispatcher);
            RacelistCommand.register(dispatcher);
            GravityCommand.register(dispatcher);
            DisguiseCommand.register(dispatcher);
        });
        @SuppressWarnings("deprecation")
        Object game = FabricLoader.getInstance().getGameInstance();
        if (game instanceof MinecraftServer) {
            ((MinecraftServer)game).setFlightEnabled(true);
        }
    }
}
