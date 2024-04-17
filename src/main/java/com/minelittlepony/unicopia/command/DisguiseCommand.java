package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.*;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

public class DisguiseCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.disguise.notfound"));

    public static LiteralArgumentBuilder<ServerCommandSource> create(CommandRegistryAccess registries) {
        return CommandManager.literal("disguise").requires(s -> s.hasPermissionLevel(2))
            .executes(context -> reveal(context.getSource(), context.getSource().getPlayer()))
            .then(
                CommandManager.argument("target", EntityArgumentType.players())
                .then(buildEntityDisguise(context -> EntityArgumentType.getPlayer(context, "target"), registries))
                .then(buildPlayerDisguise(context -> EntityArgumentType.getPlayer(context, "target")))
            )
            .then(buildEntityDisguise(context -> context.getSource().getPlayer(), registries))
            .then(buildPlayerDisguise(context -> context.getSource().getPlayer()));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildEntityDisguise(Arg<ServerPlayerEntity> targetOp, CommandRegistryAccess registries) {
        return CommandManager.argument("entity", RegistryEntryArgumentType.registryEntry(registries, RegistryKeys.ENTITY_TYPE))
                    .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadEntity(context.getSource(),
                            RegistryEntryArgumentType.getSummonableEntityType(context, "entity"),
                            new NbtCompound())))
        .then(
                CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound())
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadEntity(context.getSource(),
                            RegistryEntryArgumentType.getSummonableEntityType(context, "entity"),
                            NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))
        );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildPlayerDisguise(Arg<ServerPlayerEntity> targetOp) {
        return CommandManager.argument("playername", StringArgumentType.string())
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadPlayer(context.getSource(), StringArgumentType.getString(context, "playername"))));
    }

    static int disguise(ServerCommandSource source, PlayerEntity player, Entity entity) throws CommandSyntaxException {
        if (entity == null || !EquinePredicates.VALID_FOR_DISGUISE.test(entity)) {
            throw FAILED_EXCEPTION.create();
        }

        Pony iplayer = Pony.of(player);
        iplayer.getSpellSlot().get(SpellType.CHANGELING_DISGUISE, true)
            .orElseGet(() -> SpellType.CHANGELING_DISGUISE.withTraits().apply(iplayer, CastingMethod.INNATE))
            .setDisguise(entity);

        if (source.getEntity() == player) {
            source.sendFeedback(() -> Text.translatable("commands.disguise.success.self", entity.getName()), true);
        } else {
            if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(Text.translatable("commands.disguise.success", entity.getName()));
            }

            source.sendFeedback(() -> Text.translatable("commands.disguise.success.other", player.getName(), entity.getName()), true);
        }

        return 0;
    }

    static Entity loadEntity(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, NbtCompound nbt) {
        nbt = nbt.copy();
        nbt.putString("id", entityType.registryKey().getValue().toString());
        return EntityType.loadEntityWithPassengers(nbt, source.getWorld(), Function.identity());
    }

    static Entity loadPlayer(ServerCommandSource source, String username) {
        return InteractionManager.getInstance().createPlayer(source.getWorld(), new GameProfile(null, username));
    }

    static int reveal(ServerCommandSource source, PlayerEntity player) {
        Pony iplayer = Pony.of(player);
        iplayer.getSpellSlot().removeIf(SpellPredicate.IS_DISGUISE, true);

        if (source.getEntity() == player) {
            source.sendFeedback(() -> Text.translatable("commands.disguise.removed.self"), true);
        } else {
            if (player.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(Text.translatable("commands.disguise.removed"));
            }

            source.sendFeedback(() -> Text.translatable("commands.disguise.removed.other", player.getName()), true);
        }

        return 0;
    }

    interface Arg<T> {
        T apply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
    }
}
