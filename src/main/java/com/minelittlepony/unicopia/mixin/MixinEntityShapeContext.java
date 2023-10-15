package com.minelittlepony.unicopia.mixin;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;

@Mixin(EntityShapeContext.class)
abstract class MixinEntityShapeContext implements EquineContext {
    private EquineContext equineContext;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(boolean descending, double minY, ItemStack heldItem, Predicate<FluidState> walkOnFluidPredicate, @Nullable Entity entity, CallbackInfo into) {
        equineContext = EquineContext.of(entity);
    }

    @Override
    public Race getSpecies() {
        return equineContext.getSpecies();
    }

    @Override
    public Race.Composite getCompositeRace() {
        return equineContext.getCompositeRace();
    }
}
