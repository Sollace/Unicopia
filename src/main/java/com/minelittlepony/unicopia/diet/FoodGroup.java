package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record FoodGroup(
        Identifier id,
        FoodGroupEffects attributes) implements Effect {
    public static final Codec<FoodGroupEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FoodGroupKey.TAG_CODEC.listOf().fieldOf("tags").forGetter(FoodGroupEffects::tags),
            FoodAttributes.CODEC.optionalFieldOf("food_component").forGetter(FoodGroupEffects::foodComponent),
            Ailment.CODEC.fieldOf("ailment").forGetter(FoodGroupEffects::ailment)
    ).apply(instance, FoodGroupEffects::new));

    public FoodGroup(PacketByteBuf buffer) {
        this(buffer.readIdentifier(), new FoodGroupEffects(buffer, FoodGroupKey.TAG_ID_LOOKUP));
    }

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
    public void appendTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal(" ").append(Text.translatable(Util.createTranslationKey("food_group", id()))).formatted(Formatting.GRAY));
        Effect.super.appendTooltip(stack, tooltip, context);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeIdentifier(id());
        Effect.super.toBuffer(buffer);
    }
}
