package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class RacelistCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("racelist").requires(s -> s.hasPermissionLevel(4));

        RegistryKeyArgumentType<Race> raceArgument = Race.argument();

        builder.then(CommandManager.literal("allow")
                .then(CommandManager.argument("race", raceArgument)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "allowed", race -> {
                    boolean result = Unicopia.getConfig().speciesWhiteList.get().add(race);

                    Unicopia.getConfig().save();

                    return result;
                }))
        ));
        builder.then(CommandManager.literal("disallow")
                .then(CommandManager.argument("race", raceArgument)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "disallowed", race -> {
                    boolean result = Unicopia.getConfig().speciesWhiteList.get().remove(race);

                    Unicopia.getConfig().save();

                    return result;
                }))
        ));

        dispatcher.register(builder);
    }

    static int toggle(ServerCommandSource source, ServerPlayerEntity player, Race race, String action, Function<Race, Boolean> func) {
        String translationKey = "commands.racelist." + action;

        if (!func.apply(race)) {
            translationKey += ".failed";
        }

        Text formattedName = race.getDisplayName().copy().formatted(Formatting.GOLD);

        source.sendFeedback(Text.translatable(translationKey, formattedName).formatted(Formatting.GREEN), false);
        return 0;
    }
}
