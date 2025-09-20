package com.minelittlepony.unicopia.item.component;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
/*
public class TransientComponentMapImpl implements TransientComponentMap {
    public static Supplier<TransientComponentMap> create(Supplier<ItemStack> stack) {
        return Suppliers.memoize(() -> new TransientComponentMapImpl(stack));
    }

    private Optional<Entity> carrier = Optional.empty();

    private final Supplier<ItemStack> stack;

    private TransientComponentMapImpl(Supplier<ItemStack> stack) {
        this.stack = stack;
    }

    @Override
    public Optional<Entity> getCarrier() {
        return carrier;
    }

    @Override
    public void setCarrier(@Nullable Entity carrier) {
        this.carrier = Optional.ofNullable(carrier);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type, TransientComponentMap map, @Nullable T upstreamValue) {
        return ((Entry<T>)TransientComponentTypes.ROOT.getOrDefault(type, Entry.DEFAULT)).getter().apply(stack.get(), map, upstreamValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean contains(ComponentType<? extends T> type, TransientComponentMap map, boolean parentContains) {
        return ((Entry<T>)TransientComponentTypes.ROOT.getOrDefault(type, Entry.DEFAULT)).checker().apply(stack.get(), map, parentContains);
    }
}
*/
