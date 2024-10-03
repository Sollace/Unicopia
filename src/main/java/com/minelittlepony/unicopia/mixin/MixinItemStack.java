package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.entity.effect.FoodPoisoningStatusEffect;
import com.minelittlepony.unicopia.item.DamageChecker;
import com.minelittlepony.unicopia.item.ItemStackDuck;
import com.minelittlepony.unicopia.item.component.TransientComponentMap;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
abstract class MixinItemStack implements ItemStackDuck {
    private final Supplier<TransientComponentMap> transientComponents = Suppliers.memoize(() -> TransientComponentMap.INITIAL.createCopy());

    @Override
    public TransientComponentMap getTransientComponents() {
        return transientComponents.get();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        getTransientComponents().setCarrier(user);
        TypedActionResult<ItemStack> result = FoodPoisoningStatusEffect.apply((ItemStack)(Object)this, user);
        if (result.getResult() != ActionResult.PASS) {
            info.setReturnValue(result);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("RETURN"))
    public void onOnStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    private void afterFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "getTooltip", at = @At("HEAD"))
    public void beforeGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
        getTransientComponents().setCarrier(player);
    }

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void afterGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "takesDamageFrom", at = @At("HEAD"))
    private void onTakesDamageFrom(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() instanceof DamageChecker checker) {
            info.setReturnValue(checker.takesDamageFrom(source));
        }
    }
}

@Mixin(ComponentHolder.class)
interface MixinComponentHolder {
    @Inject(method = "get", at = @At("RETURN"))
    default <T> void unicopia_onGet(ComponentType<? extends T> type, CallbackInfoReturnable<T> info) {
        Object o = this;
        if (o instanceof ItemStack stack) {
            info.setReturnValue(ItemStackDuck.of(stack).getTransientComponents().get(type, stack, info.getReturnValue()));
        }
    }

    @Inject(method = "getOrDefault", at = @At("RETURN"))
    default <T> void unicopia_onGetOrDefault(ComponentType<? extends T> type, T fallback, CallbackInfoReturnable<T> info) {
        Object o = this;
        if (o instanceof ItemStack stack) {
            info.setReturnValue(ItemStackDuck.of(stack).getTransientComponents().get(type, stack, info.getReturnValue()));
        }
    }

    @Inject(method = "contains", at = @At("RETURN"))
    default void unicopia_onContains(ComponentType<?> type, CallbackInfoReturnable<Boolean> info) {
        Object o = this;
        if (o instanceof ItemStack stack && ItemStackDuck.of(stack).getTransientComponents().get(type, stack, null) != null) {
            info.setReturnValue(true);
        }
    }
}
