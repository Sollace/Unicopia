package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CastSpellEntity extends Entity implements Caster<LivingEntity> {

    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final TrackedData<Optional<UUID>> SPELL = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private static final LevelStore LEVELS = Levelled.fixed(0);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this, GRAVITY);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private BlockPos lastPos;

    private int orphanedTicks;

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(SPELL, Optional.empty());
    }

    @Override
    public Text getName() {
        Entity master = getMaster();
        if (master != null) {
            return new TranslatableText("entity.unicopia.cast_spell.by", master.getName());
        }
        return super.getName();
    }

    @Override
    public void tick() {
        super.tick();

        if (isRemoved()) {
            return;
        }

        if (world.isClient) {
            BlockPos currentPos = getBlockPos();

            world.getLightingProvider().addLightSource(currentPos, 11);

            if (lastPos != null && !currentPos.equals(lastPos)) {
                world.getLightingProvider().checkBlock(lastPos);
            }

            lastPos = currentPos;
        }
        LivingEntity master = getMaster();

        if (master == null || master.isRemoved()) {
            if (orphanedTicks-- > 0) {
                return;
            }
            remove(RemovalReason.DISCARDED);
            return;
        }

        orphanedTicks = 0;

        if (!Caster.of(master).filter(c -> {
            UUID spellId = dataTracker.get(SPELL).orElse(null);

            if (!c.getSpellSlot().get(true).filter(s -> s.getUuid().equals(spellId) && s.tick(this, Situation.GROUND_ENTITY)).isPresent()) {
                c.setSpell(null);

                return false;
            }

            return true;
        }).isPresent()) {
            remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (world.isClient) {
            world.getLightingProvider().checkBlock(getBlockPos());
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
    public boolean subtractEnergyCost(double amount) {
        return Caster.of(getMaster()).filter(c -> c.subtractEnergyCost(amount)).isPresent();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
        tag.put("owner", owner.toNBT());
        dataTracker.get(SPELL).ifPresent(spellId -> {
            tag.putUuid("spellId", spellId);
        });

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
        if (tag.contains("spellId")) {
            dataTracker.set(SPELL, Optional.ofNullable(tag.getUuid("spellId")));
        }
        orphanedTicks = 60;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }

}
