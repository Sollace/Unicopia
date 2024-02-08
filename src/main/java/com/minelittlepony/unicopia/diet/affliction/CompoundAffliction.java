package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

class CompoundAffliction implements Affliction {
    public final List<Affliction> afflictions;

    public CompoundAffliction(List<Affliction> afflictions) {
        this.afflictions = afflictions;
    }

    public CompoundAffliction(PacketByteBuf buffer) {
        this(buffer.readList(Affliction::read));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeCollection(afflictions, Affliction::write);
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
