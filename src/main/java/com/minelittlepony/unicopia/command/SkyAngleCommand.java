package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

class SkyAngleCommand {
    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("skyangle").requires(s -> s.hasPermissionLevel(2))
                    .executes(context -> get(context.getSource()))
            .then(CommandManager.literal("set")
                    .executes(context -> set(context.getSource(), 0))
                    .then(CommandManager.argument("angle", FloatArgumentType.floatArg(0, 360))
                            .executes(context -> set(context.getSource(), FloatArgumentType.getFloat(context, "angle")))));
    }

    static int get(ServerCommandSource source) throws CommandSyntaxException {
        source.sendFeedback(() -> Text.translatable("commands.skyangle.get", UnicopiaWorldProperties.forWorld(source.getWorld()).getTangentalSkyAngle()), true);
        return 0;
    }

    static int set(ServerCommandSource source, float newAngle) {
        UnicopiaWorldProperties.forWorld(source.getWorld()).setTangentalSkyAngle(newAngle);
        source.sendFeedback(() -> Text.translatable("commands.skyangle.set", newAngle), true);
        return 0;
    }

}
