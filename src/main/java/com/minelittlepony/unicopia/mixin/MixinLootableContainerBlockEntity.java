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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

@Mixin(LootableContainerBlockEntity.class)
abstract class MixinLootableContainerBlockEntity extends LockableContainerBlockEntity implements MimicEntity.MimicGeneratable {
    private Identifier mimicLootTable;
    private boolean allowMimics = true;
    private boolean isMimic;

    @Shadow
    @Nullable
    private Identifier lootTableId;

    MixinLootableContainerBlockEntity() { super(null, null, null); }

    @Inject(method = "deserializeLootTable", at = @At("HEAD"))
    private void deserializeMimic(NbtCompound nbt, CallbackInfoReturnable<Boolean> info) {
        isMimic = nbt.getBoolean("mimic");
    }

    @Inject(method = "serializeLootTable", at = @At("HEAD"))
    private void serializeMimic(NbtCompound nbt, CallbackInfoReturnable<Boolean> info) {
        nbt.putBoolean("mimic", isMimic);
    }

    @Override
    public void setAllowMimics(boolean allowMimics) {
        this.allowMimics = allowMimics;
        this.isMimic &= allowMimics;
        markDirty();
    }

    @Override
    public void setMimic(boolean mimic) {
        isMimic = mimic;
        markDirty();
    }

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void onCheckLootInteraction(@Nullable PlayerEntity player, CallbackInfo info) {
        if (player != null && allowMimics && lootTableId != null) {
            mimicLootTable = lootTableId;
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
        if (player != null && (isMimic || (allowMimics && MimicEntity.shouldConvert(player.getWorld(), getPos(), player, mimicLootTable)))) {
            var mimic = MimicEntity.spawnFromChest(player.getWorld(), getPos());
            if (mimic != null) {
                info.setReturnValue(mimic.createScreenHandler(syncId, playerInventory, player));
            }
            mimicLootTable = null;
        }
    }
}
