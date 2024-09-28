package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;
import com.minelittlepony.unicopia.item.ItemDuck;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck {
    private final List<ItemImpl.GroundTickCallback> tickCallbacks = new ArrayList<>();

    @Deprecated
    private final Supplier<Optional<FoodComponent>> originalFoodComponent = Suppliers.memoize(() -> {
        return Optional.ofNullable(((Item)(Object)this).getComponents().get(DataComponentTypes.FOOD));
    });

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }

    @Deprecated
    @Override
    public Optional<FoodComponent> getOriginalFoodComponent() {
        return originalFoodComponent.get();
    }
}
