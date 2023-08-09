package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class EmoteCommand {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
                .literal("emote")
                .then(CommandManager.argument("animation", Animation.argument()).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        Animation.Recipient.ANYONE
                    )
                ).then(CommandManager.argument("duration", IntegerArgumentType.integer(1, 99)).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        Animation.Recipient.ANYONE,
                        IntegerArgumentType.getInteger(source, "duration")
                    )
                ).then(CommandManager.argument("recipient_type", Animation.Recipient.argument()).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        source.getArgument("recipient_type", Animation.Recipient.class),
                        IntegerArgumentType.getInteger(source, "duration")
                    )
                ))
        )));
    }

    static int apply(ServerCommandSource source, Animation animation, Animation.Recipient recipient) throws CommandSyntaxException {
        return apply(source, animation, recipient, animation.getDuration());
    }

    static int apply(ServerCommandSource source, Animation animation, Animation.Recipient recipient, int duration) throws CommandSyntaxException {
        Pony.of(source.getPlayer()).setAnimation(animation, recipient, duration);
        return 0;
    }
}
