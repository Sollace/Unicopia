package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemStack;

@Mixin(ItemModelManager.class)
abstract class MixinItemModels {
    @ModifyVariable(method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            index = 1)
    private ItemStack modifyStack(ItemStack stack) {
        Appearance appearance = stack.get(UDataComponentTypes.APPEARANCE);
        if (appearance != null && appearance.replaceFully()) {
            return Appearance.upwrapAppearance(stack);
        }
        return stack;
    }
}
