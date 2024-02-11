package com.minelittlepony.unicopia.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.item.enchantment.HeartboundEnchantmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
abstract class MixinPlayerInventory implements Inventory, Nameable {
    @Shadow
    public @Final PlayerEntity player;
    @Shadow
    private @Final List<DefaultedList<ItemStack>> combinedInventory;

    @Nullable
    private HeartboundEnchantmentUtil.InventorySnapshot inventorySnapshot;

    @Inject(method = "dropAll()V", at = @At("HEAD"))
    public void beforeDropAll(CallbackInfo info) {
        inventorySnapshot = HeartboundEnchantmentUtil.createSnapshot(combinedInventory);
        if (!inventorySnapshot.empty()) {
            UCriteria.USE_SOULMATE.trigger(player);
        }
    }

    @Inject(method = "dropAll()V", at = @At("RETURN"))
    public void afterDropAll(CallbackInfo info) {
        if (inventorySnapshot != null) {
            inventorySnapshot.restoreInto(combinedInventory);
            inventorySnapshot = null;
        }
    }
}
