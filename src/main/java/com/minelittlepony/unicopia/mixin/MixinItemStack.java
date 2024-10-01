package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.item.DamageChecker;
import com.minelittlepony.unicopia.item.ItemStackDuck;
import com.minelittlepony.unicopia.item.component.TransientComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
abstract class MixinItemStack implements ItemStackDuck {
    private final TransientComponentMap transientComponents = TransientComponentMap.INITIAL.createCopy();

    @Shadow
    abstract <T extends TooltipAppender> void appendTooltip(
        ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type
    );

    @Override
    public TransientComponentMap getTransientComponents() {
        return transientComponents;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        transientComponents.setCarrier(user);
        TypedActionResult<ItemStack> result = PonyDiets.getInstance().startUsing((ItemStack)(Object)this, world, user, hand);
        if (result.getResult() != ActionResult.PASS) {
            info.setReturnValue(result);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("RETURN"))
    public void onOnStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        transientComponents.setCarrier(null);
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void beforeFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        transientComponents.setCarrier(user);
        PonyDiets.getInstance().finishUsing((ItemStack)(Object)this, world, user);
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    private void afterFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        transientComponents.setCarrier(null);
    }

    @Inject(method = "getTooltip", at = @At("HEAD"))
    public void beforeGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
        transientComponents.setCarrier(player);
    }

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void afterGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
        transientComponents.setCarrier(null);
    }

    @Inject(method = "getTooltip",
            at = @At(value = "INVOKE",
            target = "net/minecraft/item/ItemStack.appendAttributeModifiersTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;)V"
    ))
    public void onGetTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {

    }

    @Inject(method = "takesDamageFrom", at = @At("HEAD"))
    private void onTakesDamageFrom(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() instanceof DamageChecker checker) {
            info.setReturnValue(checker.takesDamageFrom(source));
        }
    }

    @Inject(method = "get", at = @At("RETURN"))
    private <T> void unicopia_onGet(ComponentType<? extends T> type, CallbackInfoReturnable<T> info) {
        Object o = this;
        if (o instanceof ItemStack stack) {
            info.setReturnValue(ItemStackDuck.of(stack).getTransientComponents().get(type, stack, info.getReturnValue()));
        }
    }

    @Inject(method = "getOrDefault", at = @At("RETURN"))
    private <T> void unicopia_onGetOrDefault(ComponentType<? extends T> type, T fallback, CallbackInfoReturnable<T> info) {
        Object o = this;
        if (o instanceof ItemStack stack) {
            info.setReturnValue(ItemStackDuck.of(stack).getTransientComponents().get(type, stack, info.getReturnValue()));
        }
    }

    @Inject(method = "contains", at = @At("RETURN"))
    private void unicopia_onContains(ComponentType<?> type, CallbackInfoReturnable<Boolean> info) {
        Object o = this;
        if (o instanceof ItemStack stack && ItemStackDuck.of(stack).getTransientComponents().get(type, stack, null) != null) {
            info.setReturnValue(true);
        }
    }
}
