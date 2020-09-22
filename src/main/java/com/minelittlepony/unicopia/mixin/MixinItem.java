package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder {
    @Override
    @Accessor("foodComponent")
    public abstract void setFood(FoodComponent food);
}
