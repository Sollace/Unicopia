package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.item.BaitedFishingRodItem;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(FishingBobberEntityRenderer.class)
abstract class MixinFishingBobberEntityRenderer {
    @ModifyExpressionValue(method = "getHandPos", at = {
            @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.getMainHandStack()Lnet/minecraft/item/ItemStack;")
    }, expect = 2)
    private ItemStack replaceFishingRodItem(ItemStack initialStack, PlayerEntity player, float f, float tickDelta) {
        if (player.fishHook instanceof BaitedFishingRodItem.BaitedFishingBobber bobber) {
            Item rodType = bobber.getRodType();
            if (rodType != null) {
                return initialStack.isOf(rodType) ? Items.FISHING_ROD.getDefaultStack() : initialStack;
            }
        }
        return initialStack;
    }
}
