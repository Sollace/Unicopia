package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

class WorldTribeCommand {
    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("worldtribe").requires(s -> s.hasPermissionLevel(3))
            .then(CommandManager.literal("get").executes(context -> get(context.getSource())))
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("race", Race.argument())
                .executes(context -> set(context.getSource(), Race.fromArgument(context, "race")))));
    }

    static int get(ServerCommandSource source) throws CommandSyntaxException {
        source.sendFeedback(() -> {
            UnicopiaWorldProperties manager = UnicopiaWorldProperties.forWorld(source.getWorld());
            return Text.translatable("commands.worldtribe.success.get", manager.getDefaultRace().getDisplayName());
        }, true);
        return 0;
    }

    static int set(ServerCommandSource source, Race race) {
        UnicopiaWorldProperties manager = UnicopiaWorldProperties.forWorld(source.getWorld());
        manager.setDefaultRace(race);

        source.sendFeedback(() -> Text.translatable("commands.worldtribe.success.set", race.getDisplayName()), true);
        return 0;
    }
}
