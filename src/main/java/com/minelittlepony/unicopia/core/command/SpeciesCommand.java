package com.minelittlepony.unicopia.core.command;

import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

class SpeciesCommand {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("race");

        builder.then(CommandManager.literal("get")
                      .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                      .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "target"), false))
               ));

        builder.then(CommandManager.literal("set")
               .then(CommandManager.argument("race", new RaceArgument())
                       .executes(context -> set(context.getSource(), context.getSource().getPlayer(), context.getArgument("race", Race.class), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                       .executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "target"), context.getArgument("race", Race.class), false))
               )));

        builder.then(CommandManager.literal("describe")
               .then(CommandManager.argument("race", new RaceArgument())
                       .executes(context -> describe(context.getSource().getPlayer(), context.getArgument("race", Race.class))
               )));

        builder.then(CommandManager.literal("list")
                       .executes(context -> list(context.getSource().getPlayer())
                ));

        dispatcher.register(builder);
    }

    static int set(ServerCommandSource source, PlayerEntity player, Race race, boolean isSelf) {

        if (SpeciesList.instance().speciesPermitted(race, player)) {
            SpeciesList.instance().getPlayer(player).setSpecies(race);

            Text formattedName = new TranslatableText(race.name().toLowerCase());

            if (!isSelf) {
                source.sendFeedback(new TranslatableText("commands.race.success.other", player.getName(), formattedName), true);
            } else {
                if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                    player.sendMessage(new TranslatableText("commands.race.success.self"));
                }
                source.sendFeedback(new TranslatableText("commands.race.success.otherself", player.getName(), formattedName), true);
            }
        } else if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            player.sendMessage(new TranslatableText("commands.race.permission"));
        }

        return 0;
    }

    static int get(ServerCommandSource source, PlayerEntity player, boolean isSelf) {
        Race spec = SpeciesList.instance().getPlayer(player).getSpecies();

        String name = "commands.race.tell.";
        name += isSelf ? "self" : "other";

        Text race = new TranslatableText(spec.getTranslationKey());
        Text message = new TranslatableText(name);

        race.getStyle().setColor(Formatting.GOLD);

        message.append(race);

        player.sendMessage(message);

        return 0;
    }

    static int list(PlayerEntity player) {
        player.sendMessage(new TranslatableText("commands.race.list"));

        Text message = new LiteralText("");

        boolean first = true;
        for (Race i : Race.values()) {
            if (!i.isDefault() && SpeciesList.instance().speciesPermitted(i, player)) {
                message.append(new TranslatableText((!first ? "\n" : "") + " - " + i.name().toLowerCase()));
                first = false;
            }
        }

        message.getStyle().setColor(Formatting.GOLD);

        player.sendMessage(message);

        return 0;
    }

    static int describe(PlayerEntity player, Race species) {
        String name = species.name().toLowerCase();

        Text line1 = new TranslatableText(String.format("commands.race.describe.%s.1", name));
        line1.getStyle().setColor(Formatting.YELLOW);

        player.sendMessage(line1);

        player.sendMessage(new TranslatableText(String.format("commands.race.describe.%s.2", name)));

        Text line3 = new TranslatableText(String.format("commands.race.describe.%s.3", name));
        line3.getStyle().setColor(Formatting.RED);

        player.sendMessage(line3);

        return 0;
    }
}
