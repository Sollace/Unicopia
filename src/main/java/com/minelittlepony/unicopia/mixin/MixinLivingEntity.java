package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.*;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements LivingEntityDuck, Equine.Container<Living<?>> {
    @Shadow
    protected ItemStack activeItemStack;
    @Shadow
    protected int itemUseTimeLeft;
    @Shadow
    protected boolean dead;

    @Shadow
    private Optional<BlockPos> climbingPos;

    private Equine<?> caster;

    private MixinLivingEntity() { super(null, null); }

    @Shadow
    protected abstract void setLivingFlag(int mask, boolean value);

    @Override
    public Equine<?> create() {
        return new Creature((LivingEntity)(Object)this);
    }

    @Override
    public Living<?> get() {
        synchronized (this) {
            if (caster == null) {
                caster = create();
            }
            return (Living<?>)caster;
        }
    }

    @Override
    @Accessor("jumping")
    public abstract boolean isJumping();

    @Override
    @Accessor("leaningPitch")
    public abstract float getLeaningPitch();

    @Override
    @Accessor("leaningPitch")
    public abstract void setLeaningPitch(float pitch);

    @Override
    @Accessor("lastLeaningPitch")
    public abstract float getLastLeaningPitch();

    @Override
    @Accessor("lastLeaningPitch")
    public abstract void setLastLeaningPitch(float pitch);

    @Inject(method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", at = @At("RETURN"))
    private static void onCreateAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        Creature.registerAttributes(info.getReturnValue());
    }

    @Inject(method = "isClimbing()Z", at = @At("HEAD"), cancellable = true)
    public void onIsClimbing(CallbackInfoReturnable<Boolean> info) {
        if (horizontalCollision) {
            get().chooseClimbingPos().ifPresent(pos -> {
                climbingPos = Optional.of(pos);
                info.setReturnValue(true);
            });
        }
    }

    @Inject(method = "isPushable()Z", at = @At("HEAD"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable<Boolean> info) {
        Caster.of(this)
            .flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE))
            .map(AbstractDisguiseSpell::getDisguise)
            .map(EntityAppearance::getAppearance)
            .filter(Entity::isPushable)
            .ifPresent(v -> {
                info.setReturnValue(false);
            });
    }

    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/RaycastContext$ShapeType;Lnet/minecraft/world/RaycastContext$FluidHandling;D)Z", at = @At("HEAD"), cancellable = true)
    private void onCanSee(Entity entity, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, double entityY, CallbackInfoReturnable<Boolean> info) {
        if (!get().canBeSeenBy(entity)) {
            info.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "applyFluidMovingSpeed", at = @At("RETURN"))
    private Vec3d applyFluidMovingSpeed(Vec3d speed, double gravity, boolean falling, Vec3d motion) {
        return get().adjustMovementSpeedInWater(speed).orElse(speed);
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (get().beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        get().tick();
    }

    @Dynamic("Compiler-generated class-init() method")
    @Inject(method = "<clinit>()V", at = @At("RETURN"), remap = false)
    private static void clinit(CallbackInfo info) {
        Creature.boostrap();
    }

    @Inject(method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        get().onDamage(world, source, amount).ifPresent(info::setReturnValue);
    }

    @Inject(method = "tryUseDeathProtector(Lnet/minecraft/entity/damage/DamageSource;)Z", at = @At("RETURN"))
    private void onOnDeath(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        if (!isRemoved() && !dead) {
            get().onDeath(source, info.getReturnValue());
        }
    }

    @Inject(method = "onAttacking(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void onOnAttacking(Entity target, CallbackInfo info) {
        get().onAttacking(target);
    }

    @ModifyVariable(method = "handleFallDamage(DFLnet/minecraft/entity/damage/DamageSource;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double onHandleFallDamage(double distance, double distanceAgain, float damagePerDistance, DamageSource cause) {
        return get().onImpact(distance, damagePerDistance, cause);
    }

    @ModifyReturnValue(method = "hurtByWater()Z", at = @At("RETURN"))
    private boolean onCanBeHurtByWater(boolean hurt, CallbackInfoReturnable<Boolean> info) {
        TriState hurtByWater = get().canBeHurtByWater();
        if (hurtByWater != TriState.DEFAULT) {
            return hurtByWater.get();
        }
        return hurt;
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT(getWorld().getRegistryManager()));
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompoundOrEmpty("unicopia_caster"), getWorld().getRegistryManager());
        }
    }

    @Override
    public void updateItemUsage(Hand hand, ItemStack stack, int time) {
        activeItemStack = stack;
        itemUseTimeLeft = time;

        if (!getWorld().isClient) {
            setLivingFlag(1, !stack.isEmpty());
            setLivingFlag(2, hand == Hand.OFF_HAND);
        }
    }
}
