package com.minelittlepony.unicopia.spell;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;

public class SpellDisguise extends AbstractSpell {

    @Nonnull
    private String entityId = "";

    @Nullable
    private Entity entity;

    @Nullable
    private NBTTagCompound entityNbt;

    @Override
    public String getName() {
        return "disguise";
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    @Override
    public int getTint() {
        return 0;
    }

    public SpellDisguise setDisguise(@Nullable Entity entity) {
        if (entity == this.entity) {
            entity = null;
        }
        this.entityNbt = null;

        if (this.entity != null) {
            this.entity.setDead();
        }

        this.entity = null;

        this.entityId = "";

        if (entity != null) {
            this.entityId = EntityList.getKey(entity).toString();
            this.entityNbt = entity.writeToNBT(new NBTTagCompound());
            this.entityNbt.setString("id", entityId);
        }

        return this;
    }

    @Override
    public boolean update(ICaster<?> source, int level) {
        if (entity == null && entityNbt != null) {
            entity = EntityList.createEntityFromNBT(entityNbt, source.getWorld());

            if (entity != null && source.getWorld().isRemote) {
                source.getWorld().spawnEntity(entity);
            }

            entityNbt = null;
        }

        EntityLivingBase owner = source.getOwner();

        if (owner == null) {
            return true;
        }

        if (entity != null) {
            entity.onGround = owner.onGround;
            entity.onUpdate();

            if (entity instanceof EntityLiving) {
                EntityLiving l = (EntityLiving)entity;

                l.setNoAI(true);

                l.rotationYawHead = owner.rotationYawHead;
                l.prevRotationYawHead = owner.prevRotationYawHead;
                l.renderYawOffset = owner.renderYawOffset;
                l.prevRenderYawOffset = owner.prevRenderYawOffset;

                l.limbSwing = owner.limbSwing;
                l.limbSwingAmount = owner.limbSwingAmount;
                l.prevLimbSwingAmount = owner.prevLimbSwingAmount;

                l.swingingHand = owner.swingingHand;
                l.swingProgress = owner.swingProgress;
                l.swingProgressInt = owner.swingProgressInt;
                l.isSwingInProgress = owner.isSwingInProgress;

                l.hurtTime = owner.hurtTime;
                l.deathTime = owner.deathTime;
                l.setHealth(owner.getHealth());

                for (EntityEquipmentSlot i : EntityEquipmentSlot.values()) {
                    l.setItemStackToSlot(i, owner.getItemStackFromSlot(i));
                }
            }


            entity.copyLocationAndAnglesFrom(owner);

            entity.setNoGravity(true);

            entity.prevPosX = owner.prevPosX;
            entity.prevPosY = owner.prevPosY;
            entity.prevPosZ = owner.prevPosZ;

            entity.motionX = owner.motionX;
            entity.motionY = owner.motionY;
            entity.motionZ = owner.motionZ;

            entity.prevRotationPitch = owner.prevRotationPitch;
            entity.prevRotationYaw = owner.prevRotationYaw;

            entity.distanceWalkedOnStepModified = owner.distanceWalkedOnStepModified;
            entity.distanceWalkedModified = owner.distanceWalkedModified;
            entity.prevDistanceWalkedModified = owner.prevDistanceWalkedModified;

            if (owner.isBurning()) {
                entity.setFire(1);
            } else {
                entity.extinguish();
            }

            entity.updateBlocked = true;

            owner.height = entity.height;

            entity.setSneaking(owner.isSneaking());
            entity.setInvisible(false);

            if (source instanceof IPlayer) {
                ((IPlayer) source).setInvisible(true);
            }
            owner.setInvisible(true);

            if (owner instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)owner;

                player.eyeHeight = entity.getEyeHeight();

                if (UClient.instance().isClientPlayer(player)) {
                    entity.setAlwaysRenderNameTag(false);
                    entity.setCustomNameTag("");

                    if (UClient.instance().getViewMode() == 0) {
                        entity.setInvisible(true);
                        entity.posY = -10;
                    }
                } else {
                    entity.setAlwaysRenderNameTag(true);
                    entity.setCustomNameTag(player.getName());
                }
            }

            if (!(source instanceof IPlayer) || ((IPlayer) source).getPlayerSpecies() == Race.CHANGELING) {
                return true;
            }
        }

        owner.setInvisible(false);

        if (source instanceof IPlayer) {
            ((IPlayer) source).setInvisible(false);
        }


        return false;
    }

    @Override
    public void setDead() {
        super.setDead();

        if (entity != null) {
            entity.setDead();
            entity = null;
        }
    }

    @Override
    public void render(ICaster<?> source, int level) {

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("entityId", entityId);
        compound.setBoolean("dead", getDead());

        if (entityNbt != null) {
            compound.setTag("entity", entityNbt);
        } else if (entity != null) {
            NBTTagCompound entityTag = new NBTTagCompound();
            entity.writeToNBT(entityTag);
            entityTag.setString("id", entityId);

            compound.setTag("entity", entityTag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        String newId = compound.getString("entityId");

        if (!newId.contentEquals(entityId)) {
            entityNbt = null;

            if (entity != null) {
                entity.setDead();
                entity = null;
            }
        }

        if (compound.hasKey("entity")) {
            entityId = newId;

            entityNbt = compound.getCompoundTag("entity");

            if (entity != null) {
                entity.readFromNBT(entityNbt);
            }
        }
    }
}
