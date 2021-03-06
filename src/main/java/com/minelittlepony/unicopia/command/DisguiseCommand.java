package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.NbtCompoundTagArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public class DisguiseCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.disguise.notfound"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
                .literal("disguise")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(context -> reveal(context.getSource(), context.getSource().getPlayer()));

        builder.then(CommandManager
                .argument("entity", EntitySummonArgumentType.entitySummon())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(context -> disguise(context.getSource(),
                        context.getSource().getPlayer(),
                        EntitySummonArgumentType.getEntitySummon(context, "entity"),
                        new CompoundTag(), true))
         .then(CommandManager.argument("nbt", NbtCompoundTagArgumentType.nbtCompound())
                .executes(context -> disguise(context.getSource(),
                        context.getSource().getPlayer(),
                        EntitySummonArgumentType.getEntitySummon(context, "entity"),
                        NbtCompoundTagArgumentType.getCompoundTag(context, "nbt"), false))
         ));

        dispatcher.register(builder);
    }

    static int disguise(ServerCommandSource source, PlayerEntity player, Identifier id, CompoundTag nbt, boolean isSelf) throws CommandSyntaxException {
        nbt = nbt.copy();
        nbt.putString("id", id.toString());

        Pony iplayer = Pony.of(player);

        Entity entity = EntityType.loadEntityWithPassengers(nbt, source.getWorld(), Function.identity());

        if (entity == null) {
            throw FAILED_EXCEPTION.create();
        }

        iplayer.getSpellSlot().get(SpellType.DISGUISE, true)
            .orElseGet(() -> SpellType.DISGUISE.apply(iplayer))
            .setDisguise(entity);

        if (!isSelf) {
            source.sendFeedback(new TranslatableText("commands.disguise.success.other", player.getName(), entity.getName()), true);
        } else {
            if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(new TranslatableText("commands.disguise.success.self", entity.getName()), false);
            }
            source.sendFeedback(new TranslatableText("commands.disguise.success.otherself", player.getName(), entity.getName()), true);
        }

        return 0;
    }

    static int reveal(ServerCommandSource source, PlayerEntity player) {
        Pony iplayer = Pony.of(player);
        iplayer.getSpellSlot().get(SpellType.DISGUISE, true).ifPresent(Spell::setDead);

        if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            player.sendMessage(new TranslatableText("commands.disguise.removed"), false);
        }

        return 0;
    }
}
