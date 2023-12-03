package com.minelittlepony.unicopia.item;

import java.util.Optional;

import com.minelittlepony.unicopia.entity.ItemImpl;
import net.minecraft.item.*;

public interface ItemDuck extends ItemConvertible, ItemImpl.TickableItem {
    void setFoodComponent(FoodComponent food);

    Optional<FoodComponent> getOriginalFoodComponent();

    default void resetFoodComponent() {
        setFoodComponent(getOriginalFoodComponent().orElse(null));
    }
}
