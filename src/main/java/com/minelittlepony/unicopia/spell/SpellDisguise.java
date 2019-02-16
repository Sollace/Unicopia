package com.minelittlepony.unicopia.spell;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.mixin.MixinEntity;
import com.minelittlepony.unicopia.player.IFlyingPredicate;
import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IPlayerHeightPredicate;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.ProjectileUtil;
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
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.MathHelper;

public class SpellDisguise extends AbstractSpell implements ISuppressable, IFlyingPredicate, IPlayerHeightPredicate {

    @Nonnull
    private String entityId = "";

    @Nullable
    private Entity entity;

    @Nullable
    private NBTTagCompound entityNbt;

    private int suppressionCounter;

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
        return 0x19E48E;
    }

    @Override
    public boolean isVulnerable(ICaster<?> otherSource, IMagicEffect other) {
        return suppressionCounter <= otherSource.getCurrentLevel();
    }

    @Override
    public void onSuppressed(ICaster<?> otherSource) {
        suppressionCounter = 100;
        setDirty(true);
    }

    @Override
    public boolean getSuppressed() {
        return suppressionCounter > 0;
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

        onEntityLoaded(source);
    }

    protected void checkAndCreateDisguiseEntity(ICaster<?> source) {
        if (entity == null && entityNbt != null) {
            NBTTagCompound nbt = entityNbt;
            entityNbt = null;

            if ("player".equals(entityId)) {
                createPlayer(nbt, new GameProfile(
                        nbt.getUniqueId("playerId"),
                        nbt.getString("playerName")
                    ), source);
                new Thread(() -> createPlayer(nbt, TileEntitySkull.updateGameProfile(new GameProfile(
                    null,
                    nbt.getString("playerName")
                )), source)).start();
            } else {
                entity = EntityList.createEntityFromNBT(nbt, source.getWorld());
            }

            onEntityLoaded(source);
        }
    }

    protected void onEntityLoaded(ICaster<?> source) {
        if (entity == null) {
            return;
        }

        if (source.getWorld().isRemote) {
            source.getWorld().spawnEntity(entity);
        }
    }

    protected void copyBaseAttributes(EntityLivingBase from, Entity to) {

        // Set first because position calculations rely on it
        to.onGround = from.onGround;

        if (isAttachedEntity(entity)) {
            to.posX = Math.floor(from.posX) + 0.5;
            to.posY = Math.floor(from.posY);
            to.posZ = Math.floor(from.posZ) + 0.5;

            to.lastTickPosX = to.posX;
            to.lastTickPosY = to.posY;
            to.lastTickPosZ = to.posZ;

            to.prevPosX = to.posX;
            to.prevPosY = to.posY;
            to.prevPosZ = to.posZ;

            to.setPosition(to.posX, to.posY, to.posZ);
        } else {
            to.copyLocationAndAnglesFrom(from);

            to.lastTickPosX = from.lastTickPosX;
            to.lastTickPosY = from.lastTickPosY;
            to.lastTickPosZ = from.lastTickPosZ;

            to.prevPosX = from.prevPosX;
            to.prevPosY = from.prevPosY;
            to.prevPosZ = from.prevPosZ;
        }

        if (to instanceof EntityPlayer) {
            EntityPlayer l = (EntityPlayer)to;

            l.chasingPosX = l.posX;
            l.chasingPosY = l.posY;
            l.chasingPosZ = l.posZ;
        }

        to.motionX = from.motionX;
        to.motionY = from.motionY;
        to.motionZ = from.motionZ;

        to.prevRotationPitch = from.prevRotationPitch;
        to.prevRotationYaw = from.prevRotationYaw;

        to.distanceWalkedOnStepModified = from.distanceWalkedOnStepModified;
        to.distanceWalkedModified = from.distanceWalkedModified;
        to.prevDistanceWalkedModified = from.prevDistanceWalkedModified;

        if (to instanceof EntityLivingBase) {
            EntityLivingBase l = (EntityLivingBase)to;

            l.rotationYawHead = from.rotationYawHead;
            l.prevRotationYawHead = from.prevRotationYawHead;
            l.renderYawOffset = from.renderYawOffset;
            l.prevRenderYawOffset = from.prevRenderYawOffset;

            l.limbSwing = from.limbSwing;
            l.limbSwingAmount = from.limbSwingAmount;
            l.prevLimbSwingAmount = from.prevLimbSwingAmount;

            l.swingingHand = from.swingingHand;
            l.swingProgress = from.swingProgress;
            l.swingProgressInt = from.swingProgressInt;
            l.isSwingInProgress = from.isSwingInProgress;

            l.hurtTime = from.hurtTime;
            l.deathTime = from.deathTime;
            l.setHealth(from.getHealth());

            for (EntityEquipmentSlot i : EntityEquipmentSlot.values()) {
                ItemStack neu = from.getItemStackFromSlot(i);
                ItemStack old = l.getItemStackFromSlot(i);
                if (old != neu) {
                    l.setItemStackToSlot(i, neu);
                }
            }
        }

        if (to instanceof EntityTameable) {
            ((EntityTameable)to).setSitting(from.isSneaking());
        }

        if (from.isBurning()) {
            to.setFire(1);
        } else {
            to.extinguish();
        }

        to.setSneaking(from.isSneaking());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean update(ICaster<?> source) {
        EntityLivingBase owner = source.getOwner();

        if (getSuppressed()) {
            suppressionCounter--;

            owner.setInvisible(false);
            if (source instanceof IPlayer) {
                ((IPlayer)source).setInvisible(false);
            }

            if (entity != null) {
                entity.setInvisible(true);
                entity.posY = Integer.MIN_VALUE;
            }

            return true;
        }

        checkAndCreateDisguiseEntity(source);

        if (owner == null) {
            return true;
        }

        if (entity == null) {
            if (source instanceof IPlayer) {
                owner.setInvisible(false);
                ((IPlayer) source).setInvisible(false);
            }

            return false;
        }

        entity.noClip = true;
        entity.updateBlocked = true;

        entity.getEntityData().setBoolean("disguise", true);

        if (entity instanceof EntityLiving) {
            ((EntityLiving)entity).setNoAI(true);
        }

        entity.setInvisible(false);
        entity.setNoGravity(true);

        copyBaseAttributes(owner, entity);

        if (!skipsUpdate(entity)) {
            entity.onUpdate();
        }

        if (entity instanceof EntityShulker) {
            EntityShulker shulker = ((EntityShulker)entity);

            shulker.rotationYaw = 0;
            shulker.renderYawOffset = 0;
            shulker.prevRenderYawOffset = 0;

            shulker.setAttachmentPos(null);

            if (source.getWorld().isRemote && source instanceof IPlayer) {
                IPlayer player = (IPlayer)source;


                float peekAmount = 0.3F;

                if (!owner.isSneaking()) {
                    float speed = (float)Math.sqrt(Math.pow(owner.motionX, 2) + Math.pow(owner.motionZ, 2));

                    peekAmount = MathHelper.clamp(speed * 30, 0, 1);
                }

                peekAmount = player.getInterpolator().interpolate("peek", peekAmount, 5);

                MixinEntity.Shulker.setPeek(shulker, peekAmount);
            }
        }

        if (entity instanceof EntityMinecart) {
            entity.rotationYaw += 90;
            entity.rotationPitch = 0;
        }

        if (source instanceof IPlayer) {
            IPlayer player = (IPlayer)source;

            player.setInvisible(true);
            source.getOwner().setInvisible(true);

            if (entity instanceof IOwned) {
                IOwned.cast(entity).setOwner(player.getOwner());
            }

            if (entity instanceof EntityPlayer) {
                entity.getDataManager().set(MixinEntity.Player.getModelFlag(), owner.getDataManager().get(MixinEntity.Player.getModelFlag()));
            }

            if (player.isClientPlayer() && UClient.instance().getViewMode() == 0) {
                entity.setInvisible(true);
                entity.posY = -Integer.MIN_VALUE;
            }

            return player.getPlayerSpecies() == Race.CHANGELING;
        }

        return !source.getOwner().isDead;
    }

    @Override
    public void setDead() {
        super.setDead();
        removeDisguise();
    }

    @Override
    public void render(ICaster<?> source) {
        if (getSuppressed()) {
            source.spawnParticles(UParticles.UNICORN_MAGIC, 5);
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
        } else if (source.getWorld().rand.nextInt(30) == 0) {
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("suppressionCounter", suppressionCounter);
        compound.setString("entityId", entityId);

        if (entityNbt != null) {
            compound.setTag("entity", entityNbt);
        } else if (entity != null) {
            compound.setTag("entity", encodeEntityToNBT(entity));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        suppressionCounter = compound.getInteger("suppressionCounter");

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
        if (entity == null || !player.getPlayerSpecies().canFly()) {
            return false;
        }

        if (entity instanceof IOwned) {
            IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(IOwned.<EntityPlayer>cast(entity).getOwner());

            return iplayer != null && iplayer.getPlayerSpecies().canFly();
        }

        return entity instanceof EntityFlying
                || entity instanceof net.minecraft.entity.passive.EntityFlying
                || entity instanceof EntityDragon
                || entity instanceof EntityAmbientCreature
                || entity instanceof EntityShulkerBullet
                || ProjectileUtil.isProjectile(entity);
    }

    @Override
    public float getTargetEyeHeight(IPlayer player) {
        if (entity != null && !getSuppressed()) {
            if (entity instanceof EntityFallingBlock) {
                return 0.5F;
            }
            return entity.getEyeHeight();
        }
        return -1;
    }

    @Override
    public float getTargetBodyHeight(IPlayer player) {
        if (entity != null && !getSuppressed()) {
            if (entity instanceof EntityFallingBlock) {
                return 0.9F;
            }
            return entity.height - 0.1F;
        }
        return -1;
    }

    public static boolean skipsUpdate(Entity entity) {
        return entity instanceof EntityFallingBlock
            || entity instanceof EntityPlayer;
    }

    public static boolean isAttachedEntity(Entity entity) {
        return entity instanceof EntityShulker
            || entity instanceof EntityFallingBlock;
    }
}
