package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record AffinityProperty() implements SelectProperty<Affinity> {
    public static final SelectProperty.Type<AffinityProperty, Affinity> TYPE = SelectProperty.Type.create(MapCodec.unit(new AffinityProperty()), Affinity.CODEC);

    @Override
    public Affinity getValue(ItemStack stack, ClientWorld world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return EnchantableItem.getSpellKey(stack).getAffinity();
    }

    @Override
    public Codec<Affinity> valueCodec() {
        return Affinity.CODEC;
    }

    @Override
    public Type<? extends SelectProperty<Affinity>, Affinity> getType() {
        return TYPE;
    }
}
