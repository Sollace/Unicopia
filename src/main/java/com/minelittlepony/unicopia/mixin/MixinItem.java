package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import com.minelittlepony.unicopia.item.toxin.Toxics;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder, ItemImpl.TickableItem {

    private boolean foodLoaded;
    @Nullable
    private FoodComponent originalFoodComponent;

    @Shadow @Mutable
    private @Final FoodComponent foodComponent;

    private List<ItemImpl.GroundTickCallback> tickCallbacks;

    @Override
    public List<GroundTickCallback> getCallbacks() {
        if (tickCallbacks == null) {
            tickCallbacks = new ArrayList<>();
        }
        return tickCallbacks;
    }

    @Override
    public Optional<Toxic> getToxic(ItemStack stack) {
        if (!foodLoaded) {
            foodLoaded = true;
            originalFoodComponent = ((Item)(Object)this).getFoodComponent();
        }

        foodComponent = originalFoodComponent;
        Optional<Toxic> toxic = Toxics.REGISTRY.stream()
                .filter(i -> i.matches((Item)(Object)this))
                .map(t -> {
            if (originalFoodComponent == null) {
                t.getFoodComponent().ifPresent(s -> foodComponent = s);
            }
            return t;
        }).findFirst();

        if (!toxic.isPresent() && ((Item)(Object)this).getFoodComponent() != null) {
            return Optional.of(Toxics.EDIBLE);
        }
        return toxic;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> info) {
        getToxic(stack).ifPresent(t -> t.finishUsing(stack, world, entity));
    }
}
