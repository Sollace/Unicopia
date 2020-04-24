package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.util.HoeUtil;

import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ActionResult;

@Mixin(HoeItem.class)
abstract class MixinHoeItem extends ToolItem {
    MixinHoeItem() {super(null, null);}
    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        if (state.getBlock() instanceof HoeUtil.Tillable) {
            if (!((HoeUtil.Tillable)state.getBlock()).canTill(context)) {
                info.setReturnValue(ActionResult.PASS);
            }
        }
    }
}
