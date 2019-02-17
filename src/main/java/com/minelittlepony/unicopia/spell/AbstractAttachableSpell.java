package com.minelittlepony.unicopia.spell;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntitySpell;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractAttachableSpell extends AbstractSpell {

    boolean searching = true;

    @Nullable
    private UUID targettedEntityId;

    @Nullable
    private EntitySpell targettedEntity;

    protected void setTarget(@Nonnull EntitySpell e) {
        searching = false;
        targettedEntity = e;
        targettedEntityId = e.getUniqueID();
        setDirty(true);
    }

    public boolean hasTarget() {
        if (targettedEntity != null && targettedEntity.isDead) {
            targettedEntity = null;
            targettedEntityId = null;
            searching = true;
            setDirty(true);
        }

        return targettedEntityId != null;
    }

    @Nullable
    protected EntitySpell getTarget(ICaster<?> source) {
        if (targettedEntity == null && targettedEntityId != null) {
            source.getWorld().getEntities(EntitySpell.class, e -> e.getUniqueID().equals(targettedEntityId))
                .stream()
                .findFirst()
                .ifPresent(this::setTarget);
        }

        if (targettedEntity != null && targettedEntity.isDead) {
            targettedEntity = null;
            targettedEntityId = null;
            searching = true;
            setDirty(true);
        }

        return targettedEntity;
    }

    @Override
    public boolean update(ICaster<?> source) {

        if (searching) {
            searchForTarget(source);
        } else {
            getTarget(source);
        }

        return !getDead();
    }

    protected void searchForTarget(ICaster<?> source) {
        BlockPos origin = source.getOrigin();

        source.getWorld().getEntitiesInAABBexcluding(source.getEntity(), getSearchArea(source), e -> {
            return e instanceof EntitySpell && canTargetEntity((EntitySpell)e);
        }).stream()
                .sorted((a, b) -> (int)(a.getDistanceSq(origin) - b.getDistanceSq(origin)))
                .findFirst()
                .map(EntitySpell.class::cast)
                .ifPresent(this::setTarget);
    }

    protected abstract AxisAlignedBB getSearchArea(ICaster<?> source);

    protected abstract boolean canTargetEntity(EntitySpell e);

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (targettedEntityId != null) {
            compound.setUniqueId("target", targettedEntityId);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("target")) {
            targettedEntityId = compound.getUniqueId("target");
        }
    }
}
