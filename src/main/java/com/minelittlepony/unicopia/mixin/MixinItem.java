package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;
import com.minelittlepony.unicopia.entity.effect.FoodPoisoningStatusEffect;
import com.minelittlepony.unicopia.item.toxin.*;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder, ItemImpl.TickableItem {

    private boolean foodLoaded;
    @Nullable
    private FoodComponent originalFoodComponent;

    @Shadow @Mutable
    private @Final FoodComponent foodComponent;

    private final List<ItemImpl.GroundTickCallback> tickCallbacks = new ArrayList<>();

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }

    @Override
    public void clearFoodOverride() {
        foodComponent = getOriginalFoodComponent();
    }

    @Override
    public void setFoodOverride(FoodComponent component) {
        if (getOriginalFoodComponent() == null) {
            foodComponent = component;
        }
    }

    @Override
    public FoodComponent getOriginalFoodComponent() {
        if (!foodLoaded) {
            foodLoaded = true;
            originalFoodComponent = asItem().getFoodComponent();
        }
        return originalFoodComponent;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> info) {
        getToxic(stack).finishUsing(stack, world, entity);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        TypedActionResult<ItemStack> result = FoodPoisoningStatusEffect.apply(this, user, hand);
        if (result.getResult() != ActionResult.PASS) {
            info.setReturnValue(result);
        }
    }
}
