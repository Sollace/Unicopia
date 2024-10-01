package com.minelittlepony.unicopia.item.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.Effect;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.ItemStackDuck;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

public class TransientComponentMap {
    private static final BiFunction<ItemStack, ?, ?> DEFAULT = (stack, t) -> t;
    public static final TransientComponentMap INITIAL = Util.make(new TransientComponentMap(), map -> {
        map.set(UDataComponentTypes.DIET_PROFILE, (s, original) -> {
            if (original != null) {
                return original;
            }
            return ItemStackDuck.of(s).getTransientComponents().getCarrier()
                    .flatMap(Pony::of)
                    .map(pony -> PonyDiets.getInstance().getDiet(pony))
                    .orElse(DietProfile.EMPTY);
        });
        map.set(DataComponentTypes.FOOD, (s, originalFood) -> {
            DietProfile diet = s.get(UDataComponentTypes.DIET_PROFILE);

            if (diet == null) {
                return originalFood;
            }

            if (originalFood != null) {
                return diet.getAdjustedFoodComponent(s);
            }

            if (ItemStackDuck.of(s).getTransientComponents().getCarrier()
                    .flatMap(Pony::of)
                    .filter(pony -> pony.getObservedSpecies().hasIronGut())
                    .isPresent()) {
                return diet.findEffect(s)
                    .flatMap(Effect::foodComponent)
                    .or(() -> PonyDiets.getInstance().getEffects(s).foodComponent())
                    .orElse(originalFood);
            }

            return originalFood;
        });
    });

    private final Map<ComponentType<?>, BiFunction<ItemStack, ?, ?>> components = new HashMap<>();

    private Optional<Entity> carrier = Optional.empty();

    private TransientComponentMap() {}

    public Optional<Entity> getCarrier() {
        return carrier;
    }

    public void setCarrier(@Nullable Entity carrier) {
        this.carrier = Optional.ofNullable(carrier);
    }

    public <T> void set(ComponentType<? extends T> type, BiFunction<ItemStack, T, T> getter) {
        components.put(type, getter);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ComponentType<? extends T> type, ItemStack stack, T upstreamValue) {
        return ((BiFunction<ItemStack, T, T>)components.getOrDefault(type, DEFAULT)).apply(stack, upstreamValue);
    }

    public TransientComponentMap createCopy() {
        TransientComponentMap copy = new TransientComponentMap();
        copy.components.putAll(components);
        return copy;
    }

    public interface Holder {
        TransientComponentMap getTransientComponents();
    }
}
