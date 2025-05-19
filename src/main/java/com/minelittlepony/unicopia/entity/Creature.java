package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.FleeExplosionGoal;
import com.minelittlepony.unicopia.entity.ai.PredicatedGoal;
import com.minelittlepony.unicopia.entity.ai.PrioritizedActiveTargetGoal;
import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.MathHelper;

public class Creature extends Living<LivingEntity> implements WeaklyOwned.Mutable<LivingEntity> {
    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private Optional<GoalSelector> goals = Optional.empty();

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    private int smittenTicks;

    private final Predicate<LivingEntity> targetPredicate = TargetSelecter.<LivingEntity>validTarget(() -> getOriginatingCaster().getAffinity(), this).and(e -> {
        return Equine.of(e)
                .filter(eq -> eq instanceof Creature)
                .filter(eq -> isDiscorded() != ((Creature)eq).hasCommonOwner(this))
                .isEmpty();
    });

    protected final DataTracker.Entry<Integer> eating;
    protected final DataTracker.Entry<Boolean> discorded;
    protected final DataTracker.Entry<Boolean> smitten;

    public Creature(LivingEntity entity) {
        super(entity);
        physics = new EntityPhysics<>(entity);
        addTicker(physics);
        addTicker(this::updateConsumption);

        tracker.startTracking(owner);
        eating = tracker.startTracking(TrackableDataType.INT, 0);
        discorded = tracker.startTracking(TrackableDataType.BOOLEAN, false);
        smitten = tracker.startTracking(TrackableDataType.BOOLEAN, false);
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
    }

    public boolean isMinion() {
        return getMasterReference().isSet();
    }

    public boolean isDiscorded() {
        return discorded.get();
    }

    public boolean isSmitten() {
        return smitten.get();
    }

    public void setSmitten(boolean smitten) {
        smittenTicks = smitten ? 20 : 0;
        this.smitten.set(smitten);
    }

    public void setDiscorded(boolean discorded) {
        this.discorded.set(discorded);
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        return getMasterReference().getOrEmpty(asWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    public Optional<GoalSelector> getGoals() {
        return goals;
    }

    public void initAi(GoalSelector goals, GoalSelector targets) {
        this.goals = Optional.of(goals);

        if (entity instanceof MagicImmune) {
            return;
        }

        DynamicTargetGoal targetter = new DynamicTargetGoal((MobEntity)entity);
        targets.add(1, targetter);
        if (!InteractionManager.getInstance().getSyncedConfig().wantItNeedItExcludeList().contains(EntityType.getId(entity.getType()).toString())) {
            goals.add(1, new WantItTakeItGoal(this, targetter));
        }
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            goals.add(3, new BreakHeartGoal((MobEntity)entity, targetter));
            if (entity instanceof AbstractSkeletonEntity) {
                targets.add(1, new PrioritizedActiveTargetGoal<>((MobEntity)entity, PlayerEntity.class, TargettingUtil.FLYING_PREFERRED, true));
            }
        }
        if (entity instanceof PigEntity pig) {
            eatMuffinGoal = new EatMuffinGoal(pig, targetter);
            goals.add(3, eatMuffinGoal);
        }
        if (entity instanceof TameableEntity tameable) {
            goals.add(3, new FleeExplosionGoal(tameable, 6, 1, 1.2));
        }

        if (entity instanceof CreeperEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.5, 1.9, AmuletSelectors.ALICORN_AMULET));
        }
        if (entity instanceof PassiveEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.1, 1.7, AmuletSelectors.ALICORN_AMULET_AFTER_1_DAYS));
        }

        final BooleanSupplier isMinion = () -> getMasterReference().isSet();
        final BooleanSupplier isNotMinion = () -> !isMinion.getAsBoolean();

        PredicatedGoal.applyToAll(targets, isNotMinion);
        targets.add(2, new PredicatedGoal(new ActiveEnemyGoal<>(PlayerEntity.class), isMinion));
        targets.add(2, new PredicatedGoal(new ActiveEnemyGoal<>(HostileEntity.class), isMinion));
        targets.add(2, new PredicatedGoal(new ActiveEnemyGoal<>(SlimeEntity.class), isMinion));

        if (entity instanceof MobEntity mob) {
            final BooleanSupplier isDiscorded = () -> isNotMinion.getAsBoolean() && isDiscorded();
            PredicatedGoal.applyToAll(goals, () -> !isDiscorded.getAsBoolean());

            goals.add(1, new PredicatedGoal(new SwimGoal(mob), isDiscorded));
            if (mob instanceof PathAwareEntity pae) {
                goals.add(5, new PredicatedGoal(new WanderAroundFarGoal(pae, 0.8), isDiscorded));
            }
            goals.add(6, new PredicatedGoal(new LookAtEntityGoal(mob, PlayerEntity.class, 8.0f), isDiscorded));
            goals.add(6, new PredicatedGoal(new LookAroundGoal(mob), isDiscorded));
        }
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        builder.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        builder.add(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
    }

    @Override
    public boolean beforeUpdate() {
        if (isDiscorded()) {
            spawnParticles(ParticleTypes.ELECTRIC_SPARK, 1);
        }

        if (!isClient() && smittenTicks > 0) {
            if (--smittenTicks <= 0) {
                setSmitten(false);
            }
        }

        return super.beforeUpdate();
    }

    private void updateConsumption() {
        if (isClient()) {
            eatTimer = eating.get();
        } else if (eatMuffinGoal != null) {
            eatTimer = eatMuffinGoal.getTimer();
            eating.set(eatTimer);
        }
    }

    public float getNeckAngle(float delta) {
        if (eatTimer <= 0) {
            return 0;
        }
        if (eatTimer >= 4 && eatTimer <= 36) {
            return 1;
        }
        if (eatTimer < 4) {
            return (eatTimer - delta) / 4F;
        }
        return -(eatTimer - 40 - delta) / 4F;
    }

    public float getHeadAngle(float delta) {
        if (eatTimer > 4 && eatTimer <= 36) {
            float f = (eatTimer - 4 - delta) / 32F;
            return 0.62831855f + 0.21991149f * MathHelper.sin(f * 28.7F);
        }
        if (eatTimer > 0) {
            return 0.62831855f;
        }
        return entity.getPitch() * ((float)Math.PI / 180);
    }

    @Override
    public Race getSpecies() {
        return Race.HUMAN;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public void setSpecies(Race race) {
    }

    @Override
    public LevelStore getLevel() {
        return Levelled.ZERO;
    }

    @Override
    public LevelStore getCorruption() {
        return Levelled.ZERO;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        getMaster().damage(asEntity().getDamageSources().magic(), (int)amount/2);
        return getMaster().getHealth() > 0;
    }

    @Override
    public Affinity getAffinity() {
        if (getMaster() instanceof Affine) {
            Affinity affinity = ((Affine)getMaster()).getAffinity();
            if (isDiscorded()) {
                return affinity == Affinity.BAD ? Affinity.GOOD : affinity == Affinity.GOOD ? Affinity.BAD : affinity;
            }
            return affinity;
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.put("master", getMasterReference().toNBT(lookup));
        physics.toNBT(compound, lookup);
        compound.putBoolean("discorded", isDiscorded());
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        if (compound.contains("master", NbtElement.COMPOUND_TYPE)) {
            getMasterReference().fromNBT(compound.getCompound("master"), lookup);
        }
        physics.fromNBT(compound, lookup);
        setDiscorded(compound.getBoolean("discorded"));
    }

    private class ActiveEnemyGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public ActiveEnemyGoal(Class<T> targetClass) {
            super((MobEntity)entity, targetClass, true, Creature.this.targetPredicate);
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = this.mob.getTarget();
            return target != null  && targetPredicate.test(mob, target) && super.shouldContinue();
        }
    }
}
