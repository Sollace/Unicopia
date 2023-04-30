package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.MinecraftServer;

public class Commands {
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    public static void bootstrap() {
        ArgumentTypeRegistry.registerArgumentType(
                Unicopia.id("enumeration"),
                EnumArgumentType.class,
                new EnumArgumentType.Serializer()
        );
        ArgumentTypeRegistry.registerArgumentType(
                Unicopia.id("spell_traits"),
                TraitsArgumentType.class,
                ConstantArgumentSerializer.of(TraitsArgumentType::traits)
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            RacelistCommand.register(dispatcher);
            EmoteCommand.register(dispatcher);
            SpeciesCommand.register(dispatcher, environment);
            WorldTribeCommand.register(dispatcher);

            GravityCommand.register(dispatcher);
            DisguiseCommand.register(dispatcher, registries);
            CastCommand.register(dispatcher, registries);
            TraitCommand.register(dispatcher);
            ManaCommand.register(dispatcher);
        });

        if (FabricLoader.getInstance().getGameInstance() instanceof MinecraftServer server) {
            server.setFlightEnabled(true);
        }
    }
}
