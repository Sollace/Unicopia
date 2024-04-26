package com.minelittlepony.unicopia.command;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import com.minelittlepony.common.util.settings.Setting;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgConfigurationChange;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ConfigCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> create(CommandRegistryAccess registries) {
        return CommandManager.literal("config").requires(s -> s.hasPermissionLevel(2))
            .then(createSet("dimensionswithoutatmosphere", createSuggestion(registries, RegistryKeys.DIMENSION_TYPE)))
            .then(createSet("wantitneeditentityexcludelist", createSuggestion(registries, RegistryKeys.ENTITY_TYPE)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createSet(String configName, SuggestionProvider<ServerCommandSource> suggestions) {
        return CommandManager.literal(configName)
            .then(CommandManager.literal("clear").executes(source -> {
                        source.getSource().sendFeedback(() -> Text.translatable("command.unicopia.config.clear", configName), true);
                        return changeProperty(source.getSource(), configName, values -> new HashSet<>());
                    }))
            .then(CommandManager.literal("add").then(
                CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(suggestions).executes(source -> ConfigCommand.<Set<String>>changeProperty(source.getSource(), configName, values -> {
                        String value = IdentifierArgumentType.getIdentifier(source, "id").toString();
                        source.getSource().sendFeedback(() -> Text.translatable("command.unicopia.config.add", value, configName), true);
                        values.add(value);
                        return values;
                    })))
            )
            .then(CommandManager.literal("remove").then(
                CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests((context, builder) -> {
                    return CommandSource.suggestIdentifiers(Unicopia.getConfig().getCategory("server").<Set<String>>get(configName).get().stream()
                            .map(Identifier::tryParse)
                            .filter(Objects::nonNull)
                            .toList(), builder);
                }).executes(source -> ConfigCommand.<Set<String>>changeProperty(source.getSource(), configName, values -> {
                        String value = IdentifierArgumentType.getIdentifier(source, "id").toString();
                        source.getSource().sendFeedback(() -> Text.translatable("command.unicopia.config.remove", value, configName), true);
                        values.remove(value);
                        return values;
                    })))
            )
            .then(CommandManager.literal("list").executes(source -> ConfigCommand.<Set<String>>getProperty(configName, values -> {
                    ServerPlayerEntity player = source.getSource().getPlayerOrThrow();

                    player.sendMessage(Text.translatable("command.unicopia.config.list", configName, values.size()), false);
                    values.forEach(line -> player.sendMessage(Text.literal(line)));
                }))
            );
    }

    private static <T> SuggestionProvider<ServerCommandSource> createSuggestion(CommandRegistryAccess registries, RegistryKey<Registry<T>> registryKey) {
        RegistryWrapper<T> wrapper = registries.createWrapper(registryKey);
        return (context, builder) -> CommandSource.suggestIdentifiers(wrapper.streamKeys().map(RegistryKey::getValue), builder);
    }

    private static <T> int changeProperty(ServerCommandSource source, String configName, Function<T, T> changer) {
        Config config = Unicopia.getConfig();
        Setting<T> setting = config.getCategory("server").get(configName);
        setting.set(changer.apply(setting.get()));
        config.save();

        MsgConfigurationChange msg = new MsgConfigurationChange(config.toSynced());
        InteractionManager.getInstance().setSyncedConfig(msg.config());
        source.getServer().getPlayerManager().getPlayerList().forEach(recipient -> {
            Channel.CONFIGURATION_CHANGE.sendToPlayer(msg, recipient);
        });

        return 0;
    }

    private static <T> int getProperty(String configName, UnsafeConsumer<T> reader) throws CommandSyntaxException {
        Config config = Unicopia.getConfig();
        Setting<T> setting = config.getCategory("server").get(configName);
        reader.accept(setting.get());
        return 0;
    }

    interface UnsafeConsumer<T> {
        void accept(T t) throws CommandSyntaxException;
    }
}
