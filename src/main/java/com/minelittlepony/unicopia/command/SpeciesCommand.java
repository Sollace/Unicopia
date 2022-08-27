package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

class SpeciesCommand {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("race");

        RegistryKeyArgumentType<Race> raceArgument = Race.argument();

        builder.then(CommandManager.literal("get")
                      .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                      .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "target"), false))
               ));

        builder.then(CommandManager.literal("set")
               .then(CommandManager.argument("race", raceArgument)
                       .executes(context -> set(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                       .executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "target"), Race.fromArgument(context, "race"), false)))
               ));

        builder.then(CommandManager.literal("describe")
               .then(CommandManager.argument("race", raceArgument)
                       .executes(context -> describe(context.getSource().getPlayer(), Race.fromArgument(context, "race")))
               ));

        builder.then(CommandManager.literal("list")
                       .executes(context -> list(context.getSource().getPlayer())
               ));

        dispatcher.register(builder);
    }

    static int set(ServerCommandSource source, PlayerEntity player, Race race, boolean isSelf) {

        if (race.isPermitted(player)) {
            Pony pony = Pony.of(player);
            pony.setSpecies(race);
            pony.setDirty();

            if (!isSelf) {
                source.sendFeedback(Text.translatable("commands.race.success.other", player.getName(), race.getDisplayName()), true);
            } else {
                if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                    player.sendMessage(Text.translatable("commands.race.success.self"), false);
                }
                source.sendFeedback(Text.translatable("commands.race.success.otherself", player.getName(), race.getDisplayName()), true);
            }
        } else if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            player.sendMessage(Text.translatable("commands.race.permission"), false);
        }

        return 0;
    }

    static int get(ServerCommandSource source, PlayerEntity player, boolean isSelf) {
        Race spec = Pony.of(player).getSpecies();

        String name = "commands.race.tell.";
        name += isSelf ? "self" : "other";

        player.sendMessage(Text.translatable(name)
                .append(Text.translatable(spec.getTranslationKey())
                        .styled(s -> s.withColor(Formatting.GOLD))), false);

        return 0;
    }

    static int list(PlayerEntity player) {
        player.sendMessage(Text.translatable("commands.race.list"), false);

        MutableText message = Text.literal("");

        boolean first = true;
        for (Race i : Race.REGISTRY) {
            if (!i.isDefault() && i.isPermitted(player)) {
                message.append(Text.literal((!first ? "\n" : "") + " - "));
                message.append(i.getDisplayName());
                first = false;
            }
        }

        player.sendMessage(message.styled(s -> s.withColor(Formatting.GOLD)), false);

        return 0;
    }

    static int describe(PlayerEntity player, Race species) {
        String name = species.getTranslationKey();

        player.sendMessage(Text.translatable(String.format("commands.race.describe.%s.1", name)).styled(s -> s.withColor(Formatting.YELLOW)), false);
        player.sendMessage(Text.translatable(String.format("commands.race.describe.%s.2", name)), false);
        player.sendMessage(Text.translatable(String.format("commands.race.describe.%s.3", name)).styled(s -> s.withColor(Formatting.RED)), false);

        return 0;
    }
}
