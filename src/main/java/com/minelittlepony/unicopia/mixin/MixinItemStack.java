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
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.client.ModifierTooltipRenderer;
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
            info.setReturnValue(result) ;
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

    @Inject(method = "getTooltip",
            at = @At(value = "INVOKE",
            target = "net/minecraft/item/ItemStack.appendAttributeModifiersTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;)V"
    ))
    public void onGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info, @Local List<Text> lines) {
        ItemStack self = (ItemStack)(Object)this;
        ModifierTooltipRenderer.INSTANCE.getTooltip(self, context, player, type, lines);
    }

    @ModifyReturnValue(method = "takesDamageFrom", at = @At("RETURN"))
    private boolean onTakesDamageFrom(boolean takesDamage, DamageSource source) {
        ItemStack self = (ItemStack)(Object)this;
        return self.getItem() instanceof DamageChecker checker ? checker.takesDamageFrom(source) : takesDamage;
    }
}

@Mixin(ComponentHolder.class)
interface MixinComponentHolder {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    default <T> T unicopia_onGet(T value, ComponentType<? extends T> type) {
        Object o = this;
        return o instanceof ItemStack stack ? ItemStackDuck.of(stack).getTransientComponents().get(type, stack, value) : value;
    }

    @ModifyReturnValue(method = "getOrDefault", at = @At("RETURN"))
    default <T> T unicopia_onGetOrDefault(T value, ComponentType<? extends T> type, T fallback) {
        Object o = this;
        return o instanceof ItemStack stack ? ItemStackDuck.of(stack).getTransientComponents().get(type, stack, value) : value;
    }

    @ModifyReturnValue(method = "contains", at = @At("RETURN"))
    default boolean unicopia_onContains(boolean z, ComponentType<?> type) {
        Object o = this;
        return z || (o instanceof ItemStack stack && ItemStackDuck.of(stack).getTransientComponents().get(type, stack, null) != null);
    }
}
