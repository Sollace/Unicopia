package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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

    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private Optional<GoalSelector> goals = Optional.empty();
    private Optional<GoalSelector> targets = Optional.empty();

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    public Creature(LivingEntity entity) {
        super(entity, EFFECT);
        physics = new EntityPhysics<>(entity, GRAVITY);
        entity.getDataTracker().startTracking(MASTER, owner.toNBT());
        entity.getDataTracker().startTracking(EATING, 0);

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
        return owner.getId().isPresent();
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        NbtCompound data = entity.getDataTracker().get(MASTER);
        owner.fromNBT(data);
        return owner.getOrEmpty(asWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
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
        goals.add(1, new WantItTakeItGoal((MobEntity)entity, targetter));
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            goals.add(3, new BreakHeartGoal((MobEntity)entity, targetter));
        }
        if (entity instanceof PigEntity pig) {
            eatMuffinGoal = new EatMuffinGoal(pig, targetter);
            goals.add(3, eatMuffinGoal);
        }

        if (owner.isPresent(asWorld())) {
            initMinionAi(targets);
        }

        if (entity instanceof CreeperEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.5, 1.9, AmuletSelectors.ALICORN_AMULET));
        }
        if (entity instanceof PassiveEntity mob) {
            goals.add(1, new FleeEntityGoal<>(mob, LivingEntity.class, 10, 1.1, 1.7, AmuletSelectors.ALICORN_AMULET_AFTER_1_DAYS));
        }
    }

    private void initMinionAi(GoalSelector targets) {
        Predicate<LivingEntity> filter = TargetSelecter.<LivingEntity>notOwnerOrFriend(this, this).and(e -> {
            return Equine.of(e)
                    .filter(eq -> eq instanceof Creature)
                    .filter(eq -> ((Creature)eq).hasCommonOwner(this))
                    .isEmpty();
        });

        targets.clear();
        targets.add(2, new ActiveTargetGoal<>((MobEntity)entity, PlayerEntity.class, true, filter));
        targets.add(2, new ActiveTargetGoal<>((MobEntity)entity, HostileEntity.class, true, filter));
        targets.add(2, new ActiveTargetGoal<>((MobEntity)entity, SlimeEntity.class, true, filter));
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        builder.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        builder.add(UEntityAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    @Override
    public boolean beforeUpdate() {
        return false;
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
        getMaster().damage(DamageSource.MAGIC, (int)amount/2);
        return getMaster().getHealth() > 0;
    }

    @Override
    public Affinity getAffinity() {
        if (getMaster() instanceof Affine) {
            return ((Affine)getMaster()).getAffinity();
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        getSpellSlot().get(true).ifPresent(effect -> {
            compound.put("effect", Spell.writeNbt(effect));
        });
        compound.put("master", owner.toNBT());
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        if (compound.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(compound.getCompound("effect")));
        }
        if (compound.contains("master", NbtElement.COMPOUND_TYPE)) {
            owner.fromNBT(compound.getCompound("master"));
            if (owner.isPresent(asWorld())) {
                targets.ifPresent(this::initMinionAi);
            }
        }
        physics.fromNBT(compound);
    }
}
