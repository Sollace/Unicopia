package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;

public record LoseHungerAffliction(float multiplier) implements Affliction {
    public static final MapCodec<LoseHungerAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(LoseHungerAffliction::multiplier)
    ).apply(instance, LoseHungerAffliction::new));
    public static final PacketCodec<ByteBuf, LoseHungerAffliction> PACKET_CODEC = PacketCodecs.FLOAT.xmap(LoseHungerAffliction::new, LoseHungerAffliction::multiplier);

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
