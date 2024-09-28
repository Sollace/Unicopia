package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface Affliction {
    Codec<Affliction> SINGLE_CODEC = AfflictionType.CODEC.dispatch("type", affliction -> affliction.getType(), type -> type.codec());
    Codec<Affliction> CODEC = Codec.xor(SINGLE_CODEC, Codec.list(SINGLE_CODEC).xmap(
            afflictions -> {
                afflictions = afflictions.stream().filter(f -> !f.isEmpty()).toList();
                return switch (afflictions.size()) {
                    case 0 -> EmptyAffliction.INSTANCE;
                    case 1 -> afflictions.get(0);
                    default -> new CompoundAffliction(afflictions);
                };
            },
            affliction -> ((CompoundAffliction)affliction).afflictions()
    )).xmap(
            either -> either.left().or(either::right).get(),
            affliction -> affliction instanceof CompoundAffliction ? Either.right(affliction) : Either.left(affliction)
    );
    PacketCodec<RegistryByteBuf, Affliction> PACKET_CODEC = AfflictionType.PACKET_CODEC.dispatch(Affliction::getType, AfflictionType::packetCodec);

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
}
