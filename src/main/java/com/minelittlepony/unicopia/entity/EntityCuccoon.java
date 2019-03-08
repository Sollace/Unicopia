package com.minelittlepony.unicopia.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.USounds;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityCuccoon extends EntityLivingBase implements IMagicals, IInAnimate {

    private static final DataParameter<Integer> STRUGGLE_COUNT = EntityDataManager.createKey(EntityCuccoon.class, DataSerializers.VARINT);

    private final List<ItemStack> armour = Lists.newArrayList();

    private boolean captiveLastSneakState;

    public EntityCuccoon(World world) {
        super(world);
        setSize(0.6f, 0.6f);

        width = 1.5F;
        height = 1.6F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(STRUGGLE_COUNT, 0);
    }

    public int getStruggleCount() {
        return getDataManager().get(STRUGGLE_COUNT) % 6;
    }

    public void setStruggleCount(int count) {
        getDataManager().set(STRUGGLE_COUNT, count % 6);
    }

    @Override
    protected boolean canBeRidden(Entity entity) {
        return super.canBeRidden(entity)
                && !entity.isSneaking()
                && !isBeingRidden()
                && entity instanceof EntityLivingBase
                && !Predicates.BUGGY.test(entity);
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public double getMountedYOffset() {
        return 0;
    }

    @Override
    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (isBeingRidden()) {
            Entity passenger = getPassengers().get(0);

            boolean sneaking = passenger.isSneaking();

            if (sneaking && !attemptDismount(passenger)) {
                passenger.setSneaking(false);
            }

            captiveLastSneakState = sneaking;
        }

        if (world.isRemote) {
            double x = posX + width * world.rand.nextFloat() - width/2;
            double y = posY + height * world.rand.nextFloat();
            double z = posZ + width * world.rand.nextFloat() - width/2;

            world.spawnParticle(EnumParticleTypes.DRIP_LAVA, x, y, z, 0, 0, 0);
        }
    }

    public float getBreatheAmount(float stutter) {
        return MathHelper.sin((ticksExisted + stutter) / 40) / 2
                + hurtTime / 10F;
    }

    @Override
    public boolean attackable() {
        return false;
    }

    public boolean attemptDismount(Entity captive) {
        if (captive.isSneaking() != captiveLastSneakState) {
            setStruggleCount(getStruggleCount() + 1);

            for (int k = 0; k < 20; k++) {
                double d2 = rand.nextGaussian() * 0.02;
                double d0 = rand.nextGaussian() * 0.02;
                double d1 = rand.nextGaussian() * 0.02;

                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        posX + rand.nextFloat() * width * 2 - width,
                        posY + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2 - width,
                        d2, d0, d1, Block.getStateId(Blocks.SLIME_BLOCK.getDefaultState()));
            }

            captive.playSound(USounds.SLIME_RETRACT, 1, 1);
            this.hurtTime += 15;

            if (getStruggleCount() == 0) {
                setDead();

                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDeathUpdate() {
        if (++deathTime == 20) {
            if (!world.isRemote && (isPlayer() || recentlyHit > 0 && canDropLoot() && world.getGameRules().getBoolean("doMobLoot"))) {
                int i = ForgeEventFactory.getExperienceDrop(this, attackingPlayer, getExperiencePoints(attackingPlayer));

                while (i > 0) {
                    int j = EntityXPOrb.getXPSplit(i);

                    i -= j;

                    world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, j));
                }

                this.dismountRidingEntity();
            }

            setDead();

            for (int k = 0; k < 20; k++) {
                double d2 = rand.nextGaussian() * 0.02;
                double d0 = rand.nextGaussian() * 0.02;
                double d1 = rand.nextGaussian() * 0.02;

                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        posX + rand.nextFloat() * width * 2 - width,
                        posY + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2 - width,
                        d2, d0, d1, Block.getStateId(Blocks.SLIME_BLOCK.getDefaultState()));
            }
        }
    }

    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity.canBeCollidedWith() ? entity.getEntityBoundingBox() : null;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox().shrink(0.2);
    }

    @Override
    protected void collideWithEntity(Entity entity) {
        if (canBeRidden(entity)) {
            entity.playSound(USounds.SLIME_ADVANCE, 1, 1);
            entity.startRiding(this, true);
        } else {
            super.collideWithEntity(entity);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
    }

    @Override
    public boolean canInteract(Race race) {
        return race == Race.CHANGELING;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return armour;
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.LEFT;
    }
}
