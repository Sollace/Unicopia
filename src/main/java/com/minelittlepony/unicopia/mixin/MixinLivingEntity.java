package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements LivingEntityDuck, Equine.Container<Living<?>> {
    @Shadow
    protected ItemStack activeItemStack;
    @Shadow
    protected int itemUseTimeLeft;

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
        if (caster == null) {
            caster = create();
        }
        return (Living<?>)caster;
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

    @Override
    @Accessor
    public abstract double getServerX();

    @Override
    @Accessor
    public abstract double getServerY();

    @Override
    @Accessor
    public abstract double getServerZ();

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
            .flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE, false))
            .map(AbstractDisguiseSpell::getDisguise)
            .map(EntityAppearance::getAppearance)
            .filter(Entity::isPushable)
            .ifPresent(v -> {
                info.setReturnValue(false);
            });
    }

    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void onCanSee(Entity other, CallbackInfoReturnable<Boolean> info) {
        if (!get().canBeSeenBy(other)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "jump()V", at = @At("RETURN"))
    private void onJump(CallbackInfo info) {
        get().onJump();
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

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        get().onDamage(source, amount).ifPresent(info::setReturnValue);
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT());
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompound("unicopia_caster"));
        }
    }

    @ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = {
            @Constant(doubleValue = 0.08D),
            @Constant(doubleValue = 0.01D)
    })
    private double modifyGravity(double initial) {
        return get().getPhysics().calcGravity(initial);
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

    @Override
    public BlockPos getBlockPos() {
        if (get().getPhysics().isGravityNegative()) {
            return get().getPhysics().getHeadPosition();
        }
        return super.getBlockPos();
    }

    @Override
    protected void spawnSprintingParticles() {
        if (get().getPhysics().isGravityNegative()) {
            get().getPhysics().spawnSprintingParticles();
        } else {
            super.spawnSprintingParticles();
        }
    }
}
