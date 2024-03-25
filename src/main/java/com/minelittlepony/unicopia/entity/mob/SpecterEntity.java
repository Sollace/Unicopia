package com.minelittlepony.unicopia.entity.mob;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.particle.FootprintParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SpecterEntity extends HostileEntity {
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 16F)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5F);
    }

    private double stepDistance;
    private double nextStepDistance;
    private boolean wasLeft;

    public SpecterEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick() {
        Vec3d prevPosition = getPos();
        super.tick();
        if (getBrightnessAtEyes() < 0.5F || getTarget() != null) {
            ParticleUtils.spawnParticles(ParticleTypes.AMBIENT_ENTITY_EFFECT, this, 6);

            if (getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                if (getWorld().getBlockState(getBlockPos()).isIn(BlockTags.REPLACEABLE_BY_TREES)) {
                    getWorld().breakBlock(getBlockPos(), true);
                }
            }
        }

        if (!hasVehicle() && isOnGround()) {
            stepDistance += getPos().subtract(prevPosition).horizontalLength() * 0.6F;
            if (stepDistance >= nextStepDistance) {
                nextStepDistance = stepDistance + 1;
                wasLeft = !wasLeft;
                float offset = 0.4F;
                float yaw = getHeadYaw();
                Vec3d offsetVec = new Vec3d((wasLeft ? offset : -offset), 0, 0).rotateY(yaw);
                getWorld().addParticle(new FootprintParticleEffect(yaw), true, getX() + offsetVec.getX(), getY(), getZ() + offsetVec.getZ(), 0, 0, 0);
                ParticleUtils.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, getSteppingBlockState()), getWorld(), getPos(), 6);
                playSound(getSteppingBlockState().getSoundGroup().getStepSound(), 0.5F, 1);
            }
        }
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * 0.3F;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected void playSwimSound(float volume) {

    }

    @Override
    protected void onSwimmingStart() {

    }

    static class TargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public TargetGoal(SpecterEntity specter, Class<T> targetEntityClass) {
            super(specter, targetEntityClass, true);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean canStart() {
            return mob.getBrightnessAtEyes() < 0.5F && super.canStart();
        }
    }
}
