package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.block.StrippingLootable;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MiningToolItem;
import net.minecraft.util.ActionResult;

@Mixin(AxeItem.class)
abstract class MixinAxeItem extends MiningToolItem {
    MixinAxeItem() {
        super(null, null, 0, 0, null);
    }

    @Inject(method = "useOnBlock", at = @At(
            value = "INVOKE",
            target = "net/minecraft/item/AxeItem.tryStrip(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/BlockState;)Ljava/util/Optional;",
            shift = Shift.AFTER),
            cancellable = true)
    private void onTryStrip(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        if (context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof StrippingLootable b && !b.onStripped(context)) {
            info.setReturnValue(ActionResult.FAIL);
        }
    }
}
