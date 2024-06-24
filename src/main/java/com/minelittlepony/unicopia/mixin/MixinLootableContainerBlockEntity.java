package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

@Mixin(LootableContainerBlockEntity.class)
abstract class MixinLootableContainerBlockEntity extends LockableContainerBlockEntity implements MimicEntity.MimicGeneratable {
    private Identifier mimicLootTable;
    private boolean allowMimics = true;
    private TriState isMimic = TriState.DEFAULT;

    @Shadow
    @Nullable
    private Identifier lootTableId;

    MixinLootableContainerBlockEntity() { super(null, null, null); }

    @Override
    public void readMimicAttributes(NbtCompound nbt) {
        isMimic = nbt.contains("mimic") ? TriState.of(nbt.getBoolean("mimic")) : TriState.DEFAULT;
    }

    @Override
    public void writeMimicAttributes(NbtCompound nbt) {
        if (isMimic != TriState.DEFAULT) {
            nbt.putBoolean("mimic", isMimic.get());
        }
    }

    @Override
    public void configureMimic(@Nullable PlayerEntity player) {
        if (player != null && allowMimics && lootTableId != null) {
            mimicLootTable = lootTableId;
        }
    }

    @Override
    public void setAllowMimics(boolean allowMimics) {
        this.allowMimics = allowMimics;
        markDirty();
    }

    @Override
    public void setMimic(boolean mimic) {
        isMimic = TriState.of(mimic);
        markDirty();
    }

    @Inject(
            method = "createMenu",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/block/entity/LootableContainerBlockEntity.generateLoot(Lnet/minecraft/entity/player/PlayerEntity;)V",
                shift = Shift.AFTER
    ), cancellable = true)
    private void onCreateMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<ScreenHandler> info) {
        if (player != null && allowMimics) {
            if (isMimic == TriState.DEFAULT) {
                isMimic = TriState.of(MimicEntity.shouldConvert(player.getWorld(), getPos(), player, mimicLootTable));
            }

            if (isMimic.get()) {
                var mimic = MimicEntity.spawnFromChest(player.getWorld(), getPos());
                if (mimic != null) {
                    info.setReturnValue(mimic.createScreenHandler(syncId, playerInventory, player));
                }
            }
            mimicLootTable = null;
        }
    }
}

@Mixin(LootableInventory.class)
interface MixinLootableInventory {
    @Inject(method = "generateLoot", at = @At("HEAD"))
    default void onGenerateLoot(@Nullable PlayerEntity player, CallbackInfo info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.configureMimic(player);
        }
    }

    @Inject(method = "readLootTable", at = @At("HEAD"))
    default void onReadLootTable(NbtCompound nbt, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.readMimicAttributes(nbt);
        }
    }

    @Inject(method = "writeLootTable", at = @At("HEAD"))
    default void onWriteLootTable(NbtCompound nbt, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.writeMimicAttributes(nbt);
        }
    }
}
