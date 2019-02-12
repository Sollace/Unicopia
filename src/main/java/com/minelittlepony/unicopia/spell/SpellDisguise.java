package com.minelittlepony.unicopia.spell;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.mixin.MixinEntity;
import com.minelittlepony.unicopia.player.IFlyingPredicate;
import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySkull;

public class SpellDisguise extends AbstractSpell implements IFlyingPredicate {

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

    public Entity getDisguise() {
        return entity;
    }

    public SpellDisguise setDisguise(@Nullable Entity entity) {
        if (entity == this.entity) {
            entity = null;
        }
        this.entityNbt = null;
        this.entityId = "";

        removeDisguise();

        if (entity != null) {
            entityNbt = encodeEntityToNBT(entity);
            entityId = entityNbt.getString("id");
        }

        return this;
    }

    protected void removeDisguise() {
        if (entity != null) {
            entity.setDead();
            entity = null;
        }
    }

    protected NBTTagCompound encodeEntityToNBT(Entity entity) {
        NBTTagCompound entityNbt = new NBTTagCompound();

        if (entity instanceof EntityPlayer) {
            GameProfile profile = ((EntityPlayer)entity).getGameProfile();

            entityNbt.setString("id", "player");
            entityNbt.setUniqueId("playerId", profile.getId());
            entityNbt.setString("playerName", profile.getName());
            entityNbt.setTag("playerNbt", entity.writeToNBT(new NBTTagCompound()));
        } else {
            entityNbt = entity.writeToNBT(entityNbt);
            entityNbt.setString("id", EntityList.getKey(entity).toString());
        }

        return entityNbt;
    }

    protected synchronized void createPlayer(NBTTagCompound nbt, GameProfile profile, ICaster<?> source) {
        removeDisguise();

        entity = UClient.instance().createPlayer(source.getEntity(), profile);
        entity.setCustomNameTag(source.getOwner().getName());
        ((EntityPlayer)entity).readFromNBT(nbt.getCompoundTag("playerNbt"));
        entity.setUniqueId(UUID.randomUUID());

        PlayerSpeciesList.instance().getPlayer((EntityPlayer)entity).setEffect(null);

        if (entity != null && source.getWorld().isRemote) {
            source.getWorld().spawnEntity(entity);
        }
    }

    protected void checkAndCreateDisguiseEntity(ICaster<?> source) {
        if (entity == null && entityNbt != null) {
            if ("player".equals(entityId)) {

                NBTTagCompound nbt = entityNbt;
                entityNbt = null;

                createPlayer(nbt, new GameProfile(
                        nbt.getUniqueId("playerId"),
                        nbt.getString("playerName")
                    ), source);
                new Thread(() -> createPlayer(nbt, TileEntitySkull.updateGameProfile(new GameProfile(
                    null,
                    nbt.getString("playerName")
                )), source)).start();
            } else {
                entity = EntityList.createEntityFromNBT(entityNbt, source.getWorld());
            }

            if (entity != null && source.getWorld().isRemote) {
                source.getWorld().spawnEntity(entity);
            }

            entityNbt = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean update(ICaster<?> source) {
        checkAndCreateDisguiseEntity(source);

        EntityLivingBase owner = source.getOwner();

        if (owner == null) {
            return true;
        }

        if (entity != null) {
            entity.onGround = owner.onGround;

            if (!(entity instanceof EntityFallingBlock || entity instanceof EntityPlayer)) {
                entity.onUpdate();
            }

            entity.copyLocationAndAnglesFrom(owner);

            entity.setNoGravity(true);

            entity.getEntityData().setBoolean("disguise", true);

            entity.lastTickPosX = owner.lastTickPosX;
            entity.lastTickPosY = owner.lastTickPosY;
            entity.lastTickPosZ = owner.lastTickPosZ;

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

            if (entity instanceof EntityLivingBase) {
                EntityLivingBase l = (EntityLivingBase)entity;

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
                    ItemStack neu = owner.getItemStackFromSlot(i);
                    ItemStack old = l.getItemStackFromSlot(i);
                    if (old != neu) {
                        l.setItemStackToSlot(i, neu);
                    }
                }

                if (l instanceof EntityShulker) {
                    l.rotationYaw = 0;
                    l.rotationPitch = 0;
                    l.rotationYawHead = 0;
                    l.prevRotationYawHead = 0;
                    l.renderYawOffset = 0;
                    l.prevRenderYawOffset = 0;
                }
            }

            if (entity instanceof EntityShulker || entity instanceof EntityFallingBlock) {

                entity.posX = Math.floor(owner.posX) + 0.5;
                entity.posY = Math.floor(owner.posY + 0.2);
                entity.posZ = Math.floor(owner.posZ) + 0.5;

                entity.lastTickPosX = entity.posX;
                entity.lastTickPosY = entity.posY;
                entity.lastTickPosZ = entity.posZ;

                entity.prevPosX = entity.posX;
                entity.prevPosY = entity.posY;
                entity.prevPosZ = entity.posZ;

                if (entity instanceof EntityShulker) {
                    ((EntityShulker)entity).setAttachmentPos(null);
                }
            }

            if (entity instanceof EntityLiving) {
                EntityLiving l = (EntityLiving)entity;

                l.setNoAI(true);
            }

            if (entity instanceof EntityMinecart) {
                entity.rotationYaw += 90;
                entity.rotationPitch = 0;
            }

            if (entity instanceof EntityPlayer) {
                EntityPlayer l = (EntityPlayer)entity;

                l.chasingPosX = l.posX;
                l.chasingPosY = l.posY;
                l.chasingPosZ = l.posZ;
            }

            if (owner.isBurning()) {
                entity.setFire(1);
            } else {
                entity.extinguish();
            }

            entity.noClip = true;
            entity.updateBlocked = true;

            entity.setSneaking(owner.isSneaking());
            entity.setInvisible(false);

            if (source instanceof IPlayer) {
                ((IPlayer) source).setInvisible(true);
            }

            owner.setInvisible(true);

            if (owner instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)owner;

                player.eyeHeight = entity.getEyeHeight();

                if (entity instanceof IOwned) {
                    IOwned.cast(entity).setOwner(player);
                }

                if (entity instanceof EntityPlayer) {
                    entity.getDataManager().set(MixinEntity.Player.getModelFlag(), owner.getDataManager().get(MixinEntity.Player.getModelFlag()));
                }

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
        removeDisguise();
    }

    @Override
    public void render(ICaster<?> source) {

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("entityId", entityId);
        compound.setBoolean("dead", getDead());

        if (entityNbt != null) {
            compound.setTag("entity", entityNbt);
        } else if (entity != null) {
            compound.setTag("entity", encodeEntityToNBT(entity));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        String newId = compound.getString("entityId");

        if (!newId.contentEquals(entityId)) {
            entityNbt = null;
            removeDisguise();
        }

        if (compound.hasKey("entity")) {
            entityId = newId;

            entityNbt = compound.getCompoundTag("entity");

            if (entity != null) {
                entity.readFromNBT(entityNbt);
            }
        }
    }

    @Override
    public boolean checkCanFly(IPlayer player) {
        if (!player.getPlayerSpecies().canFly()) {
            return false;
        }

        if (entity != null) {
            if (entity instanceof EntityFlying
                    || entity instanceof net.minecraft.entity.passive.EntityFlying
                    || entity instanceof EntityDragon
                    || entity instanceof EntityAmbientCreature) {
                return true;
            }

            if (entity instanceof IOwned) {
                IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(IOwned.<EntityPlayer>cast(entity).getOwner());

                return iplayer != null && iplayer.getPlayerSpecies().canFly();
            }
        }

        return false;
    }
}
