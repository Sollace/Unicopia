package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.PlayerEntityDuck;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.annotation.CaptureArg;
import com.mojang.datafixers.util.Either;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity implements PlayerEntityDuck {
    private MixinPlayerEntity() { super(null, null); }
    @Override
    @Invoker("updateCapeAngles")
    public abstract void callUpdateCapeAngles();

    @Override
    public Equine<?> create() {
        return new Pony(Untyped.cast(this));
    }

    @ModifyReturnValue(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder onCreateAttributes(DefaultAttributeContainer.Builder builder) {
        Pony.registerAttributes(builder);
        return builder;
    }

    @ModifyVariable(method = "applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    protected float modifyDamageAmount(float amount, @CaptureArg ServerWorld world, @CaptureArg DamageSource source) {
        return this.<Pony>get().modifyDamage(source, amount).orElse(amount);
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;", at = @At("HEAD"), cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (!getWorld().isClient) {
            this.<Pony>get().trySleep(pos).ifPresent(reason -> {
                ((PlayerEntity)(Object)this).sendMessage(reason, true);
                info.setReturnValue(Either.left(ServerPlayerEntity.SleepFailureReason.OTHER_PROBLEM));
            });
        }
    }

    @ModifyReturnValue(method = "getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;", at = @At("RETURN"))
    private EntityDimensions modifyEyeHeight(EntityDimensions dimensions, EntityPose pose) {
        return this.<Pony>get().getMotion().getDimensions().calculateDimensions(dimensions);
    }

    @ModifyReturnValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"))
    private float onGetBlockBreakingSpeed(float speed, BlockState state) {
        return speed * this.<Pony>get().getBlockBreakingSpeed();
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        if (EquinePredicates.PLAYER_SEAPONY.test(this)) {
            return super.getNextAirOnLand(air);
        }
        return super.getNextAirUnderwater(air);
    }

    @Override
    protected int getNextAirOnLand(int air) {
        if (EquinePredicates.PLAYER_SEAPONY.test(this)) {
            return super.getNextAirUnderwater(air);
        }
        return super.getNextAirOnLand(air);
    }
}
