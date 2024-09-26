package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.item.UFoodComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
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

    public static final class Builder {
        private final List<FoodGroupKey> tags = new ArrayList<>();
        private Optional<FoodComponent> foodComponent = Optional.empty();
        private Ailment ailment = new Ailment(Affliction.EMPTY);

        public Builder tag(Identifier tag) {
            return tag(TagKey.of(RegistryKeys.ITEM, tag));
        }

        public Builder tag(TagKey<Item> tag) {
            tags.add(FoodGroupKey.TAG_LOOKUP.apply(tag));
            return this;
        }

        public Builder ailment(Affliction affliction) {
            ailment = new Ailment(affliction);
            return this;
        }

        public Builder food(int hunger, float saturation) {
            return food(UFoodComponents.builder(hunger, saturation));
        }

        public Builder food(FoodComponent.Builder food) {
            return food(food.build());
        }

        public Builder food(FoodComponent food) {
            foodComponent = Optional.of(food);
            return this;
        }

        public FoodGroupEffects build() {
            return new FoodGroupEffects(tags, foodComponent, ailment);
        }
    }
}