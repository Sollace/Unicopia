package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;

public class Creature extends Living<LivingEntity> implements WeaklyOwned.Mutable<LivingEntity> {
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    private static final TrackedData<NbtCompound> MASTER = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Float> GRAVITY = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> EATING = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DISCORDED = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private Optional<GoalSelector> goals = Optional.empty();
    private Optional<GoalSelector> targets = Optional.empty();

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    private boolean discordedChanged = true;

    private final Predicate<LivingEntity> targetPredicate = TargetSelecter.<LivingEntity>notOwnerOrFriend(() -> getOriginatingCaster().getAffinity(), this).and(e -> {
        return Equine.of(e)
                .filter(eq -> eq instanceof Creature)
                .filter(eq -> isDiscorded() != ((Creature)eq).hasCommonOwner(this))
                .isEmpty();
    });

    public Creature(LivingEntity entity) {
        super(entity, EFFECT);
        physics = new EntityPhysics<>(entity, GRAVITY);
        entity.getDataTracker().startTracking(MASTER, owner.toNBT());
        entity.getDataTracker().startTracking(EATING, 0);
        entity.getDataTracker().startTracking(DISCORDED, false);

        addTicker(physics);
        addTicker(this::updateConsumption);
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
        entity.getDataTracker().set(MASTER, this.owner.toNBT());
        if (owner != null) {
            targets.ifPresent(this::initMinionAi);
        }
    }

    public boolean isMinion() {
        return getMasterReference().isSet();
    }

    public boolean isDiscorded() {
        return entity.getDataTracker().get(DISCORDED);
    }

    public void setDiscorded(boolean discorded) {
        entity.getDataTracker().set(DISCORDED, discorded);
        discordedChanged = true;
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        return getMasterReference().getOrEmpty(asWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        if (entity.getDataTracker().containsKey(MASTER)) {
            NbtCompound data = entity.getDataTracker().get(MASTER);
            owner.fromNBT(data);
        }
        return owner;
    }

    public Optional<GoalSelector> getTargets() {
        return targets;
    }

    public Optional<GoalSelector> getGoals() {
        return goals;
    }

    public void initAi(GoalSelector goals, GoalSelector targets) {
        this.goals = Optional.of(goals);
        this.targets = Optional.of(targets);

        DynamicTargetGoal targetter = new DynamicTargetGoal((MobEntity)entity);
        targets.add(1, targetter);
        if (!Unicopia.getConfig().wantItNeedItEntityExcludelist.get().contains(EntityType.getId(entity.getType()).toString())) {
            goals.add(1, new WantItTakeItGoal((MobEntity)entity, targetter));
        }
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            goals.add(3, new BreakHeartGoal((MobEntity)entity, targetter));
        }
        if (entity instanceof PigEntity pig) {
            eatMuffinGoal = new EatMuffinGoal(pig, targetter);
            goals.add(3, eatMuffinGoal);
        }

        if (getMasterReference().isSet()) {
            initMinionAi(targets);
        }

        if (isDiscorded()) {
            initDiscordedAi();
        }

        if (entity instanceof CreeperEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.5, 1.9, AmuletSelectors.ALICORN_AMULET));
        }
        if (entity instanceof PassiveEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.1, 1.7, AmuletSelectors.ALICORN_AMULET_AFTER_1_DAYS));
        }
    }



    private void initMinionAi(GoalSelector targets) {
        clearGoals(targets);
        targets.add(2, new ActiveEnemyGoal<>(PlayerEntity.class));
        targets.add(2, new ActiveEnemyGoal<>(HostileEntity.class));
        targets.add(2, new ActiveEnemyGoal<>(SlimeEntity.class));
    }

    private void initDiscordedAi() {
        if (getMasterReference().isSet()) {
            return;
        }
        targets.ifPresent(this::clearGoals);
        // the brain drain
        entity.getBrain().clear();
        if (entity instanceof MobEntity mob) {
            mob.setTarget(null);
            goals.ifPresent(goalSelector -> {
                clearGoals(goalSelector);
                goalSelector.add(1, new SwimGoal(mob));
                if (mob instanceof PathAwareEntity pae) {
                    goalSelector.add(5, new WanderAroundFarGoal(pae, 0.8));
                }
                goalSelector.add(6, new LookAtEntityGoal(mob, PlayerEntity.class, 8.0f));
                goalSelector.add(6, new LookAroundGoal(mob));
            });
        } else {
            goals.ifPresent(this::clearGoals);
        }
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        builder.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        builder.add(UEntityAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    @Override
    public boolean beforeUpdate() {
        if (isDiscorded() && discordedChanged) {
            discordedChanged = false;
            initDiscordedAi();
        }



        return false;
    }

    private void clearGoals(GoalSelector t) {
        t.clear(g -> true);
    }

    private void updateConsumption() {
        if (isClient()) {
            eatTimer = entity.getDataTracker().get(EATING);
        } else if (eatMuffinGoal != null) {
            eatTimer = eatMuffinGoal.getTimer();
            entity.getDataTracker().set(EATING, eatTimer);
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
        return Levelled.EMPTY;
    }

    @Override
    public LevelStore getCorruption() {
        return Levelled.EMPTY;
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
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        getSpellSlot().get(true).ifPresent(effect -> {
            compound.put("effect", Spell.writeNbt(effect));
        });
        compound.put("master", getMasterReference().toNBT());
        physics.toNBT(compound);
        compound.putBoolean("discorded", isDiscorded());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        if (compound.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(compound.getCompound("effect")));
        }
        if (compound.contains("master", NbtElement.COMPOUND_TYPE)) {
            owner.fromNBT(compound.getCompound("master"));
            if (entity.getDataTracker().containsKey(MASTER)) {
                entity.getDataTracker().set(MASTER, owner.toNBT());
            }
            if (owner.isSet()) {
                targets.ifPresent(this::initMinionAi);
            }
        }
        physics.fromNBT(compound);
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
