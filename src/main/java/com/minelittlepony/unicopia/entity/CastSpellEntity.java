package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellContainer.Operation;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.datasync.EffectSync;

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
import net.minecraft.world.World;

public class CastSpellEntity extends LightEmittingEntity implements Caster<LivingEntity>, WeaklyOwned<LivingEntity> {
    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(CastSpellEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    private static final LevelStore LEVELS = Levelled.fixed(0);

    private final EntityPhysics<CastSpellEntity> physics = new EntityPhysics<>(this, GRAVITY);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    public CastSpellEntity(EntityType<?> type, World world) {
        super(type, world);
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

        if (!getSpellSlot().forEach(spell -> Operation.ofBoolean(spell.tick(this, Situation.GROUND_ENTITY)), true)) {
            discard();
        }
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    @Override
    public Entity getEntity() {
        return this;
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
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }

    @Override
    public World getReferenceWorld() {
        return WeaklyOwned.super.getReferenceWorld();
    }
}
