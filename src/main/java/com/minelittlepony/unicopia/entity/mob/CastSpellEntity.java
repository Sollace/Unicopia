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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CastSpellEntity extends LightEmittingEntity implements Caster<CastSpellEntity>, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this);

    private final SpellInventory spells = SpellSlots.ofSingle(this);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private LevelStore level = Levelled.EMPTY;
    private LevelStore corruption = Levelled.EMPTY;

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    @Override
    protected void initDataTracker() {
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
        level = Levelled.fromNbt(tag.getCompound("level"));
        corruption = Levelled.fromNbt(tag.getCompound("corruption"));
    }
}
