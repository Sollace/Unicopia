package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.minelittlepony.unicopia.util.HoeUtil;

import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ActionResult;

@Mixin(HoeItem.class)
abstract class MixinHoeItem extends ToolItem {
    MixinHoeItem() {super(null, null);}

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        if (state.getBlock() instanceof HoeUtil.Tillable) {
            if (!((HoeUtil.Tillable)state.getBlock()).canTill(context)) {
                return ActionResult.PASS;
            }
        }

        return null;

    }

}
