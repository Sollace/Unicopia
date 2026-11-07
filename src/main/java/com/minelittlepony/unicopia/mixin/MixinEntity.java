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
import com.minelittlepony.unicopia.util.Untyped;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.BaseEquine;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Entity.PositionUpdater;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck {
    @Nullable
    private Equine<?> caster;

    @Override
    public Entity asEntity() {
        return (Entity)(Object)this;
    }

    @Override
    @Nullable
    public Caster<?> getHost() {
        return ((BaseEquine<?>)get()).getHost();
    }

    @Override
    public Equine<?> create() {
        return new BaseEquine<>(this);
    }

    @Override
    public final <T extends Equine<?>> T get() {
        if (caster == null) {
            caster = create();
        }
        return Untyped.cast(caster);
    }

    @Override
    public void setHost(Caster<?> host) {
        ((BaseEquine<?>)get()).setHost(host);
    }

    @Override
    public DataTrackerManager getDataTrackers() {
        synchronized (this) {
            return ((BaseEquine<?>)get()).getDataTrackerManager();
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
        return asEntity().hasVehicle() && asEntity().getVehicle() instanceof LavaAffine affine && affine.isLavaAffine();
    }

    @Inject(method = "writeNbt",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/entity/Entity.writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"
    ))
    private void onWriteNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {
        nbt.put("unicopia_caster", get().toNBT(asEntity().getRegistryManager()));
    }

    @Inject(method = "readNbt",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/entity/Entity.readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"
    ))
    private void onReadNbt(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("unicopia_caster")) {
            get().fromNBT(nbt.getCompoundOrEmpty("unicopia_caster"), asEntity().getRegistryManager());
        }
    }

    @ModifyReturnValue(method = "isInvulnerable", at = @At("RETURN"))
    private boolean onIsInvulnerable(boolean invulnerable) {
        return invulnerable || (get() instanceof Living l && l.isInvulnerable());
    }

    @ModifyReturnValue(method = "isFireImmune", at = @At("RETURN"))
    private boolean onIsFireImmune(boolean fireImmune) {
        return fireImmune || (isLavaAffine() || get().getCompositeRace().includes(Race.KIRIN));
    }

    @ModifyReturnValue(method = "isSneaky", at = @At("RETURN"))
    private boolean onIsSneaky(boolean sneaky) {
        return sneaky || (EquinePredicates.PLAYER_KIRIN.test(asEntity()) && !EquinePredicates.RAGING.test(asEntity()));
    }

    @ModifyReturnValue(method = "getMaxAir", at = @At("RETURN"))
    private int onGetMaxAir(int maxAir) {
        return maxAir / (EquinePredicates.PLAYER_KIRIN.test(asEntity()) ? 2 : 1);
    }

    @ModifyReturnValue(method = "doesRenderOnFire", at = @At("RETURN"))
    private boolean onDoesRenderOnFire(boolean onFire) {
        return onFire || EquinePredicates.RAGING.test(asEntity());
    }

    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater, CallbackInfo info) {
        if (get() instanceof Living l && l.onUpdatePassengerPosition(passenger, positionUpdater)) {
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
        if (item != null && get() instanceof Living l) {
            l.onDropItem(item);
        }
        return item;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d beforeMove(Vec3d velocity, MovementType movementType, Vec3d copyOfMovement) {
        return get().getTransportation().beforeMove(velocity, movementType);
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void afterMove(MovementType movementType, Vec3d movement, CallbackInfo info) {
        get().getTransportation().afterMove(movementType);
    }

    @ModifyVariable(method = "applyMoveEffect", at = @At("HEAD"), argsOnly = true)
    private BlockState modifyPrimarySteppingStateOnMove(BlockState state) {
        return get().getTransportation().getSteppingState().orElse(state);
    }

    @ModifyVariable(method = "applyMoveEffect", at = @At("STORE"), ordinal = 1)
    private BlockState modifySteppingStateOnMove(BlockState state) {
        return get().getTransportation().getSteppingState().orElse(state);
    }
}
