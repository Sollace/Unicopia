package com.minelittlepony.unicopia.diet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public record MultiplyHungerAffliction(float multiplier) implements Affliction {
    public static final Codec<MultiplyHungerAffliction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(MultiplyHungerAffliction::multiplier)
    ).apply(instance, MultiplyHungerAffliction::new));

    public MultiplyHungerAffliction(PacketByteBuf buffer) {
        this(buffer.readFloat());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(multiplier);
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.MULTIPLY_HUNGER;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        FoodComponent food = stack.getItem().getFoodComponent();
        player.getHungerManager().setFoodLevel((int)(food.getHunger() * multiplier));
        player.getHungerManager().setSaturationLevel(food.getSaturationModifier() * multiplier);
    }

    @Override
    public Text getName() {
        return Text.translatable("Lose %s%% hunger", multiplier * 100);
    }

}
