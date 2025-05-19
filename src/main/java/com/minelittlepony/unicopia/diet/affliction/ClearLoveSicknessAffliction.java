package com.minelittlepony.unicopia.diet.affliction;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;

public final class ClearLoveSicknessAffliction implements Affliction {
    public static final ClearLoveSicknessAffliction INSTANCE = new ClearLoveSicknessAffliction();
    public static final MapCodec<ClearLoveSicknessAffliction> CODEC = MapCodec.unit(INSTANCE);
    public static final PacketCodec<ByteBuf, ClearLoveSicknessAffliction> PACKET_CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.CURE_LOVE_SICKNESS;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        @Nullable
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        player.heal(food == null ? 1 : food.nutrition());
        if (player.getWorld().isClient) {
            return;
        }
        player.removeStatusEffect(StatusEffects.NAUSEA);
        player.removeStatusEffect(UEffects.FOOD_POISONING);
        player.removeStatusEffect(StatusEffects.WEAKNESS);
    }
}
