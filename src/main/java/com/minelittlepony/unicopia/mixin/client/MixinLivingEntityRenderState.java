package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Mixin(LivingEntityRenderState.class)
abstract class MixinLivingEntityRenderState implements CasterState.Container {
    private CasterState unicopia_casterState;

    @Override
    public CasterState getUnicopiaState() {
        if (unicopia_casterState == null) {
            unicopia_casterState = new CasterState((LivingEntityRenderState)(Object)this);
        }
        return unicopia_casterState;
    }
}
