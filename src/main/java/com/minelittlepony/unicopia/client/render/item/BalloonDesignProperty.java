package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record BalloonDesignProperty() implements SelectProperty<AirBalloonEntity.BalloonDesign> {
    public static final BalloonDesignProperty INSTANCE = new BalloonDesignProperty();
    public static final SelectProperty.Type<BalloonDesignProperty, AirBalloonEntity.BalloonDesign> TYPE = SelectProperty.Type.create(MapCodec.unit(INSTANCE), AirBalloonEntity.BalloonDesign.CODEC);

    @Override
    public AirBalloonEntity.BalloonDesign getValue(ItemStack stack, ClientWorld world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return BalloonDesignComponent.get(stack).design();
    }

    @Override
    public Codec<AirBalloonEntity.BalloonDesign> valueCodec() {
        return AirBalloonEntity.BalloonDesign.CODEC;
    }

    @Override
    public Type<? extends SelectProperty<AirBalloonEntity.BalloonDesign>, AirBalloonEntity.BalloonDesign> getType() {
        return TYPE;
    }
}
