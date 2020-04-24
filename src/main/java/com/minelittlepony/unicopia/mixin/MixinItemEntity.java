package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ducks.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemEntityCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity implements IItemEntity {

    private final ItemEntityCapabilities caster = create();

    private MixinItemEntity() { super(null, null); }

    @Override
    public ItemEntityCapabilities create() {
        return new ItemEntityCapabilities((ItemEntity)(Object)this);
    }

    @Override
    public ItemEntityCapabilities get() {
        return caster;
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (caster.beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        caster.onUpdate();
    }

    @Accessor("age")
    @Override
    public abstract int getAge();

    @Accessor("pickupDelay")
    @Override
    public abstract int getPickupDelay();
}
