package com.minelittlepony.unicopia.magic.spell;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.world.entity.SpellcastEntity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public abstract class AbstractLinkedSpell extends AbstractSpell {

    protected boolean searching = true;

    @Nullable
    private UUID targettedEntityId;

    @Nullable
    private SpellcastEntity targettedEntity;

    protected void setTarget(@Nonnull SpellcastEntity e) {
        searching = false;
        targettedEntity = e;
        targettedEntityId = e.getUuid();
        setDirty(true);
    }

    public boolean hasTarget() {
        if (targettedEntity != null && targettedEntity.removed) {
            targettedEntity = null;
            targettedEntityId = null;
            searching = true;
            setDirty(true);
        }

        return targettedEntityId != null;
    }

    @Nullable
    protected SpellcastEntity getTarget(Caster<?> source) {
        if (targettedEntity == null && targettedEntityId != null) {
            Entity e = ((ServerWorld)source.getWorld()).getEntity(targettedEntityId);

            if (e instanceof SpellcastEntity) {
                setTarget((SpellcastEntity)e);
            }
        }

        if (targettedEntity != null && targettedEntity.removed) {
            targettedEntity = null;
            targettedEntityId = null;
            searching = true;
            setDirty(true);
        }

        return targettedEntity;
    }

    @Override
    public boolean update(Caster<?> source) {

        if (source.getWorld() instanceof ServerWorld) {
            if (searching) {
                searchForTarget(source);
            } else {
                getTarget(source);
            }
        }

        return !isDead();
    }

    protected void searchForTarget(Caster<?> source) {
        BlockPos origin = source.getOrigin();

        source.getWorld().getEntities(source.getEntity(), getSearchArea(source), e -> {
            return e instanceof SpellcastEntity && canTargetEntity((SpellcastEntity)e);
        }).stream()
                .sorted((a, b) -> (int)(a.getBlockPos().getSquaredDistance(origin) - b.getBlockPos().getSquaredDistance(origin)))
                .findFirst()
                .map(SpellcastEntity.class::cast)
                .ifPresent(this::setTarget);
    }

    protected abstract Box getSearchArea(Caster<?> source);

    protected abstract boolean canTargetEntity(SpellcastEntity e);

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (targettedEntityId != null) {
            compound.putUuid("target", targettedEntityId);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (compound.contains("target")) {
            targettedEntityId = compound.getUuid("target");
        }
    }
}
