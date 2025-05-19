package com.minelittlepony.unicopia.command;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.TextHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class RacelistCommand {

    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("racelist").requires(s -> s.hasPermissionLevel(3))
            .then(CommandManager.literal("show")
            .executes(context -> {
                context.getSource().sendFeedback(() -> {
                    if (!AllowList.INSTANCE.isEnabled()) {
                        return Text.translatable("commands.racelist.inactive");
                    }
                    Set<MutableText> allowed = new HashSet<>();
                    Set<MutableText> unallowed = new HashSet<>();
                    Race.REGISTRY.forEach(race -> {
                        (AllowList.INSTANCE.permits(race) ? allowed : unallowed).add(Text.translatable("commands.racelist.get.list_item",
                                race.getDisplayName(),
                                Text.literal(race.getId().toString()).formatted(Formatting.GRAY)
                        ));
                    });

                    return Text.translatable("commands.racelist.get.allowed", allowed.size()).formatted(Formatting.YELLOW)
                            .append("\n").append(TextHelper.join(Text.literal("\n"), allowed))
                            .append("\n")
                            .append(Text.translatable("commands.racelist.get.not_allowed", unallowed.size()).formatted(Formatting.YELLOW))
                            .append("\n").append(TextHelper.join(Text.literal("\n"), unallowed));
                }, false);
                return 0;
            })
            )
            .then(CommandManager.literal("reset")
            .executes(context -> {
                boolean success = AllowList.INSTANCE.disable();
                context.getSource().sendFeedback(() -> Text.translatable("commands.racelist.reset." + (success ? "success" : "fail")).formatted(Formatting.YELLOW), false);
                return 0;
            })
            )
            .then(CommandManager.literal("allow")
                .then(CommandManager.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "allowed", AllowList.INSTANCE::add))
            ))
            .then(CommandManager.literal("disallow")
                .then(CommandManager.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "disallowed", AllowList.INSTANCE::remove))
            ));
    }

    static int toggle(ServerCommandSource source, ServerPlayerEntity player, Race race, String action, Function<Race, Boolean> func) {
        boolean enabled = AllowList.INSTANCE.isEnabled();
        boolean success = func.apply(race);

        if (enabled != AllowList.INSTANCE.isEnabled()) {
            source.sendFeedback(() -> Text.translatable("commands.racelist." + (enabled ? "disabled" : "enabled")).formatted(enabled ? Formatting.RED : Formatting.GREEN), false);
        }

        source.sendFeedback(() -> {
            String translationKey = "commands.racelist." + action;
            if (!success) {
                if (race.isUnset()) {
                    translationKey = "commands.racelist.illegal";
                } else {
                    translationKey += ".failed";
                }
            }

            return Text.translatable(translationKey, race.getDisplayName().copy().formatted(Formatting.GOLD)).formatted(success ? Formatting.GREEN : Formatting.RED);
        }, false);
        return 0;
    }
}
