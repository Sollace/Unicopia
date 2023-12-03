package com.minelittlepony.unicopia.diet;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;

final class FoodAttributes {
    static final Codec<FoodComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("hunger").forGetter(FoodComponent::getHunger),
            Codec.FLOAT.fieldOf("saturation").forGetter(FoodComponent::getSaturationModifier),
            Codec.BOOL.optionalFieldOf("petFood", false).forGetter(FoodComponent::isMeat),
            Codec.BOOL.optionalFieldOf("fastFood", false).forGetter(FoodComponent::isAlwaysEdible),
            Codec.BOOL.optionalFieldOf("eatenQuickly", false).forGetter(FoodComponent::isSnack)
    ).apply(instance, FoodAttributes::create));

    static FoodComponent create(int hunger, float saturation, boolean petFood, boolean fastFood, boolean eatenQuickly) {
        return create(hunger, saturation, petFood, fastFood, eatenQuickly, List.of()).build();
    }

    static FoodComponent.Builder create(int hunger, float saturation, boolean petFood, boolean fastFood, boolean eatenQuickly, List<Pair<StatusEffectInstance, Float>> effects) {
        var builder = new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation);
        if (petFood) {
            builder.meat();
        }
        if (fastFood) {
            builder.alwaysEdible();
        }
        if (eatenQuickly) {
            builder.snack();
        }
        for (var effect : effects) {
            builder.statusEffect(effect.getFirst(), effect.getSecond());
        }
        return builder;
    }

    static FoodComponent.Builder copy(FoodComponent food) {
        return create(food.getHunger(), food.getSaturationModifier(), food.isMeat(), food.isAlwaysEdible(), food.isSnack(), food.getStatusEffects());
    }

    static FoodComponent read(PacketByteBuf buffer) {
        return create(buffer.readInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    static void write(PacketByteBuf buffer, FoodComponent food) {
        buffer.writeInt(food.getHunger());
        buffer.writeFloat(food.getSaturationModifier());
        buffer.writeBoolean(food.isMeat());
        buffer.writeBoolean(food.isAlwaysEdible());
        buffer.writeBoolean(food.isSnack());
    }
}
