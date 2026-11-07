package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity implements IItemEntity {
    private MixinItemEntity() { super(null, null); }

    @Override
    public Equine<?> create() {
        return new ItemImpl(Untyped.cast(this));
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

    @Accessor("itemAge")
    @Override
    public abstract int getAge();

    @Accessor("pickupDelay")
    @Override
    public abstract int getPickupDelay();
}
