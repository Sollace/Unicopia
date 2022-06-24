package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class Commands {
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    public static void bootstrap() {
        ArgumentTypeRegistry.registerArgumentType(new Identifier("unicopia", "enumeration"), EnumArgumentType.class, new EnumArgumentType.Serializer());
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            SpeciesCommand.register(dispatcher);
            RacelistCommand.register(dispatcher);
            GravityCommand.register(dispatcher);
            DisguiseCommand.register(dispatcher);
            TraitCommand.register(dispatcher);
            EmoteCommand.register(dispatcher);
        });
        Object game = FabricLoader.getInstance().getGameInstance();
        if (game instanceof MinecraftServer) {
            ((MinecraftServer)game).setFlightEnabled(true);
        }
    }
}
