package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class RacelistCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("racelist").requires(s -> s.hasPermissionLevel(4));

        EnumArgumentType<Race> raceArgument = EnumArgumentType.of(Race.class, Race::isUsable, Race.EARTH);

        builder.then(CommandManager.literal("allow")
                .then(CommandManager.argument("race", raceArgument)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), context.getArgument("race", Race.class), "allowed", race -> {
                    boolean result = Unicopia.getConfig().speciesWhiteList.get().add(race);

                    Unicopia.getConfig().save();

                    return result;
                }))
        ));
        builder.then(CommandManager.literal("disallow")
                .then(CommandManager.argument("race", raceArgument)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), context.getArgument("race", Race.class), "disallowed", race -> {
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

        Text formattedName = Text.translatable(race.name().toLowerCase()).formatted(Formatting.GOLD);

        source.sendFeedback(Text.translatable(translationKey, formattedName).formatted(Formatting.GREEN), false);
        return 0;
    }
}
