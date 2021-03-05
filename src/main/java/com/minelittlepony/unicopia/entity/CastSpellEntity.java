package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractPlacedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellPredicate;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

public class CastSpellEntity extends Entity implements Caster<LivingEntity> {

    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final TrackedData<Optional<UUID>> SPELL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private static final SpellPredicate<AbstractPlacedSpell> SPELL_TYPE = s -> s instanceof AbstractPlacedSpell;

    private static final LevelStore LEVELS = Levelled.fixed(0);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this, GRAVITY);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(SPELL, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (removed) {
            return;
        }

        LivingEntity master = getMaster();

        if (master == null || master.removed) {
            remove();
            return;
        }

        if (!Caster.of(master).filter(c -> {
            UUID spellId = dataTracker.get(SPELL).orElse(null);

            if (!c.getSpellSlot().get(SPELL_TYPE, true).filter(s -> s.getUuid().equals(spellId) && s.onGroundTick(this)).isPresent()) {
                c.setSpell(null);

                return false;
            }

            return true;
        }).isPresent()) {
            remove();
        }
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
    }

    @Override
    public void setSpell(Spell spell) {
        getDataTracker().set(SPELL, Optional.ofNullable(spell).map(Spell::getUuid));
    }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public LivingEntity getMaster() {
        return owner.get(world);
    }

    @Override
    public LevelStore getLevel() {
        return Caster.of(getMaster()).map(Caster::getLevel).orElse(LEVELS);
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
        return Caster.of(getMaster()).map(Caster::getSpellSlot).orElse(SpellContainer.EMPTY);
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        tag.put("owner", owner.toNBT());
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
    }


    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }

}
