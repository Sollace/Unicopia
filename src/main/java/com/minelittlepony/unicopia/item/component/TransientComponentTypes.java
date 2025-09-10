package com.minelittlepony.unicopia.item.component;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.TransientComponentMap.Entry;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;

public interface TransientComponentTypes {
    Map<ComponentType<?>, Entry<?>> ROOT = new HashMap<>();

    Entry<DietProfile> PROFILE = register(UDataComponentTypes.DIET_PROFILE, (s, comps, original) -> {
        return original != null ? original : comps.getCarrier()
                .flatMap(Pony::of)
                .map(pony -> PonyDiets.getInstance().getDiet(pony))
                .orElse(DietProfile.EMPTY);
    }, (s, comps, original) -> original || comps.getCarrier()
            .flatMap(Pony::of)
            .map(pony -> PonyDiets.getInstance().getDiet(pony))
            .orElse(DietProfile.EMPTY) != DietProfile.EMPTY);

    Entry<FoodComponent> FOOD_COMPONENT = register(DataComponentTypes.FOOD,
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).getAdjustedFoodComponent(s, original),
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).hasFoodAttributes(s, original)
    );

    Entry<ConsumableComponent> CONSUMABLE_COMPONENT = register(DataComponentTypes.CONSUMABLE,
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).getAdjustedConsumableComponent(s, original),
            (s, comps, original) -> s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY).hasFoodAttributes(s, original)
    );

    static <T> TransientComponentMap.Entry<T> register(ComponentType<T> type,
            TransientComponentMap.Entry.Func<T> getter,
            TransientComponentMap.Entry.Func<Boolean> checker) {
        var entry = new Entry<>(getter, checker);
        ROOT.put(type, entry);
        return entry;
    }
}
