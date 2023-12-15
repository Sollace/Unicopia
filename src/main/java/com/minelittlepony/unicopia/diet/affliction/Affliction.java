package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;

public interface Affliction {
    Affliction EMPTY = new Affliction() {
        @Override
        public void afflict(PlayerEntity player, ItemStack stack) { }

        @Override
        public AfflictionType<?> getType() {
            return AfflictionType.EMPTY;
        }

        @Override
        public void toBuffer(PacketByteBuf buffer) { }
    };
    Codec<Affliction> CODEC = Codecs.xor(AfflictionType.CODEC, Codec.list(AfflictionType.CODEC).xmap(
            afflictions -> {
                afflictions = afflictions.stream().filter(f -> !f.isEmpty()).toList();
                return switch (afflictions.size()) {
                    case 0 -> EMPTY;
                    case 1 -> afflictions.get(0);
                    default -> new CompoundAffliction(afflictions);
                };
            },
            affliction -> ((CompoundAffliction)affliction).afflictions
    )).xmap(
            either -> either.left().or(either::right).get(),
            affliction -> affliction instanceof CompoundAffliction ? Either.left(affliction) : Either.right(affliction)
    );

    void afflict(PlayerEntity player, ItemStack stack);

    default boolean isEmpty() {
        return getType() == AfflictionType.EMPTY;
    }

    default void appendTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal(" ").append(getName()).formatted(Formatting.DARK_GRAY));
    }

    default Text getName() {
        return Text.translatable(getType().getTranslationKey());
    }

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
