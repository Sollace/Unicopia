package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.network.datasync.EffectSync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CastSpellEntity extends LightEmittingEntity implements Caster<CastSpellEntity>, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {
    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this, GRAVITY);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private LevelStore level = Levelled.EMPTY;
    private LevelStore corruption = Levelled.EMPTY;

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(EFFECT, new NbtCompound());
    }

    @Override
    public int getLightLevel() {
        return 11;
    }

    @Override
    public Text getName() {
        Entity master = getMaster();
        if (master != null) {
            return Text.translatable("entity.unicopia.cast_spell.by", master.getName());
        }
        return super.getName();
    }

    @Override
    public void tick() {
        super.tick();

        if (isRemoved()) {
            return;
        }

        if (!effectDelegate.tick(Situation.GROUND_ENTITY)) {
            discard();
        }
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    @Override
    public CastSpellEntity asEntity() {
        return this;
    }

    public void setCaster(Caster<?> caster) {
        this.level = Levelled.copyOf(caster.getLevel());
        this.corruption = Levelled.copyOf(caster.getCorruption());
        setMaster(caster);
    }

    @Override
    public LevelStore getLevel() {
        return level;
    }

    @Override
    public LevelStore getCorruption() {
        return corruption;
    }

    @Override
    public Affinity getAffinity() {
        return getSpellSlot().get(true).map(Spell::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public SpellContainer getSpellSlot() {
        return effectDelegate;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        if (getMaster() == null) {
            return true;
        }

        return Caster.of(getMaster())
                .filter(c -> c.subtractEnergyCost(amount))
                .isPresent();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
        tag.put("owner", owner.toNBT());
        tag.put("level", level.toNbt());
        tag.put("corruption", corruption.toNbt());
        getSpellSlot().get(true).ifPresent(effect -> {
            tag.put("effect", Spell.writeNbt(effect));
        });
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
        if (tag.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(tag.getCompound("effect")));
        }
        level = Levelled.fromNbt(tag.getCompound("level"));
        corruption = Levelled.fromNbt(tag.getCompound("corruption"));
    }
}
