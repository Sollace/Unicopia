package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.mojang.datafixers.util.Either;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerPlayerEntity.class)
abstract class MixinServerPlayerEntity extends PlayerEntity implements ScreenHandlerListener, Equine.Container<Pony>, ServerPlayerEntityDuck {
    MixinServerPlayerEntity() {super(null, null, 0, null);}

    @Override
    @Accessor("inTeleportationState")
    public abstract void setPreventMotionChecks(boolean enabled);

    @SuppressWarnings("unchecked")
    @Inject(method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At("HEAD"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
        get().copyFrom(((Equine.Container<Pony>)oldPlayer).get(), alive);
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At(value = "FIELD", target = "net/minecraft/entity/player/PlayerEntity$SleepFailureReason.NOT_POSSIBLE_NOW:Lnet/minecraft/entity/player/PlayerEntity$SleepFailureReason;"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (get().getSpecies().isNocturnal() && get().asWorld().getGameRules().getBoolean(UGameRules.DO_NOCTURNAL_BAT_PONIES)) {
            ((PlayerEntity)this).sendMessage(Text.translatable("block.unicopia.bed.no_sleep.nocturnal"), true);

            info.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM));
        }
    }

    @Inject(method = "updateKilledAdvancementCriterion(Lnet/minecraft/entity/Entity;ILnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("TAIL"))
    private void onUpdateKilledAdvancementCriterion(Entity entityKilled, int score, DamageSource damageSource, CallbackInfo info) {
        get().onKill(entityKilled, damageSource);
    }
}
