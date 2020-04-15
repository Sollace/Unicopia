package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.IAffine;
import com.minelittlepony.unicopia.magic.IAttachedEffect;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.EffectSync;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;

public class LivingEntityCapabilities implements RaceContainer<LivingEntity>, ICaster<LivingEntity> {

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    public static void boostrap() {}

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final LivingEntity entity;

    public LivingEntityCapabilities(LivingEntity entity) {
        this.entity = entity;

        entity.getDataTracker().startTracking(EFFECT, new CompoundTag());
    }

    @Override
    public Race getSpecies() {
        return Race.HUMAN;
    }

    @Override
    public void setSpecies(Race race) {
    }

    @Override
    public void setEffect(IMagicEffect effect) {
        effectDelegate.set(effect);
    }

    @Override
    public <T extends IMagicEffect> T getEffect(Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Override
    public void onUpdate() {
        if (hasEffect()) {
            IAttachedEffect effect = getEffect(IAttachedEffect.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isClient()) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setEffect(null);
                }
            }
        }
    }

    @Override
    public void onDimensionalTravel(int destinationDimension) {

    }

    @Override
    public void setOwner(LivingEntity owner) {

    }

    @Override
    public LivingEntity getOwner() {
        return entity;
    }

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

    @Override
    public Affinity getAffinity() {
        if (getOwner() instanceof IAffine) {
            return ((IAffine)getOwner()).getAffinity();
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        IMagicEffect effect = getEffect();

        if (effect != null) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(effect));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.containsKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
    }
}
