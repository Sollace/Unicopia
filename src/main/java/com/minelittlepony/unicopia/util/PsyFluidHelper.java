package com.minelittlepony.unicopia.util;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

final class PsyFluidHelper {
    private static final Supplier<Optional<Class<?>>> SIMPLE_FLUID_CLASS = Suppliers.memoize(() -> {
        try {
            return Optional.ofNullable(Class.forName("ivorius.psychedelicraft.fluid.SimpleFluid"));
        } catch (Throwable t) {
            return Optional.empty();
        }
    });
    private static final Function<FluidVariant, FluidState> FALLBACK_METHOD = fluid -> fluid.getFluid().getDefaultState();
    private static final Supplier<Function<FluidVariant, FluidState>> GET_FULL_FLUID_STATE = Suppliers.memoize(() -> SIMPLE_FLUID_CLASS.get().<Function<FluidVariant, FluidState>>map(type -> {
            try {
                final Method method = type.getDeclaredMethod("getFluidState", ItemStack.class);
                if (method != null) {
                    return fluid -> {
                        try {
                            ItemStack stack = Items.STONE.getDefaultStack();
                            NbtCompound fluidTag = stack.getOrCreateSubNbt("fluid");
                            fluidTag.putString("id", Registries.FLUID.getId(fluid.getFluid()).toString());
                            fluidTag.put("attributes", fluid.getNbt());
                            return FluidState.class.cast(method.invoke(type.cast(fluid), stack));
                        } catch (Throwable tt) {}
                        return FALLBACK_METHOD.apply(fluid);
                    };
                }
            } catch (Throwable t) {}
            return FALLBACK_METHOD;
        }).orElse(FALLBACK_METHOD));

    static FluidState getFullFluidState(FluidVariant variant) {
        return SIMPLE_FLUID_CLASS.get()
                .filter(type -> type.isAssignableFrom(variant.getFluid().getClass()))
                .map(type -> GET_FULL_FLUID_STATE.get().apply(variant))
                .orElseGet(() -> variant.getFluid().getDefaultState());
    }
}
