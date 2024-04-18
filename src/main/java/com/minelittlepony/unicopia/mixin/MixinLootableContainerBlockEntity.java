package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;

@Mixin(LootableContainerBlockEntity.class)
abstract class MixinLootableContainerBlockEntity extends LockableContainerBlockEntity {
    private boolean generateMimic;

    MixinLootableContainerBlockEntity() { super(null, null, null); }

    @Inject(
            method = "checkLootInteraction",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/loot/LootTable.supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContextParameterSet;J)V",
                shift = Shift.AFTER
    ))
    private void onCheckLootInteraction(@Nullable PlayerEntity player, CallbackInfo info) {
        if (player != null) {
            generateMimic = true;
        }
    }

    @Inject(
            method = "createMenu",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/block/entity/LootableContainerBlockEntity.checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
                shift = Shift.AFTER
    ), cancellable = true)
    private void onCreateMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<ScreenHandler> info) {
        if (generateMimic) {
            generateMimic = false;
            var mimic = MimicEntity.spawnFromChest(player.getWorld(), getPos(), player);
            if (mimic.getResult() == ActionResult.SUCCESS) {
                info.setReturnValue(mimic.getValue().createScreenHandler(syncId, playerInventory, player));
            }
        }
    }
}
