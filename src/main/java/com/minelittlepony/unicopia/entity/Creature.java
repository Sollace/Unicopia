package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.FleeExplosionGoal;
import com.minelittlepony.unicopia.entity.ai.PrioritizedActiveTargetGoal;
import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.serialization.PacketCodec;

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
import net.minecraft.util.math.MathHelper;

public class Creature extends Living<LivingEntity> implements WeaklyOwned.Mutable<LivingEntity> {
    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private Optional<GoalSelector> goals = Optional.empty();
    private Optional<GoalSelector> targets = Optional.empty();

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    private boolean discordedChanged = true;
    private int smittenTicks;

    private final Predicate<LivingEntity> targetPredicate = TargetSelecter.<LivingEntity>validTarget(() -> getOriginatingCaster().getAffinity(), this).and(e -> {
        return Equine.of(e)
                .filter(eq -> eq instanceof Creature)
                .filter(eq -> isDiscorded() != ((Creature)eq).hasCommonOwner(this))
                .isEmpty();
    });

    protected final DataTracker.Entry<NbtCompound> master;
    protected final DataTracker.Entry<Integer> eating;
    protected final DataTracker.Entry<Boolean> discorded;
    protected final DataTracker.Entry<Boolean> smitten;

    public Creature(LivingEntity entity) {
        super(entity);
        physics = new EntityPhysics<>(entity, tracker);
        addTicker(physics);
        addTicker(this::updateConsumption);

        master = tracker.startTracking(TrackableDataType.of(PacketCodec.NBT), owner.toNBT());
        eating = tracker.startTracking(TrackableDataType.of(PacketCodec.INT), 0);
        discorded = tracker.startTracking(TrackableDataType.of(PacketCodec.BOOLEAN), false);
        smitten = tracker.startTracking(TrackableDataType.of(PacketCodec.BOOLEAN), false);
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
        tracker.set(master, this.owner.toNBT());
        if (owner != null) {
            targets.ifPresent(this::initMinionAi);
        }
    }

    public boolean isMinion() {
        return getMasterReference().isSet();
    }

    public boolean isDiscorded() {
        return tracker.get(this.discorded);
    }

    public boolean isSmitten() {
        return tracker.get(this.smitten);
    }

    public void setSmitten(boolean smitten) {
        smittenTicks = smitten ? 20 : 0;
        tracker.set(this.smitten, smitten);
    }

    public void setDiscorded(boolean discorded) {
        tracker.set(this.discorded, discorded);
        discordedChanged = true;
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        return getMasterReference().getOrEmpty(asWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        if (master != null) {
            owner.fromNBT(tracker.get(master));
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

        initMinionAi(targets);
        initDiscordedAi();

        if (entity instanceof CreeperEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.5, 1.9, AmuletSelectors.ALICORN_AMULET));
        }
        if (entity instanceof PassiveEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.1, 1.7, AmuletSelectors.ALICORN_AMULET_AFTER_1_DAYS));
        }
    }

    private void initMinionAi(GoalSelector targets) {
        if (entity instanceof MagicImmune || !getMasterReference().isSet()) {
            return;
        }

        clearGoals(targets);
        targets.add(2, new ActiveEnemyGoal<>(PlayerEntity.class));
        targets.add(2, new ActiveEnemyGoal<>(HostileEntity.class));
        targets.add(2, new ActiveEnemyGoal<>(SlimeEntity.class));
    }

    private void initDiscordedAi() {
        if (entity instanceof MagicImmune || getMasterReference().isSet() || !isDiscorded()) {
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
        builder.add(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
    }

    @Override
    public boolean beforeUpdate() {
        if (discordedChanged) {
            discordedChanged = false;
            initDiscordedAi();
        }

        if (!isClient() && smittenTicks > 0) {
            if (--smittenTicks <= 0) {
                setSmitten(false);
            }
        }

        return super.beforeUpdate();
    }

    private void clearGoals(GoalSelector t) {
        t.clear(g -> true);
    }

    private void updateConsumption() {
        if (isClient()) {
            eatTimer = tracker.get(eating);
        } else if (eatMuffinGoal != null) {
            eatTimer = eatMuffinGoal.getTimer();
            tracker.set(eating, eatTimer);
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
        getSpellSlot().get().ifPresent(effect -> {
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
            if (master != null) {
                tracker.set(master, owner.toNBT());
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
