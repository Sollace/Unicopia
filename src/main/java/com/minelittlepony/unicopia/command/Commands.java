package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class Commands {
    public static void bootstrap() {
        CommandRegistry.INSTANCE.register(false, SpeciesCommand::register);
        CommandRegistry.INSTANCE.register(false, RacelistCommand::register);
        CommandRegistry.INSTANCE.register(false, GravityCommand::register);
        CommandRegistry.INSTANCE.register(false, DisguiseCommand::register);

        Object game = FabricLoader.getInstance().getGameInstance();
        if (game instanceof MinecraftServer) {
            ((MinecraftServer)game).setFlightEnabled(true);
        }
    }
}
