package com.minelittlepony.unicopia.command;

import java.util.Arrays;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

class GravityCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
                .literal("gravity")
                .requires(s -> s.hasPermissionLevel(2));

        builder.then(CommandManager.literal("get")
                        .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "target"), false))
                ));
        builder.then(CommandManager.literal("set")
               .then(CommandManager.argument("gravity", FloatArgumentType.floatArg(-99, 99))
                           .executes(context -> set(context.getSource(), context.getSource().getPlayer(), FloatArgumentType.getFloat(context, "gravity"), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                           .executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "target"), FloatArgumentType.getFloat(context, "gravity"), false))
               )));

        dispatcher.register(builder);
    }

    static int get(ServerCommandSource source, PlayerEntity player, boolean isSelf) throws CommandSyntaxException {
        sendFeedback(source, player, "get", false, Pony.of(player).getPhysics().getGravityModifier());
        return 0;
    }

    static int set(ServerCommandSource source, PlayerEntity player, float gravity, boolean isSelf) {

        Pony iplayer = Pony.of(player);

        iplayer.getPhysics().setBaseGravityModifier(gravity);
        iplayer.setDirty();

        sendFeedback(source, player, "set", true, gravity);
        return 0;
    }


    static void sendFeedback(ServerCommandSource source, PlayerEntity player, String key, boolean notifyTarget, Object...arguments) {
        String translationKey = "commands.gravity." + key;

        if (source.getEntity() == player) {
            source.sendFeedback(Text.translatable(translationKey + ".self", arguments), true);
        } else {
            if (notifyTarget && source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(Text.translatable(translationKey, arguments));
            }

            source.sendFeedback(Text.translatable(translationKey + ".other", Streams.concat(Stream.of(player.getDisplayName()), Arrays.stream(arguments)).toArray()), true);
        }
    }
}
