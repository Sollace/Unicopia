package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.network.track.Trackable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CastSpellEntity extends LightEmittingEntity implements Caster<CastSpellEntity>, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {
    private static final TrackedData<Integer> LEVEL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_LEVEL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CORRUPTION = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_CORRUPTION = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this);

    private final SpellInventory spells = SpellSlots.ofSingle(this);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private final LevelStore level = Levelled.of(
            () -> dataTracker.get(LEVEL),
            l -> dataTracker.set(LEVEL, l),
            () -> dataTracker.get(MAX_LEVEL)
    );
    private final LevelStore corruption = Levelled.of(
            () -> dataTracker.get(CORRUPTION),
            l -> dataTracker.set(CORRUPTION, l),
            () -> dataTracker.get(MAX_CORRUPTION)
    );

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
        Trackable.of(this).getDataTrackers().getPrimaryTracker().startTracking(owner);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(LEVEL, 0);
        dataTracker.startTracking(CORRUPTION, 0);
        dataTracker.startTracking(MAX_LEVEL, 1);
        dataTracker.startTracking(MAX_CORRUPTION, 1);
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

        if (!spells.tick(Situation.GROUND_ENTITY)) {
            discard();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(getSpellSlot().get(SpellType.IS_PLACED).map(spell -> spell.getScale(1)).orElse(1F));
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
        dataTracker.set(LEVEL, caster.getLevel().get());
        dataTracker.set(MAX_LEVEL, caster.getLevel().getMax());
        dataTracker.set(CORRUPTION, caster.getCorruption().get());
        dataTracker.set(MAX_CORRUPTION, caster.getCorruption().getMax());
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
        return getSpellSlot().get().map(Spell::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
    }

    @Override
    public boolean canHit() {
        return false;
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
        spells.getSlots().toNBT(tag);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
        spells.getSlots().fromNBT(tag);
        var level = Levelled.fromNbt(tag.getCompound("level"));
        dataTracker.set(MAX_LEVEL, level.getMax());
        dataTracker.set(LEVEL, level.get());
        var corruption = Levelled.fromNbt(tag.getCompound("corruption"));
        dataTracker.set(MAX_CORRUPTION, corruption.getMax());
        dataTracker.set(CORRUPTION, corruption.get());
    }
}
