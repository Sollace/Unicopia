package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public class ManaCommand {
    static LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal("mana").requires(s -> s.hasPermissionLevel(2))
            .then(CommandManager.argument("type", ManaType.argument()).executes(source -> {
                var type = source.getArgument("type", ManaType.class);
                var pony = Pony.of(source.getSource().getPlayer());
                var bar = type.getBar(pony);

                source.getSource().sendFeedback(() -> Text.literal(type.name() + " is " + Either.unwrap(bar.mapBoth(
                        left -> left.get() + "/" + left.getMax(),
                        right -> right.get() + "/" + right.getMax()))), true);
                return 0;
            })
            .then(CommandManager.argument("value", FloatArgumentType.floatArg()).executes(source -> {
                var type = source.getArgument("type", ManaType.class);
                var pony = Pony.of(source.getSource().getPlayer());
                float value = source.getArgument("value", Float.class);

                var answer = type.getBar(pony).mapBoth(left -> {
                    left.set((int)value);
                    return left.get() + "/" + left.getMax();
                }, right -> {
                    right.set(value);
                    return right.get() + "/" + right.getMax();
                });
                pony.asWorld().playSound(null, pony.getOrigin(), USounds.Vanilla.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);

                source.getSource().sendFeedback(() -> Text.literal("Set " + type.name() + " to " + Either.unwrap(answer)), true);
                return 0;
            })));
    }

    enum ManaType implements CommandArgumentEnum<ManaType> {
        EXERTION(pony -> Either.right(pony.getMagicalReserves().getExertion())),
        EXHAUSTION(pony -> Either.right(pony.getMagicalReserves().getExhaustion())),
        ENERGY(pony -> Either.right(pony.getMagicalReserves().getEnergy())),
        MANA(pony -> Either.right(pony.getMagicalReserves().getMana())),
        XP(pony -> Either.right(pony.getMagicalReserves().getXp())),
        LEVEL(pony -> Either.left(pony.getLevel())),
        CORRUPTION(pony -> Either.left(pony.getCorruption()));

        private final Function<Pony, Either<Levelled.LevelStore, MagicReserves.Bar>> getter;

        ManaType(Function<Pony, Either<Levelled.LevelStore, MagicReserves.Bar>> getter) {
            this.getter = getter;
        }

        public Either<Levelled.LevelStore, MagicReserves.Bar> getBar(Pony pony) {
            return getter.apply(pony);
        }

        public static EnumArgumentType<ManaType> argument() {
            return new ArgumentType();
        }

        public static final class ArgumentType extends EnumArgumentType<ManaType> {
            static final Codec<ManaType> CODEC = StringIdentifiable.createCodec(ManaType::values);

            protected ArgumentType() {
                super(CODEC, ManaType::values);
            }
        }
    }
}
