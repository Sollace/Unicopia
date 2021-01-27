package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.datafixers.util.Either;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity implements PonyContainer<Pony> {
    private MixinPlayerEntity() { super(null, null); }

    @Override
    public Equine<?> create() {
        return new Pony((PlayerEntity)(Object)this);
    }

    @Inject(method = "handleFallDamage(FF)Z", at = @At("HEAD"), cancellable = true)
    private void onHandleFallDamage(float distance, float damageMultiplier, CallbackInfoReturnable<Boolean> info) {
        get().onImpact(fallDistance, damageMultiplier).ifPresent(newDistance -> {
            PlayerEntity self = (PlayerEntity)(Object)this;

            if (newDistance >= 2) {
                self.increaseStat(Stats.FALL_ONE_CM, Math.round(newDistance * 100));
            }

            info.setReturnValue(super.handleFallDamage(newDistance, damageMultiplier));
        });
    }

    @Inject(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", at = @At("RETURN"))
    private static void onCreateAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        Pony.registerAttributes(info.getReturnValue());
    }

    @Inject(method = "eatFood(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"))
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        if (stack.isFood()) {
            get().onEat(stack);
        }
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (!world.isClient) {
            get().trySleep(pos).ifPresent(reason -> {
                ((PlayerEntity)(Object)this).sendMessage(reason, true);

                info.setReturnValue(Either.right(Unit.INSTANCE));
            });
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("RETURN"))
    private void onDropItem(ItemStack itemStack_1, boolean scatter, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> info) {
        PonyContainer.of(info.getReturnValue()).ifPresent(container -> {
            container.get().setSpecies(get().getSpecies());
        });
    }

    @Inject(method = "setGameMode(Lnet/minecraft/world/GameMode;)V",
            at = @At("RETURN"))
    private void onSetGameMode(GameMode mode, CallbackInfo info) {
        get().setSpecies(get().getSpecies());
        get().setDirty();
    }

    @Inject(method = "getActiveEyeHeight(Lnet/minecraft/entity/EntityPose;Lnet/minecraft/entity/EntityDimensions;)F",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(get().getMotion().getDimensions().calculateActiveEyeHeight(dimensions, info.getReturnValue()));
    }

    @Inject(method = "getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> info) {
        info.setReturnValue(get().getMotion().getDimensions().calculateDimensions(info.getReturnValue()));
    }

    @Inject(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(info.getReturnValue() * get().getBlockBreakingSpeed());
    }
}
