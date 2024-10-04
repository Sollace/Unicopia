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
    public static final TransientComponentMap INITIAL = Util.make(new TransientComponentMap(null), map -> {
        map.set(UDataComponentTypes.DIET_PROFILE, (s, original) -> {
            return original != null ? original : ItemStackDuck.of(s).getTransientComponents().getCarrier()
                    .flatMap(Pony::of)
                    .map(pony -> PonyDiets.getInstance().getDiet(pony))
                    .orElse(DietProfile.EMPTY);
        });
        map.set(DataComponentTypes.FOOD, (s, originalFood) -> {
            DietProfile diet = s.get(UDataComponentTypes.DIET_PROFILE);

            if (diet == null || diet == DietProfile.EMPTY) {
                return originalFood;
            }

            if (originalFood != null) {
                return diet.getAdjustedFoodComponent(s, originalFood);
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

    @Nullable
    private final TransientComponentMap parent;
    private Map<ComponentType<?>, BiFunction<ItemStack, ?, ?>> components;

    private Optional<Entity> carrier = Optional.empty();

    private TransientComponentMap(TransientComponentMap parent) {
        this.parent = parent;
        if (parent == null) {
            components = new HashMap<>();
        }
    }

    public Optional<Entity> getCarrier() {
        return carrier;
    }

    public void setCarrier(@Nullable Entity carrier) {
        this.carrier = Optional.ofNullable(carrier);
    }

    public <T> void set(ComponentType<? extends T> type, BiFunction<ItemStack, T, T> getter) {
        if (components == null) {
            components = parent == null ? new HashMap<>() : new HashMap<>(parent.components);
        }
        components.put(type, getter);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ComponentType<? extends T> type, ItemStack stack, T upstreamValue) {
        if (components != null) {
            return ((BiFunction<ItemStack, T, T>)components.getOrDefault(type, DEFAULT)).apply(stack, upstreamValue);
        }
        if (parent != null) {
            return parent.get(type, stack, upstreamValue);
        }
        return upstreamValue;
    }

    public TransientComponentMap createCopy() {
        return new TransientComponentMap(this);
    }

    public interface Holder {
        TransientComponentMap getTransientComponents();
    }
}
