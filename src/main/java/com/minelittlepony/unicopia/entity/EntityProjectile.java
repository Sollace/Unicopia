package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.item.ITossable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityProjectile extends EntitySnowball {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager
            .createKey(EntityProjectile.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Float> DAMAGE = EntityDataManager
            .createKey(EntityProjectile.class, DataSerializers.FLOAT);

    public EntityProjectile(World world) {
        super(world);
    }

    public EntityProjectile(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityProjectile(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    @Override
    protected void entityInit() {
        getDataManager().register(ITEM, ItemStack.EMPTY);
        getDataManager().register(DAMAGE, (float)0);
    }

    public ItemStack getItem() {
        ItemStack stack = getDataManager().get(ITEM);

        return  stack == null ? ItemStack.EMPTY : stack;
    }

    public void setItem(ItemStack stack) {
        getDataManager().set(ITEM, stack);
        getDataManager().setDirty(ITEM);
    }

    public void setThrowDamage(float damage) {
        getDataManager().set(DAMAGE, Math.max(0, damage));
    }

    public float getThrowDamage() {
        return getDataManager().get(DAMAGE);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        ItemStack itemstack = new ItemStack(compound.getCompoundTag("Item"));

        if (itemstack.isEmpty()) {
            setDead();
        } else {
            setItem(itemstack);
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            ItemStack stack = getItem();

            for (int i = 0; i < 8; i++) {
                world.spawnParticle(EnumParticleTypes.ITEM_CRACK, posX, posY, posZ, 0, 0, 0, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        ItemStack itemstack = this.getItem();

        if (!itemstack.isEmpty()) {
            compound.setTag("Item", itemstack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.entityHit != this && !(result.entityHit instanceof IProjectile)) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                Item item = getItem().getItem();

                if (item instanceof ITossable) {
                    ((ITossable)item).onImpact(world, result.getBlockPos(), world.getBlockState(result.getBlockPos()));
                }
            }

            if (result.entityHit != null) {
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), getThrowDamage());
            }



            if (!world.isRemote) {
                world.setEntityState(this, (byte)3);
                setDead();
            }
        }
    }
}
