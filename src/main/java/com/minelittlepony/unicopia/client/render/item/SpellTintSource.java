package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;

public record SpellTintSource(int defaultColor) implements TintSource {
    public static final MapCodec<SpellTintSource> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(Codecs.RGB.fieldOf("default").forGetter(SpellTintSource::defaultColor)).apply(instance, SpellTintSource::new)
    );

    @Override
    public int getTint(ItemStack stack, ClientWorld world, LivingEntity user) {
        return !EnchantableItem.isEnchanted(stack) ? defaultColor : EnchantableItem.getSpellKey(stack).getColor() | 0xFF000000;
    }

    @Override
    public MapCodec<? extends TintSource> getCodec() {
        return CODEC;
    }
}
