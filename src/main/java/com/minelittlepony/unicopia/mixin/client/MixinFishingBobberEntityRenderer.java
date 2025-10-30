package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(FishingBobberEntityRenderer.class)
abstract class MixinFishingBobberEntityRenderer {
    @ModifyExpressionValue(method = "getHandPos", at = {
            @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.getMainHandStack()Lnet/minecraft/item/ItemStack;")
    }, expect = 2)
    private ItemStack replaceFishingRodItem(ItemStack initialStack, PlayerEntity player, float f, float tickDelta) {
        return initialStack.isOf(UItems.BAITED_FISHING_ROD) ? Items.FISHING_ROD.getDefaultStack() : initialStack;
    }
}
