package com.minelittlepony.unicopia.entity.mob;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.FootprintParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
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
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
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
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(4, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(6, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        //this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class));
    }

    @Override
    public void tick() {
        Vec3d prevPosition = getPos();
        super.tick();

        if (getTarget() != null) {
            ParticleUtils.spawnParticles(ParticleTypes.EFFECT, this, 6);

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
    public void setAttacker(@Nullable LivingEntity attacker) {
        if (!getWorld().isClient && attacker != null) {
            getWorld().getEntitiesByClass(SpecterEntity.class, this.getBoundingBox().expand(5), e -> e != this && e.getTarget() == null).forEach(specter -> {
                specter.notifyPartyAttacker(this, attacker);
            });
        }
        super.setAttacker(attacker);
    }

    private void notifyPartyAttacker(SpecterEntity sender, @Nullable LivingEntity attacker) {
        super.setAttacker(attacker);
        getNavigation().stop();
        getNavigation().startMovingTo(sender, 3);
        playSound(USounds.Vanilla.ENTITY_VEX_HURT, 1, 0.5F);
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

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData data, @Nullable NbtCompound entityNbt) {
        data = super.initialize(world, difficulty, spawnReason, data, entityNbt);
        Random random = world.getRandom();
        float diff = difficulty.getClampedLocalDifficulty();
        setCanPickUpLoot(random.nextFloat() < 0.55F * diff);
        initEquipment(random, difficulty);
        return data;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < (getWorld().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            if (random.nextFloat() < (getWorld().getDifficulty() == Difficulty.HARD ? 0.5F : 0.1F)) {
                super.initEquipment(random, localDifficulty);
            }
            equipStack(EquipmentSlot.MAINHAND, (random.nextInt(3) == 0 ? Items.STONE_SWORD : Items.WOODEN_SWORD).getDefaultStack());
        }
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
