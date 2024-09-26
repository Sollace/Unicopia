package com.minelittlepony.unicopia.diet;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;

final class FoodAttributes {
    static final Codec<FoodComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("hunger").forGetter(FoodComponent::nutrition),
            Codec.FLOAT.fieldOf("saturation").forGetter(FoodComponent::saturation),
            Codec.BOOL.optionalFieldOf("fastFood", false).forGetter(FoodComponent::canAlwaysEat),
            Codec.BOOL.optionalFieldOf("eatenQuickly", false).forGetter(food -> food.eatSeconds() < 1.6F),
            ItemStack.CODEC.optionalFieldOf("usingConvertsInto").forGetter(FoodComponent::usingConvertsTo)
    ).apply(instance, (nutrition, saturation, fastFood, eatenQuickly, convertsInto) -> {
        return new FoodComponent(nutrition, saturation, fastFood, eatenQuickly ? 0.8F : 1.6F, convertsInto, List.of());
    }));

    @Deprecated
    static FoodComponent read(RegistryByteBuf buffer) {
        return FoodComponent.PACKET_CODEC.decode(buffer);
    }

    @Deprecated
    static void write(RegistryByteBuf buffer, FoodComponent food) {
        FoodComponent.PACKET_CODEC.encode(buffer, food);
    }
}
