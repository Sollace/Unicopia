package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.Equine;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;

@Mixin(MobEntity.class)
abstract class MixinMobEntity extends LivingEntity implements PonyContainer<Equine<?>> {
    private MixinMobEntity() { super(null, null); }

    @Shadow
    protected @Final GoalSelector goalSelector;
    @Shadow
    protected @Final GoalSelector targetSelector;

    @Inject(method = "<init>()V", at = @At("RETURN"), remap = false)
    private void init(CallbackInfo info) {
        ((Creature)get()).initAi(goalSelector, targetSelector);
    }
}
