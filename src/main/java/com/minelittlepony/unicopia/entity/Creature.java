package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;

public class Creature extends Living<LivingEntity> {

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private static final LevelStore LEVELS = Levelled.fixed(0);

    public static void boostrap() {}

    private final Physics physics = new EntityPhysics<>(this);

    public Creature(LivingEntity entity) {
        super(entity, EFFECT);
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
        Spell effect = getSpell(true);

        if (effect != null) {
            compound.put("effect", SpellRegistry.toNBT(effect));
        }
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains("effect")) {
            setSpell(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
        physics.fromNBT(compound);
    }
}
