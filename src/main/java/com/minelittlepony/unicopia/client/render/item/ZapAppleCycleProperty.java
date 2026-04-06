package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.item.property.numeric.NeedleAngleState;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ZapAppleCycleProperty extends NeedleAngleState implements NumericProperty {
    public static final MapCodec<ZapAppleCycleProperty> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.optionalFieldOf("wobble", true).forGetter(ZapAppleCycleProperty::hasWobble)
    ).apply(i, ZapAppleCycleProperty::new));
    private final NeedleAngleState.Angler angler;

    public ZapAppleCycleProperty(boolean wobble) {
        super(wobble);
        angler = createAngler(0.9F);
    }

    @Override
    protected float getAngle(ItemStack stack, ClientWorld world, int seed, Entity user) {
        float angle = UnicopiaClient.getInstance().getZapAppleStage().getCycleProgress(world);
        long time = world.getTime();
        if (angler.shouldUpdate(time)) {
            angler.update(time, angle);
        }

        return angler.getAngle();
    }

    @Override
    public MapCodec<? extends NumericProperty> getCodec() {
        return CODEC;
    }

}
