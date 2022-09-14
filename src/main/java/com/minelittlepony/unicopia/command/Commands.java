package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.server.MinecraftServer;

public class Commands {
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    public static void bootstrap() {
        ArgumentTypes.register(
                "unicopia:enumeration",
                EnumArgumentType.class,
                new EnumArgumentType.Serializer()
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SpeciesCommand.register(dispatcher);
            RacelistCommand.register(dispatcher);
            GravityCommand.register(dispatcher);
            DisguiseCommand.register(dispatcher);
            TraitCommand.register(dispatcher);
            EmoteCommand.register(dispatcher);
            ManaCommand.register(dispatcher);
        });
        Object game = FabricLoader.getInstance().getGameInstance();
        if (game instanceof MinecraftServer) {
            ((MinecraftServer)game).setFlightEnabled(true);
        }
    }
}
