package com.minelittlepony.unicopia.diet.affliction;

import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.mojang.serialization.Codec;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

final class ClearLoveSicknessAffliction implements Affliction {
    public static final ClearLoveSicknessAffliction INSTANCE = new ClearLoveSicknessAffliction();
    public static final Codec<ClearLoveSicknessAffliction> CODEC = Codec.unit(INSTANCE);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.CURE_LOVE_SICKNESS;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        player.heal(stack.isFood() ? stack.getItem().getFoodComponent().getHunger() : 1);
        player.removeStatusEffect(StatusEffects.NAUSEA);
        player.removeStatusEffect(UEffects.FOOD_POISONING);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
    }
}
