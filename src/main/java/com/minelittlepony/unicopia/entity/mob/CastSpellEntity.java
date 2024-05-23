package com.minelittlepony.unicopia.entity.mob;

import java.util.UUID;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.server.world.Ether;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CastSpellEntity extends LightEmittingEntity implements Caster<CastSpellEntity>, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {
    private static final TrackedData<Integer> LEVEL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_LEVEL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CORRUPTION = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_CORRUPTION = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> DEAD = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

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

    private UUID controllingEntityUuid;
    private UUID controllingSpellUuid;

    private int prevAge;

    private int prevDeathTicks;
    private int deathTicks;

    public CastSpellEntity(World world, Caster<?> caster, PlacementControlSpell control) {
        this(UEntities.CAST_SPELL, world);
        this.controllingEntityUuid = caster.asEntity().getUuid();
        this.controllingSpellUuid = control.getUuid();
        setCaster(caster);
        Spell spell = Spell.copy(control.getDelegate());
        spells.getSlots().put(spell);
    }

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
        dataTracker.startTracking(DEAD, false);
    }

    @Override
    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.updatePositionAndAngles(x, y, z, yaw, pitch);
        spells.getSlots().stream(SpellPredicate.IS_ORIENTED).forEach(spell -> spell.setOrientation(this, pitch, yaw));
    }

    private boolean checkConnection() {
        return Ether.get(getWorld()).get(SpellType.PLACE_CONTROL_SPELL, controllingEntityUuid, controllingSpellUuid) != null;
    }

    public float getAge(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevAge, age);
    }

    public float getScale(float tickDelta) {
        float add = MathHelper.clamp(getAge(tickDelta) / 25F, 0, 1);
        float subtract = MathHelper.clamp(MathHelper.lerp(tickDelta, prevDeathTicks, deathTicks) / 20F, 0, 1);
        return MathHelper.clamp(add - subtract, 0, 1);
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
    public void baseTick() {
        prevAge = age;
        age++;

        super.baseTick();

        if (!isClient()) {
            if (!checkConnection()) {
                kill();
            }

            spells.getSlots().get().ifPresent(spell -> {
                Ether.get(getWorld()).getOrCreate(spell, this);
            });
        } else if (isDead()) {
            spells.getSlots().clear();
        }

        prevDeathTicks = deathTicks;

        if (!spells.tick(Situation.GROUND) && deathTicks++ > 40) {
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void kill() {
        setDead(true);
    }

    public boolean isDead() {
        return dataTracker.get(DEAD);
    }

    public void setDead(boolean dead) {
        dataTracker.set(DEAD, dead);
        if (dead) {
            spells.getSlots().clear();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(getScale(1));
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
        tag.put("level", level.toNbt());
        tag.put("corruption", corruption.toNbt());

        if (controllingEntityUuid != null) {
            tag.putUuid("owningEntity", controllingEntityUuid);
        }
        if (controllingSpellUuid != null) {
            tag.putUuid("owningSpell", controllingSpellUuid);
        }

        spells.getSlots().toNBT(tag);
        tag.putInt("age", age);
        tag.putInt("prevAge", prevAge);
        tag.putBoolean("dead", isDead());
        tag.put("owner", owner.toNBT());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        var level = Levelled.fromNbt(tag.getCompound("level"));
        dataTracker.set(MAX_LEVEL, level.getMax());
        dataTracker.set(LEVEL, level.get());
        var corruption = Levelled.fromNbt(tag.getCompound("corruption"));
        dataTracker.set(MAX_CORRUPTION, corruption.getMax());
        dataTracker.set(CORRUPTION, corruption.get());

        controllingEntityUuid = tag.containsUuid("owningEntity") ? tag.getUuid("owningEntity") : null;
        controllingSpellUuid = tag.containsUuid("owningSpell") ? tag.getUuid("owningSpell") : null;

        spells.getSlots().fromNBT(tag);
        age = tag.getInt("age");
        prevAge = tag.getInt("prevAge");
        setDead(tag.getBoolean("dead"));

        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
    }
}
