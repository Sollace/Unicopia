package com.minelittlepony.unicopia.item.component;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.ItemStackDuck;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface TransientComponentMap {
    TransientComponentMap EMPTY = new TransientComponentMap() {
        @Override
        public <T> @Nullable T get(ComponentType<? extends T> type, TransientComponentMap map, @Nullable T upstreamValue) {
            return upstreamValue;
        }

        @Override
        public <T> boolean contains(ComponentType<? extends T> type, TransientComponentMap map, boolean parentContains) {
            return parentContains;
        }
    };

    static TransientComponentMap of(Object o) {
        return o instanceof ItemStack stack ? ItemStackDuck.of(stack).getTransientComponents() : EMPTY;
    }

    default Optional<Entity> getCarrier() {
        return Optional.empty();
    }

    default void setCarrier(@Nullable Entity carrier) {
    }

    default <T> T get(ComponentType<? extends T> type, T upstreamValue) {
        return get(type, this, upstreamValue);
    }

    default <T> T getOrDefault(ComponentType<? extends T> type, T upstreamValue, T fallback) {
        upstreamValue = get(type, upstreamValue);
        return upstreamValue == null ? fallback : upstreamValue;
    }

    default <T> boolean contains(ComponentType<? extends T> type, boolean parentContains) {
        return contains(type, this, parentContains);
    }

    <T> @Nullable T get(ComponentType<? extends T> type, TransientComponentMap map, @Nullable T upstreamValue);

    <T> boolean contains(ComponentType<? extends T> type, TransientComponentMap map, boolean parentContains);

    public interface Holder {
        TransientComponentMap getTransientComponents();
    }

    public record Entry<T>(Func<T> getter, Func<Boolean> checker) {
        public static final Entry<?> DEFAULT = new Entry<>((stack, comps, t) -> t, (stack, comps, t) -> t);

        public interface Func<T> {
            @Nullable T apply(ItemStack stack, TransientComponentMap components, T initial);
        }
    }
}
