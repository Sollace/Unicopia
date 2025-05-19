package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.server.world.ServerWorld;

@Mixin(Brain.class)
abstract class MixinBrain<E extends LivingEntity> {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(ServerWorld world, E entity, CallbackInfo info) {
        if (Living.living(entity) instanceof Creature c && c.isDiscorded()) {
            info.cancel();
        }
    }
}
