package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.item.UFoodComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record FoodGroupEffects(
        List<FoodGroupKey> tags,
        Optional<FoodAttributes> foodAttributes,
        Ailment ailment
) implements Effect {
    public static Codec<FoodGroupEffects> createCodec(Codec<FoodGroupKey> keyCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                keyCodec.listOf().fieldOf("tags").forGetter(FoodGroupEffects::tags),
                FoodAttributes.CODEC.optionalFieldOf("food_attributes").forGetter(FoodGroupEffects::foodAttributes),
                Ailment.CODEC.fieldOf("ailment").forGetter(FoodGroupEffects::ailment)
        ).apply(instance, FoodGroupEffects::new));
    }
    public static final PacketCodec<RegistryByteBuf, FoodGroupEffects> createPacketCodec(PacketCodec<ByteBuf, FoodGroupKey> keyCodec) {
        return PacketCodec.tuple(
                keyCodec.collect(PacketCodecs.toList()), FoodGroupEffects::tags,
                PacketCodecs.optional(FoodAttributes.PACKET_CODEC), FoodGroupEffects::foodAttributes,
                Ailment.PACKET_CODEC, FoodGroupEffects::ailment,
                FoodGroupEffects::new
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, Consumer<Text> tooltip, TooltipType context) {
        tags.forEach(tag -> {
            if (tag.contains(stack)) {
                tooltip.accept(Text.literal(" ").append(Text.translatable(Util.createTranslationKey("tag", tag.id()))).formatted(Formatting.GRAY));
            }
        });
        Effect.super.appendTooltip(stack, tooltip, context);
    }

    public static final class Builder {
        private final List<FoodGroupKey> tags = new ArrayList<>();
        private Optional<FoodAttributes> food = Optional.empty();
        private Ailment ailment = Ailment.EMPTY;

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
            return food(UFoodComponents.builder(hunger, saturation), null);
        }

        public Builder food(FoodComponent.Builder food) {
            return food(food, null);
        }

        public Builder food(FoodComponent.Builder food, @Nullable ConsumableComponent.Builder consumable) {
            return food(food.build(), consumable == null ? null : consumable.build());
        }

        public Builder food(FoodComponent food) {
            return food(food, null);
        }

        public Builder food(FoodComponent food, @Nullable ConsumableComponent consumable) {
            this.food = Optional.of(new FoodAttributes(food, Optional.ofNullable(consumable)));
            return this;
        }

        public FoodGroupEffects build() {
            return new FoodGroupEffects(tags, food, ailment);
        }
    }
}
