package com.minelittlepony.unicopia.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.core.ducks.IItemEntity;
import com.minelittlepony.unicopia.core.entity.ItemEntityCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements IItemEntity {

    private final ItemEntityCapabilities caster = createRaceContainer();

    private MixinItemEntity() { super(null, null); }

    @Override
    public ItemEntityCapabilities createRaceContainer() {
        return new ItemEntityCapabilities((ItemEntity)(Object)this);
    }

    @Override
    public ItemEntityCapabilities getRaceContainer() {
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
