package com.minelittlepony.unicopia.mixin;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Entity.PositionUpdater;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck, Trackable {
    @Nullable
    private transient Caster<?> host;

    private DataTrackerManager dataTrackerManager;

    @Override
    @Nullable
    public Caster<?> getHost() {
        return host;
    }

    @Override
    public void setHost(Caster<?> host) {
        this.host = host;
    }

    @Override
    public DataTrackerManager getDataTrackers() {
        if (dataTrackerManager == null) {
            dataTrackerManager = new DataTrackerManager((Entity)(Object)this);
        }
        return dataTrackerManager;
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
    @Accessor
    public abstract float getNextStepSoundDistance();

    @Override
    public boolean isLavaAffine() {
        Entity self = (Entity)(Object)this;
        return self.hasVehicle() && self.getVehicle() instanceof LavaAffine affine && affine.isLavaAffine();
    }


    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.initDataTracker()V"))
    private void onInstanceInit(EntityType<?> type, World world, CallbackInfo info) {
        if (this instanceof Equine.Container c) {
            c.get().initDataTracker();
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

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        if (dataTrackerManager != null) {
            dataTrackerManager.tick();
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

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> info) {
        if (getHost() != null) {
            info.setReturnValue(null);
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void beforeMove(MovementType movementType, Vec3d movement, CallbackInfo info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null) {
            living.getTransportation().updatePreviousPosition();
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void afterMove(MovementType movementType, Vec3d movement, CallbackInfo info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null) {
            living.getTransportation().onMove(movementType);
        }
    }
}
