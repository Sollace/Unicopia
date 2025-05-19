package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.item.BaitedFishingRodItem;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;

@Mixin(FishingBobberEntity.class)
abstract class MixinFishingBobberEntity extends ProjectileEntity implements BaitedFishingRodItem.BaitedFishingBobber {
    private MixinFishingBobberEntity() { super(null, null); }

    @Nullable
    private Item rodType;

    @Nullable
    @Override
    public Item getRodType() {
        return rodType;
    }

    @Override
    public void setRodType(Item rodType) {
        this.rodType = rodType;
    }

    @ModifyExpressionValue(method = "removeIfInvalid", at = {
            @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.getMainHandStack()Lnet/minecraft/item/ItemStack;"),
            @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.getOffHandStack()Lnet/minecraft/item/ItemStack;")
    }, expect = 2)
    private ItemStack replaceFishingRodItem(ItemStack initialStack) {
        if (rodType != null) {
            return initialStack.isOf(rodType) ? Items.FISHING_ROD.getDefaultStack() : ItemStack.EMPTY;
        }
        return initialStack;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        nbt.putString("rodType", Registries.ITEM.getId(rodType == null ? Items.FISHING_ROD : rodType).toString());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        rodType = nbt.contains("rodType", NbtElement.STRING_TYPE)
                ? NbtSerialisable.decode(Registries.ITEM.getCodec(), nbt.get("rodType"), getRegistryManager()).orElse(null)
                : null;
    }
}
