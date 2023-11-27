package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;
import com.minelittlepony.unicopia.item.toxin.*;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck {
    private final List<ItemImpl.GroundTickCallback> tickCallbacks = new ArrayList<>();
    private final Supplier<FoodComponent> originalFoodComponent = Suppliers.memoize(((Item)(Object)this)::getFoodComponent);

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }

    @Override
    @Mutable
    @Accessor("foodComponent")
    public abstract void setFoodComponent(FoodComponent food);

    @Override
    public Toxic getToxic(ItemStack stack, @Nullable LivingEntity entity) {
        if (entity != null) {
            setFoodComponent(originalFoodComponent.get());
        }
        return Toxics.lookup(this, entity);
    }
}
