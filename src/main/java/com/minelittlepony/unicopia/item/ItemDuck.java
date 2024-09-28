package com.minelittlepony.unicopia.item;

import java.util.Optional;

import com.minelittlepony.unicopia.diet.DietView;
import com.minelittlepony.unicopia.entity.ItemImpl;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.*;

public interface ItemDuck extends ItemConvertible, ItemImpl.TickableItem, DietView.Holder {
    @Deprecated
    void setFoodComponent(FoodComponent food);

    // TODO: Inject into Item.Settings::getComponents and return our own implementation of ComponentMap to handle food component overrides
    // ItemStack(ComponentMapImpl(UnicopiaComponentMap(ItemComponentMap()), Changes))
    @Deprecated
    Optional<FoodComponent> getOriginalFoodComponent();

    @Deprecated
    default void resetFoodComponent() {
        setFoodComponent(getOriginalFoodComponent().orElse(null));
    }
}
