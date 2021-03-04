package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractPlacedSpell extends AbstractSpell implements Attached {

    @Nullable
    private Identifier dimension;

    private final ParticleHandle particlEffect = new ParticleHandle();

    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    protected AbstractPlacedSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {

        if (!source.isClient()) {

            if (dimension == null) {
                dimension = source.getWorld().getRegistryKey().getValue();
                setDirty(true);

                if (!source.isClient() && !castEntity.isPresent(source.getWorld())) {
                    CastSpellEntity entity = UEntities.CAST_SPELL.create(source.getWorld());
                    Vec3d pos = source.getOriginVector();
                    entity.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                    entity.setSpell(this);
                    entity.setMaster(source.getMaster());
                    entity.world.spawnEntity(entity);

                    castEntity.set(entity);
                }
            }

            if (!source.getWorld().getRegistryKey().getValue().equals(dimension)) {
                return false;
            }
        }

        return true;
    }

    public boolean onGroundTick(Caster<?> source) {
        particlEffect.ifAbsent(source, spawner -> {
            spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.MAGIC_RUNES, 90, 0), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.attach(source);
            p.setAttribute(1, getType().getColor());
        });

        return true;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (dimension != null) {
            compound.putString("dimension", dimension.toString());
        }
        compound.put("owner", castEntity.toNBT());
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (compound.contains("dimension")) {
            dimension = new Identifier(compound.getString("dimension"));
        }
        if (compound.contains("castEntity")) {
            castEntity.fromNBT(compound.getCompound("castEntity"));
        }
    }
}
