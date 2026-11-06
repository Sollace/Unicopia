package com.minelittlepony.unicopia.mixin;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.Transportation;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Entity.PositionUpdater;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck, Trackable, Transportation.Holder<Entity> {
    @Nullable
    private transient Caster<?> host;

    private DataTrackerManager dataTrackerManager;

    private final Transportation<Entity> transportation = new Transportation<>((Entity)(Object)this);

    @Override
    @Nullable
    public Caster<?> getHost() {
        return host;
    }

    @Override
    public Transportation<Entity> get() {
        return transportation;
    }

    @Override
    public void setHost(Caster<?> host) {
        this.host = host;
    }

    @Override
    public DataTrackerManager getDataTrackers() {
        synchronized (this) {
            if (dataTrackerManager == null) {
                dataTrackerManager = new DataTrackerManager((Entity)(Object)this);
                // ensure lazy registration happens
                if (this instanceof Equine.Container<?> eq) {
                    eq.get();
                }
            }
            return dataTrackerManager;
        }
    }

    @Override
    @Accessor("submergedFluidTag")
    public abstract Set<TagKey<Fluid>> getSubmergedFluidTags();

    @Override
    @Accessor
    public abstract void setRemovalReason(RemovalReason reason);

    @Override
    @Accessor
    public abstract void setVehicle(Entity vehicle);

    @Override
    public boolean isLavaAffine() {
        Entity self = (Entity)(Object)this;
        return self.hasVehicle() && self.getVehicle() instanceof LavaAffine affine && affine.isLavaAffine();
    }

    @Inject(method = "isInvulnerable", at = @At("HEAD"), cancellable = true)
    private void onIsInvulnerable(CallbackInfoReturnable<Boolean> info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null && living.isInvulnerable()) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void onIsFireImmune(CallbackInfoReturnable<Boolean> info) {
        if (isLavaAffine() || (this instanceof Equine.Container c) && c.get().getCompositeRace().includes(Race.KIRIN)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isSneaky", at = @At("HEAD"), cancellable = true)
    private void onIsSneaky(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this) && !EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    private void onGetMaxAir(CallbackInfoReturnable<Integer> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this)) {
            info.setReturnValue(150);
        }
    }

    @Inject(method = "doesRenderOnFire", at = @At("HEAD"), cancellable = true)
    private void onDoesRenderOnFire(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater, CallbackInfo info) {
        if (Living.getOrEmpty((Entity)(Object)this).filter(l -> l.onUpdatePassengerPosition(passenger, positionUpdater)).isPresent()) {
            info.cancel();
        }
    }

    @Inject(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropStack(ServerWorld world, ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> info) {
        if (hasHost()) {
            info.setReturnValue(null);
        }
    }

    @ModifyReturnValue(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private @Nullable ItemEntity onDropItem(@Nullable ItemEntity item) {
        if (item != null) {
            Living.getOrEmpty((Entity)(Object)this).ifPresent(l -> l.onDropItem(item));
        }
        return item;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d beforeMove(Vec3d velocity, MovementType movementType, Vec3d copyOfMovement) {
        return transportation.beforeMove(velocity, movementType);
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void afterMove(MovementType movementType, Vec3d movement, CallbackInfo info) {
        transportation.afterMove(movementType);
    }

    @ModifyVariable(method = "applyMoveEffect", at = @At("HEAD"), argsOnly = true)
    private BlockState modifyPrimarySteppingStateOnMove(BlockState state) {
        return transportation.getSteppingState().orElse(state);
    }

    @ModifyVariable(method = "applyMoveEffect", at = @At("STORE"), ordinal = 1)
    private BlockState modifySteppingStateOnMove(BlockState state) {
        return transportation.getSteppingState().orElse(state);
    }
}
