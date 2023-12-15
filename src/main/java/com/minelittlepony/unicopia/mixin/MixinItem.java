package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.diet.DietView;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;
import com.minelittlepony.unicopia.item.ItemDuck;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck, DietView.Holder {
    private final List<ItemImpl.GroundTickCallback> tickCallbacks = new ArrayList<>();
    private final Supplier<Optional<FoodComponent>> originalFoodComponent = Suppliers.memoize(() -> {
        return Optional.ofNullable(((Item)(Object)this).getFoodComponent());
    });

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }

    @Override
    @Mutable
    @Accessor("foodComponent")
    public abstract void setFoodComponent(FoodComponent food);

    @Override
    public Optional<FoodComponent> getOriginalFoodComponent() {
        return originalFoodComponent.get();
    }
}
