package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import com.minelittlepony.unicopia.item.enchantment.WantItNeedItEnchantment;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
abstract class MixinMobEntity extends LivingEntity implements Equine.Container<Creature> {
    private MixinMobEntity() { super(null, null); }

    @Shadow
    protected @Final GoalSelector goalSelector;
    @Shadow
    protected @Final GoalSelector targetSelector;

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo info) {
        get().initAi(goalSelector, targetSelector);
    }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    public void beforeTickAi(CallbackInfo into) {
        if (get().getPhysics().isGravityNegative()) {
            ((RotatedView)world).pushRotation((int)getY());
        }
    }

    @Inject(method = "tickNewAi", at = @At("RETURN"))
    public void afterTickAi(CallbackInfo into) {
        ((RotatedView)world).popRotation();
    }

    @Inject(method = "prefersNewEquipment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private void onPrefersNewEquipment(ItemStack newStack, ItemStack oldStack, CallbackInfoReturnable<Boolean> info) {
        if (WantItNeedItEnchantment.prefersEquipment(newStack, oldStack)) {
            info.setReturnValue(true);
        }
    }
}
