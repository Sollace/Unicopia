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

public record HealingAffliction(float health) implements Affliction {
    public static final MapCodec<HealingAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("health").forGetter(HealingAffliction::health)
    ).apply(instance, HealingAffliction::new));
    public static final PacketCodec<ByteBuf, HealingAffliction> PACKET_CODEC = PacketCodecs.FLOAT.xmap(HealingAffliction::new, HealingAffliction::health);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.HEALING;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        player.heal(health());
    }

    @Override
    public Text getName() {
        return Text.translatable(getType().getTranslationKey(), health());
    }
}
