package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
                var bar = type.getBar(pony.getMagicalReserves());

                source.getSource().sendFeedback(() -> Text.literal(type.name() + " is " + bar.get() + "/" + bar.getMax()), true);
                return 0;
            })
            .then(CommandManager.argument("value", FloatArgumentType.floatArg()).executes(source -> {
                var type = source.getArgument("type", ManaType.class);
                var pony = Pony.of(source.getSource().getPlayer());
                var bar = type.getBar(pony.getMagicalReserves());

                float value = source.getArgument("value", Float.class);
                if (type == ManaType.LEVEL) {
                    pony.getLevel().set((int)value);
                    value -= (int)value;
                    type = ManaType.XP;
                }
                if (type == ManaType.XP) {
                    int currentLevel = pony.getLevel().get();
                    while (type == ManaType.XP && value > 1) {
                        currentLevel++;
                        value -= 1;
                    }
                    pony.getLevel().set(currentLevel);
                    pony.asWorld().playSound(null, pony.getOrigin(), USounds.Vanilla.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
                }
                bar.set(value);
                var t = type;
                source.getSource().sendFeedback(() -> Text.literal("Set " + t.name() + " to " + bar.get() + "/" + bar.getMax()), true);
                return 0;
            })));
    }

    enum ManaType implements CommandArgumentEnum<ManaType> {
        EXERTION(MagicReserves::getExertion),
        EXHAUSTION(MagicReserves::getExhaustion),
        ENERGY(MagicReserves::getEnergy),
        MANA(MagicReserves::getMana),
        XP(MagicReserves::getXp),
        LEVEL(MagicReserves::getXp);

        private final Function<MagicReserves, MagicReserves.Bar> getter;

        ManaType(Function<MagicReserves, MagicReserves.Bar> getter) {
            this.getter = getter;
        }

        public MagicReserves.Bar getBar(MagicReserves reserves) {
            return getter.apply(reserves);
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
