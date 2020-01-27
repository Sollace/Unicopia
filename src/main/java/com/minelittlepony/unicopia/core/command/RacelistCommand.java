package com.minelittlepony.unicopia.core.command;

import java.util.function.BiFunction;

import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

class RacelistCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("racelist").requires(s -> s.hasPermissionLevel(4));

        builder.then(CommandManager.literal("allow")
                .then(CommandManager.argument("race", new RaceArgument())
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), context.getArgument("race", Race.class), "allowed", SpeciesList::unwhiteListRace))
        ));
        builder.then(CommandManager.literal("disallow")
                .then(CommandManager.argument("race", new RaceArgument())
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), context.getArgument("race", Race.class), "disallowed", SpeciesList::whiteListRace))
        ));

        dispatcher.register(builder);
    }

    static int toggle(ServerCommandSource source, ServerPlayerEntity player, Race race, String action, BiFunction<SpeciesList, Race, Boolean> func) {
        String translationKey = "commands.racelist." + action;

        if (!func.apply(SpeciesList.instance(), race)) {
            translationKey += ".failed";
        }

        Text formattedName = new TranslatableText(race.name().toLowerCase());
        formattedName.getStyle().setColor(Formatting.GOLD);

        Text comp = new TranslatableText(translationKey, formattedName);
        comp.getStyle().setColor(Formatting.GREEN);

        player.sendMessage(comp);

        source.sendFeedback(new TranslatableText(translationKey + ".other", player.getName(), formattedName), true);

        return 0;
    }
}
