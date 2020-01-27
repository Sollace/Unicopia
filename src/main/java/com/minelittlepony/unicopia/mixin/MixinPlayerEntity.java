package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.ducks.IRaceContainerHolder;
import com.minelittlepony.unicopia.entity.IEntity;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.minelittlepony.unicopia.entity.capabilities.PlayerCapabilities;
import com.mojang.datafixers.util.Either;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IRaceContainerHolder<IPlayer> {
    private MixinPlayerEntity() { super(null, null); }

    @Override
    public IEntity createRaceContainer() {
        return new PlayerCapabilities((PlayerEntity)(Object)this);
    }

    @ModifyArg(method = "Lnet/minecraft/entity/LivingEntity;handleFallDamage(FF)V",
            at = @At("HEAD"),
            index = 0)
    private float onHandleFallDamage(float distance) {
        return getRaceContainer().onImpact(distance);
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (!world.isClient) {
            Either<PlayerEntity.SleepFailureReason, Unit> result = getRaceContainer().trySleep(pos);

            result.ifLeft(reason -> info.setReturnValue(result));
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD"))
    private void onDropItem(ItemStack itemStack_1, boolean a, boolean b, CallbackInfoReturnable<ItemEntity> info) {
        SpeciesList.instance().getForEntity(info.getReturnValue()).ifPresent(o -> {
            o.getRaceContainer().setSpecies(getRaceContainer().getSpecies());
        });
    }

    @Inject(method = "setGameMode(Lnet/minecraft/world/GameMode;)V",
            at = @At("RETURN"))
    public void setGameMode(GameMode mode, CallbackInfo info) {
        getRaceContainer().setSpecies(getRaceContainer().getSpecies());
    }
}
