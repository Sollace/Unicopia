package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ducks.IRaceContainerHolder;
import com.minelittlepony.unicopia.entity.IEntity;
import com.minelittlepony.unicopia.entity.capabilities.ItemEntityCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements IRaceContainerHolder<IEntity> {

    private final IEntity caster = createRaceContainer();

    private MixinItemEntity() { super(null, null); }

    @Override
    public IEntity createRaceContainer() {
        return new ItemEntityCapabilities((ItemEntity)(Object)this);
    }

    @Override
    public IEntity getRaceContainer() {
        return caster;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void beforeTick(CallbackInfo info) {
        caster.beforeUpdate();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        caster.onUpdate();
    }
}
