package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.item.enchantment.HeartboundEnchantmentUtil;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;

@Mixin(PlayerInventory.class)
abstract class MixinPlayerInventory implements Inventory {
    @Nullable
    private HeartboundEnchantmentUtil.InventorySnapshot inventorySnapshot;

    @Inject(method = "dropAll()V", at = @At("HEAD"))
    public void beforeDropAll(CallbackInfo info) {
        inventorySnapshot = HeartboundEnchantmentUtil.createSnapshot(this);
        if (!inventorySnapshot.empty()) {
            UCriteria.USE_SOULMATE.trigger(((PlayerInventory)(Object)this).player);
        }
    }

    @Inject(method = "dropAll()V", at = @At("RETURN"))
    public void afterDropAll(CallbackInfo info) {
        if (inventorySnapshot != null) {
            inventorySnapshot.restoreInto(this);
            inventorySnapshot = null;
        }
    }
}
