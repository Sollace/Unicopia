package com.minelittlepony.unicopia.item.component;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.Effect;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.ItemStackDuck;
import com.minelittlepony.unicopia.item.component.TransientComponentMap.Entry;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

public interface TransientComponentTypes {
    Map<ComponentType<?>, Entry<?>> ROOT = new HashMap<>();

    Entry<DietProfile> PROFILE = register(UDataComponentTypes.DIET_PROFILE, (s, comps, original) -> {
        return original != null && original != DietProfile.EMPTY ? original : comps.getCarrier()
                .flatMap(Pony::of)
                .map(pony -> PonyDiets.getInstance().getDiet(pony))
                .orElse(DietProfile.EMPTY);
    }, (s, comps, original) -> original || comps.getCarrier()
            .flatMap(Pony::of)
            .map(pony -> PonyDiets.getInstance().getDiet(pony))
            .orElse(DietProfile.EMPTY) != DietProfile.EMPTY);

    Entry<FoodComponent> FOOD_COMPONENT = register(DataComponentTypes.FOOD, (s, comps, original) -> {
        DietProfile diet = s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY);

        if (diet == DietProfile.EMPTY) {
            return original;
        }

        if (original != null && (original.nutrition() > 0 || original.saturation() > 0)) {
            return diet.getAdjustedFoodComponent(s, original);
        }

        if (ItemStackDuck.of(s).getTransientComponents().getCarrier()
                .flatMap(Pony::of)
                .filter(pony -> pony.getObservedSpecies().hasIronGut())
                .isPresent()) {
            return diet.findEffect(s)
                .flatMap(Effect::foodComponent)
                .or(() -> PonyDiets.getInstance().getEffects(s).foodComponent())
                .orElse(original);
        }

        return original;
    }, (s, comps, original) -> {
        DietProfile diet = s.getOrDefault(UDataComponentTypes.DIET_PROFILE, DietProfile.EMPTY);

        if (diet == DietProfile.EMPTY) {
            return original;
        }

        if (diet.isInedible(s)) {
            return false;
        }

        return original || (comps.getCarrier()
                .flatMap(Pony::of)
                .filter(pony -> pony.getObservedSpecies().hasIronGut())
                .isPresent() && diet.findEffect(s)
                .flatMap(Effect::foodComponent)
                .or(() -> PonyDiets.getInstance().getEffects(s).foodComponent())
                .isPresent());
    });

    static <T> TransientComponentMap.Entry<T> register(ComponentType<T> type,
            TransientComponentMap.Entry.Func<T> getter,
            TransientComponentMap.Entry.Func<Boolean> checker) {
        var entry = new Entry<>(getter, checker);
        ROOT.put(type, entry);
        return entry;
    }
}
