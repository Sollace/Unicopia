package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

public record CompoundAffliction (List<Affliction> afflictions) implements Affliction {
    public static final MapCodec<CompoundAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Affliction.CODEC.listOf().fieldOf("afflictions").forGetter(CompoundAffliction::afflictions)
    ).apply(instance, CompoundAffliction::new));
    public static final PacketCodec<RegistryByteBuf, CompoundAffliction> PACKET_CODEC = null;

    public static CompoundAffliction of(Affliction...afflictions) {
        return new CompoundAffliction(List.of(afflictions));
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.MANY;
    }

    @Override
    public boolean isEmpty() {
        return afflictions.isEmpty();
    }

    @Override
    public void appendTooltip(List<Text> tooltip) {
        afflictions.forEach(i -> i.appendTooltip(tooltip));
    }

    @Override
    public Text getName() {
        return afflictions.stream().map(Affliction::getName).reduce(null, (a, b) -> {
            return a == null ? b : a.copy().append(" + ").append(b);
        });
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        afflictions.forEach(i -> i.afflict(player, stack));
    }
}
