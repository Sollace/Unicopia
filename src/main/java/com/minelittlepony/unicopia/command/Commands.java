package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.MinecraftServer;

public class Commands {
    public static void bootstrap() {
        ArgumentTypes.register("unicopia:race", RaceArgument.class, new ConstantArgumentSerializer<>(RaceArgument::new));
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
