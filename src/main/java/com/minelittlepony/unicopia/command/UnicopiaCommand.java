package com.minelittlepony.unicopia.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;

class UnicopiaCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> create(CommandRegistryAccess registries, RegistrationEnvironment environment) {
        return CommandManager.literal("unicopia")
                .then(EmoteCommand.create())
                .then(ConfigCommand.create(registries))
                .then(SpeciesCommand.create(environment))
                .then(RacelistCommand.create())
                .then(WorldTribeCommand.create())
                .then(SkyAngleCommand.create())
                .then(GravityCommand.create())
                .then(DisguiseCommand.create(registries))
                .then(CastCommand.create(registries))
                .then(TraitCommand.create())
                .then(ManaCommand.create());
    }
}
