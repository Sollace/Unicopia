package com.minelittlepony.unicopia.util;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.component.ComponentType;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

final class PsyFluidHelper {
    private static final Supplier<Optional<Class<?>>> SIMPLE_FLUID_CLASS = Suppliers.memoize(() -> {
        try {
            return Optional.ofNullable(Class.forName("ivorius.psychedelicraft.fluid.SimpleFluid"));
        } catch (Throwable t) {
            return Optional.empty();
        }
    });
    private static final Supplier<Optional<Class<?>>> ITEM_FLUIDS_CLASS = Suppliers.memoize(() -> {
        try {
            return Optional.ofNullable(Class.forName("ivorius.psychedelicraft.item.component.ItemFluids"));
        } catch (Throwable t) {
            return Optional.empty();
        }
    });
    private static final Function<FluidVariant, FluidState> FALLBACK_METHOD = fluid -> fluid.getFluid().getDefaultState();
    private static final Supplier<Function<FluidVariant, FluidState>> GET_FULL_FLUID_STATE = Suppliers.memoize(
            () -> SIMPLE_FLUID_CLASS.get().<Function<FluidVariant, FluidState>>map(type -> {
            return ITEM_FLUIDS_CLASS.get().<Function<FluidVariant, FluidState>>map(fluidsType -> {
                try {
                    ComponentType<?> fluidsComponentType = Registries.DATA_COMPONENT_TYPE.get(Identifier.of("psychedelicraft", "fluids"));
                    if (fluidsComponentType != null) {
                        final Method method = type.getDeclaredMethod("getFluidState", fluidsType);
                        if (method != null) {
                            return fluidVariant -> {
                                try {
                                    Object fluids = fluidVariant.getComponents().get(fluidsComponentType).orElse(null);
                                    if (fluids == null) {
                                        return FALLBACK_METHOD.apply(fluidVariant);
                                    }
                                    return (FluidState)method.invoke(type.cast(fluidVariant), fluids);
                                } catch (Throwable tt) {}
                                return FALLBACK_METHOD.apply(fluidVariant);
                            };
                        }
                    }
                } catch (Throwable t) {}
                return FALLBACK_METHOD;
            }).orElse(FALLBACK_METHOD);
        }).orElse(FALLBACK_METHOD));

    static FluidState getFullFluidState(FluidVariant variant) {
        return SIMPLE_FLUID_CLASS.get()
                .filter(type -> type.isAssignableFrom(variant.getFluid().getClass()))
                .map(type -> GET_FULL_FLUID_STATE.get().apply(variant))
                .orElseGet(() -> variant.getFluid().getDefaultState());
    }
}
