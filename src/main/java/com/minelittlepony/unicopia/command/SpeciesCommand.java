package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgTribeSelect;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

class SpeciesCommand {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher, RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("race");

        if (environment.dedicated) {
            if (Unicopia.getConfig().enableCheats.get()) {
                builder = builder.requires(source -> source.hasPermissionLevel(2));
            } else {
                builder = builder.requires(source -> source.hasPermissionLevel(4));
            }
        }

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

            if (race.isUnset()) {
                Channel.SERVER_SELECT_TRIBE.sendToPlayer(new MsgTribeSelect(Race.allPermitted(player), "gui.unicopia.tribe_selection.respawn"), (ServerPlayerEntity)player);
            }

            if (player == source.getPlayer()) {
                source.sendFeedback(() -> Text.translatable("commands.race.success.self", race.getDisplayName()), true);
            } else {
                if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                    player.sendMessage(Text.translatable("commands.race.success", race.getDisplayName()), false);
                }
                source.sendFeedback(() -> Text.translatable("commands.race.success.other", player.getName(), race.getDisplayName()), true);
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

        player.sendMessage(Text.translatable(name, player.getName())
                .append(Text.translatable(spec.getTranslationKey())
                        .styled(s -> s.withColor(Formatting.GOLD))), false);

        return 0;
    }

    static int list(PlayerEntity player) {
        player.sendMessage(Text.translatable("commands.race.list"), false);

        MutableText message = Text.literal("");

        boolean first = true;
        for (Race i : Race.REGISTRY) {
            if (!i.isUnset() && i.isPermitted(player)) {
                message.append(Text.literal((!first ? "\n" : "") + " - "));
                message.append(i.getDisplayName());
                first = false;
            }
        }

        player.sendMessage(message.styled(s -> s.withColor(Formatting.GOLD)), false);

        return 0;
    }

    static int describe(PlayerEntity player, Race species) {
        Identifier id = Race.REGISTRY.getId(species);

        for (String category : new String[] { "goods", "bads" }) {
            player.sendMessage(Text.translatable(
                    String.format("gui.unicopia.tribe_selection.confirm.%s.%d.%s.%s", category),
                    species.getAltDisplayName()
            ), false);
            for (int i = 1; i < 5; i++) {
                String line = String.format("gui.unicopia.tribe_selection.confirm.%s.%d.%s.%s", category, i, id.getNamespace(), id.getPath());

                player.sendMessage(Text.translatable(line).styled(s -> s.withColor(category.equals("goods") ? Formatting.YELLOW : Formatting.RED)), false);
            }
        }

        return 0;
    }
}
