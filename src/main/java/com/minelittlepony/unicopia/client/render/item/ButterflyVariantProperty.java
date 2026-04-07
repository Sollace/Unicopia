package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record ButterflyVariantProperty() implements SelectProperty<ButterflyEntity.Variant> {
    public static final ButterflyVariantProperty INSTANCE = new ButterflyVariantProperty();
    public static final SelectProperty.Type<ButterflyVariantProperty, ButterflyEntity.Variant> TYPE = SelectProperty.Type.create(MapCodec.unit(INSTANCE), ButterflyEntity.Variant.CODEC);

    @Override
    public ButterflyEntity.Variant getValue(ItemStack stack, ClientWorld world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return BufferflyVariantComponent.get(stack).variant();
    }

    @Override
    public Codec<ButterflyEntity.Variant> valueCodec() {
        return ButterflyEntity.Variant.CODEC;
    }

    @Override
    public Type<? extends SelectProperty<ButterflyEntity.Variant>, ButterflyEntity.Variant> getType() {
        return TYPE;
    }
}
