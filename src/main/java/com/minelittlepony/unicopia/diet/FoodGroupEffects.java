package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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

public record FoodGroupEffects(
        List<FoodGroupKey> tags,
        Optional<FoodComponent> foodComponent,
        Ailment ailment
) implements Effect {
    public static final Codec<FoodGroupEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FoodGroupKey.CODEC.listOf().fieldOf("tags").forGetter(FoodGroupEffects::tags),
            FoodAttributes.CODEC.optionalFieldOf("food_component").forGetter(FoodGroupEffects::foodComponent),
            Ailment.CODEC.fieldOf("ailment").forGetter(FoodGroupEffects::ailment)
    ).apply(instance, FoodGroupEffects::new));

    public FoodGroupEffects(PacketByteBuf buffer, Function<Identifier, FoodGroupKey> lookup) {
        this(buffer.readList(b -> lookup.apply(b.readIdentifier())), buffer.readOptional(FoodAttributes::read), new Ailment(buffer));
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context) {
        tags.forEach(tag -> {
            if (tag.contains(stack)) {
                tooltip.add(Text.literal(" ").append(Text.translatable(Util.createTranslationKey("tag", tag.id()))).formatted(Formatting.GRAY));
            }
        });
        Effect.super.appendTooltip(stack, tooltip, context);
    }
}