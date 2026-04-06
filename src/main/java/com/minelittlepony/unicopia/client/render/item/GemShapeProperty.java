package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GemstoneItem.Shape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record GemShapeProperty() implements SelectProperty<Shape> {
    public static final SelectProperty.Type<GemShapeProperty, Shape> TYPE = SelectProperty.Type.create(MapCodec.unit(new GemShapeProperty()), Shape.CODEC);

    @Override
    public Shape getValue(ItemStack stack, ClientWorld world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return EnchantableItem.getSpellKey(stack).getGemShape();
    }

    @Override
    public Codec<Shape> valueCodec() {
        return Shape.CODEC;
    }

    @Override
    public Type<? extends SelectProperty<Shape>, Shape> getType() {
        return TYPE;
    }
}
