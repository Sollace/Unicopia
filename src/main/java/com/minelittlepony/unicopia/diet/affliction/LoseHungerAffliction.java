package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public record LoseHungerAffliction(float multiplier) implements Affliction {
    public static final Codec<LoseHungerAffliction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(LoseHungerAffliction::multiplier)
    ).apply(instance, LoseHungerAffliction::new));

    public LoseHungerAffliction(PacketByteBuf buffer) {
        this(buffer.readFloat());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(multiplier);
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.LOSE_HUNGER;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        var hunger = player.getHungerManager();
        hunger.setFoodLevel((int)(hunger.getFoodLevel() * multiplier));
        hunger.setSaturationLevel(hunger.getSaturationLevel() * multiplier);
    }

    @Override
    public Text getName() {
        return Text.translatable(getType().getTranslationKey(), multiplier * 100);
    }
}
