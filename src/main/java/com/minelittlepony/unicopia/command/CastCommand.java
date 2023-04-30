package com.minelittlepony.unicopia.command;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class CastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(
            CommandManager.literal("cast").requires(s -> s.hasPermissionLevel(2))
            .then(
                buildBranches(
                    CommandManager.argument("spell", RegistryKeyArgumentType.registryKey(SpellType.REGISTRY_KEY)),
                    c -> Optional.empty()
                )
            )
            .then(
                CommandManager.argument("spell", RegistryKeyArgumentType.registryKey(SpellType.REGISTRY_KEY))
                .then(buildBranches(
                        CommandManager.argument("traits", TraitsArgumentType.traits()),
                        CastCommand::getTraits
                ))
            )
        );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildBranches(ArgumentBuilder<ServerCommandSource, ?> builder,
            TraitsFunc traitsFunc) {
        return builder.then(
                CommandManager.literal("throw").then(
                    CommandManager.argument("rot", RotationArgumentType.rotation()).executes(c -> thrown(c, traitsFunc, 1.5F)).then(
                        CommandManager.argument("speed", FloatArgumentType.floatArg(0, 10)).executes(c -> thrown(c,
                                traitsFunc,
                                FloatArgumentType.getFloat(c, "speed")
                        ))
                    )
                )
            )
            .then(
                CommandManager.literal("place").executes(c -> placed(c, traitsFunc, Optional.empty(), c.getSource().getRotation())).then(
                    CommandManager.argument("loc", BlockPosArgumentType.blockPos()).executes(c -> placed(c,
                                traitsFunc,
                                Optional.of(BlockPosArgumentType.getBlockPos(c, "location").toCenterPos()),
                                c.getSource().getRotation()
                        )).then(
                        CommandManager.argument("rot", RotationArgumentType.rotation()).executes(c -> placed(c,
                                traitsFunc,
                                Optional.of(BlockPosArgumentType.getBlockPos(c, "location").toCenterPos()),
                                RotationArgumentType.getRotation(c, "rot").toAbsoluteRotation(c.getSource())
                        ))
                    )
                )
            )
            .then(
                CommandManager.literal("apply").then(
                    CommandManager.argument("targets", EntityArgumentType.entities()).executes(c -> apply(c, traitsFunc))
                )
            );
    }

    private static Optional<SpellTraits> getTraits(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        return Optional.of(TraitsArgumentType.getSpellTraits(source, "traits"));
    }

    private static CustomisedSpellType<?> getSpell(CommandContext<ServerCommandSource> source, TraitsFunc traits) throws CommandSyntaxException {
        SpellType<?> spellType = SpellType.fromArgument(source, "spell");
        return spellType.withTraits(traits.getTraits(source).orElse(spellType.getTraits()));
    }

    private static int thrown(CommandContext<ServerCommandSource> source, TraitsFunc traits, float speed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getSource().getPlayerOrThrow();
        getSpell(source, traits).create().toThrowable().throwProjectile(Caster.of(player).orElseThrow()).ifPresent(projectile -> {
            Vec2f rotation = RotationArgumentType.getRotation(source, "rot").toAbsoluteRotation(source.getSource());
            projectile.setVelocity(player, rotation.x, rotation.y, 0, speed, 1);
        });

        return 0;
    }

    private static int placed(CommandContext<ServerCommandSource> source, TraitsFunc traits, Optional<Vec3d> position, Vec2f rotation) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getSource().getPlayerOrThrow();
        PlaceableSpell spell = getSpell(source, traits).create().toPlaceable();
        Caster<?> caster = Caster.of(player).orElseThrow();

        spell.setOrientation(rotation.x, rotation.y);
        spell.apply(caster);

        position.ifPresent(pos -> {
            spell.tick(caster, Situation.BODY);
            CastSpellEntity entity = spell.getSpellEntity(caster).orElseThrow();
            entity.setPosition(pos);
        });


        return 0;
    }

    private static int apply(CommandContext<ServerCommandSource> source, TraitsFunc traits) throws CommandSyntaxException {
        CustomisedSpellType<?> spellType = getSpell(source, traits);
        EntityArgumentType.getEntities(source, "targets").forEach(target -> {
            Caster.of(target).ifPresent(caster -> spellType.apply(caster));
        });

        return 0;
    }

    interface TraitsFunc {
        Optional<SpellTraits> getTraits(CommandContext<ServerCommandSource> source) throws CommandSyntaxException;
    }
}
