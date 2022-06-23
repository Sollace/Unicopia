package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Creature extends Living<LivingEntity> implements WeaklyOwned<LivingEntity> {
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    private static final TrackedData<NbtCompound> MASTER = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Float> GRAVITY = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> EATING = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final LevelStore LEVELS = Levelled.fixed(0);

    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> master = new EntityReference<>();

    @Nullable
    private GoalSelector goals;
    @Nullable
    private GoalSelector targets;

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    public Creature(LivingEntity entity) {
        super(entity, EFFECT);
        physics = new EntityPhysics<>(entity, GRAVITY);
        entity.getDataTracker().startTracking(MASTER, master.toNBT());
        entity.getDataTracker().startTracking(EATING, 0);
    }

    @Override
    public void setMaster(LivingEntity owner) {
        master.set(owner);
        entity.getDataTracker().set(MASTER, master.toNBT());
        if (targets != null && owner != null) {
            initMinionAi();
        }
    }

    public boolean isMinion() {
        return master.getId().isPresent();
    }

    @Override
    public World getReferenceWorld() {
        return super.getReferenceWorld();
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        NbtCompound data = entity.getDataTracker().get(MASTER);
        master.fromNBT(data);
        return master.getOrEmpty(getReferenceWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return master;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    public Optional<GoalSelector> getTargets() {
        return Optional.ofNullable(targets);
    }

    public Optional<GoalSelector> getGoals() {
        return Optional.ofNullable(goals);
    }

    public void initAi(GoalSelector goals, GoalSelector targets) {
        this.goals = goals;
        this.targets = targets;

        DynamicTargetGoal targetter = new DynamicTargetGoal((MobEntity)entity);
        targets.add(1, targetter);
        goals.add(1, new WantItTakeItGoal((MobEntity)entity, targetter));
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            goals.add(3, new BreakHeartGoal((MobEntity)entity, targetter));
        }
        if (entity instanceof PigEntity) {
            eatMuffinGoal = new EatMuffinGoal((MobEntity)entity, targetter);
            goals.add(3, eatMuffinGoal);
        }

        if (master.isPresent(getReferenceWorld())) {
            initMinionAi();
        }
    }

    private void initMinionAi() {
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
        builder.add(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    @Override
    public void tick() {
        super.tick();
        physics.tick();

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
        return LEVELS;
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
        compound.put("master", master.toNBT());
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        if (compound.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(compound.getCompound("effect")));
        }
        if (compound.contains("master", NbtElement.COMPOUND_TYPE)) {
            master.fromNBT(compound.getCompound("master"));
            if (master.isPresent(getReferenceWorld()) && targets != null) {
                initMinionAi();
            }
        }
        physics.fromNBT(compound);
    }
}
