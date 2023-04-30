package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.server.world.WorldTribeManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

class WorldTribeCommand {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
                .literal("worldtribe")
                .requires(s -> s.hasPermissionLevel(4));

        builder.then(CommandManager.literal("get").executes(context -> get(context.getSource())));
        builder.then(CommandManager.literal("set")
                .then(CommandManager.argument("race", Race.argument())
                .executes(context -> set(context.getSource(), Race.fromArgument(context, "race")))));

        dispatcher.register(builder);
    }

    static int get(ServerCommandSource source) throws CommandSyntaxException {
        WorldTribeManager manager = WorldTribeManager.forWorld(source.getWorld());
        source.sendFeedback(Text.translatable("commands.worldtribe.success.get", manager.getDefaultRace().getDisplayName()), true);
        return 0;
    }

    static int set(ServerCommandSource source, Race race) {
        WorldTribeManager manager = WorldTribeManager.forWorld(source.getWorld());
        manager.setDefaultRace(race);

        source.sendFeedback(Text.translatable("commands.worldtribe.success.set", race.getDisplayName()), true);
        return 0;
    }
}
