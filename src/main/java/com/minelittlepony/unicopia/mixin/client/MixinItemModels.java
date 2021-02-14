package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.item.ChameleonItem;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.ItemStack;

@Mixin(ItemModels.class)
abstract class MixinItemModels {
    @ModifyVariable(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"),
            index = 1)
    private ItemStack modifyStack(ItemStack stack) {
        if (stack.getItem() instanceof ChameleonItem && ((ChameleonItem)stack.getItem()).isFullyDisguised()) {
            return ((ChameleonItem)stack.getItem()).getAppearanceStack(stack);
        }
        return stack;
    }
}
