package com.minelittlepony.unicopia.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.command.ManaCommand.ManaType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class Commands {
    public static void bootstrap() {
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("animation"), Animation.ArgumentType.class, ConstantArgumentSerializer.of(Animation::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("animation_recipient"), Animation.Recipient.ArgumentType.class, ConstantArgumentSerializer.of(Animation.Recipient::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("mana_type"), ManaType.ArgumentType.class, ConstantArgumentSerializer.of(ManaType::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("trait_type"), Trait.ArgumentType.class, ConstantArgumentSerializer.of(Trait::argument));
        ArgumentTypeRegistry.registerArgumentType(Unicopia.id("spell_traits"), TraitsArgumentType.class, ConstantArgumentSerializer.of(TraitsArgumentType::traits));

        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            dispatcher.register(EmoteCommand.create());
            dispatcher.register(SpeciesCommand.create(environment));
            dispatcher.register(UnicopiaCommand.create(registries, environment));
        });
    }
}
