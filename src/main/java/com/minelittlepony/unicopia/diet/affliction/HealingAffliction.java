package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

record HealingAffliction(float health) implements Affliction {
    public static final Codec<HealingAffliction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("health").forGetter(HealingAffliction::health)
    ).apply(instance, HealingAffliction::new));

    public HealingAffliction(PacketByteBuf buffer) {
        this(buffer.readFloat());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(health);
    }

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
