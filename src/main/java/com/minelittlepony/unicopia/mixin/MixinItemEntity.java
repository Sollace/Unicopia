package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ducks.IItemEntity;
import com.minelittlepony.unicopia.equine.ItemImpl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.CompoundTag;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity implements IItemEntity {

    private ItemImpl caster;

    private MixinItemEntity() { super(null, null); }

    @Override
    public ItemImpl create() {
        return new ItemImpl((ItemEntity)(Object)this);
    }

    @Override
    public ItemImpl get() {
        if (caster == null) {
            caster = create();
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

    @Inject(method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompound("unicopia_caster"));
        }
    }

    @Inject(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT());
    }

    @Accessor("age")
    @Override
    public abstract int getAge();

    @Accessor("pickupDelay")
    @Override
    public abstract int getPickupDelay();

}
