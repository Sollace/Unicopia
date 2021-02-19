package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;

public class Creature extends Living<LivingEntity> {

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private static final LevelStore LEVELS = Levelled.fixed(0);

    public static void boostrap() {}

    private final Physics physics = new EntityPhysics<>(this);

    public Creature(LivingEntity entity) {
        super(entity, EFFECT);
    }

    public void initAi(GoalSelector goals, GoalSelector targets) {
        DynamicTargetGoal targetter = new DynamicTargetGoal((MobEntity)entity);
        targets.add(1, targetter);
        goals.add(1, new WantItTakeItGoal((MobEntity)entity, targetter));
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            goals.add(3, new BreakHeartGoal((MobEntity)entity, targetter));
        }
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        builder.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        builder.add(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);
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
    public Affinity getAffinity() {
        if (getMaster() instanceof Affine) {
            return ((Affine)getMaster()).getAffinity();
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        Spell effect = getSpell(true);

        if (effect != null) {
            compound.put("effect", SpellRegistry.toNBT(effect));
        }
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        if (compound.contains("effect")) {
            setSpell(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
        physics.fromNBT(compound);
    }
}
