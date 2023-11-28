package com.minelittlepony.unicopia.diet;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

public interface Affliction {
    Text NO_EFFECT_TEXT = Text.of("No Effect");
    Affliction EMPTY = new Affliction() {
        @Override
        public void afflict(PlayerEntity player, ItemStack stack) { }

        @Override
        public Text getName() {
            return NO_EFFECT_TEXT;
        }

        @Override
        public AfflictionType<?> getType() {
            return AfflictionType.EMPTY;
        }

        @Override
        public void toBuffer(PacketByteBuf buffer) { }
    };
    Codec<Affliction> CODEC = Codecs.xor(Codec.list(AfflictionType.CODEC)
            .mapResult(null)
            .xmap(
            afflictions -> {
                afflictions.removeIf(f -> f.getType() == AfflictionType.EMPTY);
                return switch (afflictions.size()) {
                    case 0 -> EMPTY;
                    case 1 -> afflictions.get(0);
                    default -> new CompoundAffliction(afflictions);
                };
            },
            affliction -> ((CompoundAffliction)affliction).afflictions
    ), AfflictionType.CODEC).xmap(
            either -> either.left().or(either::right).get(),
            affliction -> affliction instanceof CompoundAffliction ? Either.left(affliction) : Either.right(affliction)
    );

    void afflict(PlayerEntity player, ItemStack stack);

    default void appendTooltip(List<Text> tooltip) {
        tooltip.add(getName());
    }

    Text getName();

    AfflictionType<?> getType();

    void toBuffer(PacketByteBuf buffer);

    static void write(PacketByteBuf buffer, Affliction affliction) {
        buffer.writeIdentifier(affliction.getType().id());
        affliction.toBuffer(buffer);
    }

    static Affliction read(PacketByteBuf buffer) {
        return AfflictionType.REGISTRY.get(buffer.readIdentifier()).reader().apply(buffer);
    }
}
