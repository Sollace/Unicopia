package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.RotatedView;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
abstract class MixinMobEntity extends LivingEntity implements PonyContainer<Equine<?>> {
    private MixinMobEntity() { super(null, null); }

    @Shadow
    protected @Final GoalSelector goalSelector;
    @Shadow
    protected @Final GoalSelector targetSelector;

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo info) {
        ((Creature)get()).initAi(goalSelector, targetSelector);
    }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    public void beforeTickAi(CallbackInfo into) {
        Equine<?> eq = Equine.of(this).orElse(null);

        if (eq instanceof Living<?> && eq.getPhysics().isGravityNegative()) {
            ((RotatedView)world).pushRotation((int)getY());
        }
    }

    @Inject(method = "tickNewAi", at = @At("RETURN"))
    public void afterTickAi(CallbackInfo into) {
        ((RotatedView)world).popRotation();
    }
}
