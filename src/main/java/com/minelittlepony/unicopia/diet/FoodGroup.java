package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record FoodGroup(
        Identifier id,
        FoodGroupEffects attributes) implements Effect {
    public static final Codec<FoodGroupEffects> EFFECTS_CODEC = FoodGroupEffects.createCodec(FoodGroupKey.TAG_CODEC);
    public static final PacketCodec<RegistryByteBuf, FoodGroup> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, FoodGroup::id,
            FoodGroupEffects.createPacketCodec(FoodGroupKey.TAG_PACKET_CODEC), FoodGroup::attributes,
            FoodGroup::new
    );

    @Override
    public List<FoodGroupKey> tags() {
        return attributes.tags();
    }

    @Override
    public Optional<FoodComponent> foodComponent() {
        return attributes.foodComponent();
    }

    @Override
    public Ailment ailment() {
        return attributes.ailment();
    }
    @Override
    public void appendTooltip(ItemStack stack, Consumer<Text> tooltip, TooltipType context) {
        tooltip.accept(Text.literal(" ").append(Text.translatable(Util.createTranslationKey("food_group", id()))).formatted(Formatting.GRAY));
        Effect.super.appendTooltip(stack, tooltip, context);
    }
}
