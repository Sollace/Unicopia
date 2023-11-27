package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import com.minelittlepony.unicopia.server.world.WaterLoggingManager;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;

@Mixin(BlockItem.class)
abstract class MixinBlockItem extends Item implements ToxicHolder {
    MixinBlockItem() {super(null); }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return getToxic(stack, null).useAction().orElseGet(() -> super.getUseAction(stack));
    }

    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> info) {
        WaterLoggingManager.getInstance().getPlacementState(context, info);
    }
}
