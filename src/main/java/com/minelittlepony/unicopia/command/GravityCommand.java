package com.minelittlepony.unicopia.command;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.minelittlepony.unicopia.entity.Living;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

class GravityCommand {

    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("gravity").requires(s -> s.hasPermissionLevel(2))
            .then(CommandManager.literal("get")
                            .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
                   .then(CommandManager.argument("target", EntityArgumentType.entity())
                            .executes(context -> get(context.getSource(), EntityArgumentType.getEntity(context, "target"), false))
                    ))
            .then(CommandManager.literal("set")
                   .then(CommandManager.argument("gravity", FloatArgumentType.floatArg(-99, 99))
                               .executes(context -> set(context.getSource(), List.of(context.getSource().getPlayer()), FloatArgumentType.getFloat(context, "gravity"), true))
                   .then(CommandManager.argument("target", EntityArgumentType.entities())
                               .executes(context -> set(context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "gravity"), false))
                   )));
    }

    static int get(ServerCommandSource source, Entity target, boolean isSelf) throws CommandSyntaxException {
        Living<?> l = Living.living(target);

        float gravity = l == null ? 1 : l.getPhysics().getGravityModifier();
        if (source.getEntity() == target) {
            source.sendFeedback(() -> Text.translatable("commands.gravity.get.self", gravity), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.gravity.get.other", target.getDisplayName(), gravity), true);
        }
        return 0;
    }

    static int set(ServerCommandSource source, Collection<? extends Entity> targets, float gravity, boolean isSelf) {
        List<Entity> affected = targets.stream().map(Living::living).filter(Objects::nonNull).map(l -> {
            l.getPhysics().setBaseGravityModifier(gravity);
            l.setDirty();
            if (l.asEntity() instanceof PlayerEntity player) {
                if (source.getEntity() == player) {
                    player.sendMessage(Text.translatable("commands.gravity.set.self", gravity));
                } else if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                    player.sendMessage(Text.translatable("commands.gravity.set.other", l.asEntity().getDisplayName(), gravity));
                }
            }
            return (Entity)l.asEntity();
        }).toList();

        if (affected.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.gravity.set.other", affected.get(0).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.gravity.set.multiple", affected.size()), true);
        }
        return 0;
    }
}
