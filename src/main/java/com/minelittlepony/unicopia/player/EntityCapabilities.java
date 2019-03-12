package com.minelittlepony.unicopia.player;

import java.util.Map;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.spell.IAligned;
import com.minelittlepony.unicopia.spell.IAttachedEffect;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellAffinity;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class EntityCapabilities implements IRaceContainer<EntityLivingBase>, ICaster<EntityLivingBase> {

    private static final Map<Class<? extends EntityLivingBase>, DataParameter<NBTTagCompound>> EFFECT_KEYS = Maps.newHashMap();

    private final DataParameter<NBTTagCompound> EFFECT;

    private final EffectSync<EntityLivingBase> effectDelegate;

    private EntityLivingBase entity;

    EntityCapabilities(EntityLivingBase entity) {
        setOwner(entity);

        EFFECT = EFFECT_KEYS.computeIfAbsent(entity.getClass(), c -> {
            return EntityDataManager.createKey(c, DataSerializers.COMPOUND_TAG);
        });
        effectDelegate = new EffectSync<>(this, EFFECT);

        entity.getDataManager().register(EFFECT, new NBTTagCompound());
    }

    @Override
    public Race getPlayerSpecies() {
        return Race.HUMAN;
    }

    @Override
    public void setPlayerSpecies(Race race) {
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
    public void beforeUpdate() {

    }

    @Override
    public void onUpdate() {
        if (hasEffect()) {
            IAttachedEffect effect = getEffect(IAttachedEffect.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isRemote) {
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
    public void setOwner(EntityLivingBase owner) {
        entity = owner;
    }

    @Override
    public EntityLivingBase getOwner() {
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
    public SpellAffinity getAffinity() {
        if (getOwner() instanceof IAligned) {
            return ((IAligned)getOwner()).getAffinity();
        }
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        IMagicEffect effect = getEffect();

        if (effect != null) {
            compound.setTag("effect", SpellRegistry.instance().serializeEffectToNBT(effect));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompoundTag("effect")));
        }
    }
}
