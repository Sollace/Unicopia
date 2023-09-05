package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class RacelistCommand {

    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("racelist").requires(s -> s.hasPermissionLevel(3))
            .then(CommandManager.literal("allow")
                .then(CommandManager.argument("race", Race.argument())
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "allowed", race -> {

                    if (race.isUnset()) {
                        return false;
                    }

                    boolean result = Unicopia.getConfig().speciesWhiteList.get().add(race.getId().toString());

                    Unicopia.getConfig().save();

                    return result;
                }))
            ))
            .then(CommandManager.literal("disallow")
                .then(CommandManager.argument("race", Race.argument())
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "disallowed", race -> {
                    boolean result = Unicopia.getConfig().speciesWhiteList.get().remove(race.getId().toString());

                    Unicopia.getConfig().save();

                    return result;
                }))
            ));
    }

    static int toggle(ServerCommandSource source, ServerPlayerEntity player, Race race, String action, Function<Race, Boolean> func) {
        source.sendFeedback(() -> {
            String translationKey = "commands.racelist." + action;

            if (!func.apply(race)) {
                if (race.isUnset()) {
                    translationKey = "commands.racelist.illegal";
                } else {
                    translationKey += ".failed";
                }
            }

            Text formattedName = race.getDisplayName().copy().formatted(Formatting.GOLD);
            return Text.translatable(translationKey, formattedName).formatted(Formatting.GREEN);
        }, false);
        return 0;
    }
}
