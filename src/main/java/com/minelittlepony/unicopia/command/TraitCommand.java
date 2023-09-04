package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Hand;

class TraitCommand {
    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("trait").requires(s -> s.hasPermissionLevel(2))
            .then(CommandManager.literal("add")
                    .then(CommandManager.argument("trait", Trait.argument())
                    .then(CommandManager.argument("value", FloatArgumentType.floatArg()).executes(source -> add(
                            source.getSource(),
                            source.getSource().getPlayer(),
                            source.getArgument("trait", Trait.class),
                            FloatArgumentType.getFloat(source, "value")
                    )))
            ))
            .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("trait", Trait.argument()).executes(source -> remove(
                            source.getSource(),
                            source.getSource().getPlayer(),
                            source.getArgument("trait", Trait.class)
                    ))
            ));
    }

    static int add(ServerCommandSource source, PlayerEntity player, Trait trait, float amount) throws CommandSyntaxException {
        if (trait == null) {
            source.sendError(Text.literal("Invalid trait"));
            return 0;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            source.sendError(Text.literal("That trait cannot be added to the current item"));
            return 0;
        }

        player.setStackInHand(Hand.MAIN_HAND, SpellTraits.union(SpellTraits.of(stack), new SpellTraits.Builder().with(trait, amount).build()).applyTo(stack));

        return 0;
    }

    static int remove(ServerCommandSource source, PlayerEntity player, Trait trait) throws CommandSyntaxException {
        if (trait == null) {
            source.sendError(Text.literal("Invalid trait"));
            return 0;
        }

        ItemStack stack = player.getMainHandStack();

        SpellTraits existing = SpellTraits.of(stack);
        if (existing.get(trait) == 0) {
            return 0;
        }

        player.setStackInHand(Hand.MAIN_HAND, existing.map((t, v) -> t == trait ? 0 : v).applyTo(stack));

        return 0;
    }

    static int get(ServerCommandSource source, PlayerEntity player, Trait trait, float amount) throws CommandSyntaxException {
        String translationKey = "commands.gravity.get";

        Pony iplayer = Pony.of(player);

        float gravity = iplayer.getPhysics().getGravityModifier();

        if (source.getPlayer() == player) {
            player.sendMessage(Text.translatable(translationKey, gravity), false);
        } else {
            source.sendFeedback(() -> Text.translatable(translationKey + ".other", player.getName(), gravity), true);
        }

        return 0;
    }
}
