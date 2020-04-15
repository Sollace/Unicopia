package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

class GravityCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("gravity").requires(s -> s.hasPermissionLevel(4));

        builder.then(CommandManager
                .literal("get")
                .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
        ).then(CommandManager.argument("target", EntityArgumentType.player())
                .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "target"), false))
        );
        builder.then(CommandManager
                .literal("set")
                .then(CommandManager.argument("gravity", FloatArgumentType.floatArg(-99, 99))
                        .executes(context -> set(context.getSource(), context.getSource().getPlayer(), FloatArgumentType.getFloat(context, "gravity"), true))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "target"), FloatArgumentType.getFloat(context, "gravity"), false))
        )));

        dispatcher.register(builder);
    }

    static int get(ServerCommandSource source, PlayerEntity player, boolean isSelf) {
        String translationKey = "commands.gravity.get";

        IPlayer iplayer = SpeciesList.instance().getPlayer(player);

        float gravity = iplayer.getGravity().getGravitationConstant();

        if (isSelf) {
            player.sendMessage(new TranslatableText(translationKey, gravity));
        }

        source.sendFeedback(new TranslatableText(translationKey + ".other", player.getName(), gravity), true);

        return 0;
    }

    static int set(ServerCommandSource source, PlayerEntity player, float gravity, boolean isSelf) {
        String translationKey = "commands.gravity.set";

        IPlayer iplayer = SpeciesList.instance().getPlayer(player);

        iplayer.getGravity().setGraviationConstant(gravity);
        iplayer.sendCapabilities(true);

        if (isSelf) {
            player.sendMessage(new TranslatableText(translationKey, gravity));
        }

        source.sendFeedback(new TranslatableText(translationKey + ".other", player.getName(), gravity), true);

        return 0;
    }
}
