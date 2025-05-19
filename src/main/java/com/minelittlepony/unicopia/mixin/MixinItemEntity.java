package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity implements IItemEntity {

    private ItemImpl caster;

    private MixinItemEntity() { super(null, null); }

    @Override
    public Equine<?> create() {
        return new ItemImpl((ItemEntity)(Object)this);
    }

    @Override
    public ItemImpl get() {
        if (caster == null) {
            caster = (ItemImpl)create();
        }
        return caster;
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (get().beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        get().tick();
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT(getWorld().getRegistryManager()));
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompound("unicopia_caster"), getWorld().getRegistryManager());
        }
    }

    @Accessor("itemAge")
    @Override
    public abstract int getAge();

    @Accessor("pickupDelay")
    @Override
    public abstract int getPickupDelay();

}
