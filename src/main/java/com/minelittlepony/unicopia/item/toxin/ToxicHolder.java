package com.minelittlepony.unicopia.item.toxin;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.*;

public interface ToxicHolder {
    @Nullable
    default FoodComponent getOriginalFoodComponent() {
        return null;
    }

    default Toxic getDefaultToxic() {
        return getOriginalFoodComponent() == null ? Toxic.EMPTY : Toxics.EDIBLE;
    }

    default void clearFoodOverride() {}

    default void setFoodOverride(FoodComponent component) {}

    default Toxic getToxic(ItemStack stack) {
        clearFoodOverride();
        return Toxics.REGISTRY.stream()
                .filter(i -> i.matches((Item)this))
                .map(ToxicRegistryEntry::value)
                .map(t -> {
            t.component().ifPresent(this::setFoodOverride);
            return t;
        }).findFirst().orElseGet(this::getDefaultToxic);
    }

}
