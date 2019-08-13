package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.tossable.ITossed;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySpear extends EntityArrow implements ITossed {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager
            .createKey(EntitySpear.class, DataSerializers.ITEM_STACK);

    private static final DataParameter<Integer> KNOCKBACK = EntityDataManager
            .createKey(EntitySpear.class, DataSerializers.VARINT);

    public EntitySpear(World world) {
        super(world);
    }

    public EntitySpear(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntitySpear(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(ITEM, ItemStack.EMPTY);
        getDataManager().register(KNOCKBACK, 0);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {

        setDamage(0);

        super.shoot(x, y, z, velocity, inaccuracy);
    }

    public void move(MoverType type, double x, double y, double z) {
        super.move(type, x, y, z);

        if (type == MoverType.SELF && !inGround) {
            setDamage(getDamage() + 0.02);
        }
    }

    public void setKnockbackStrength(int amount) {
        super.setKnockbackStrength(amount);
        getDataManager().set(KNOCKBACK, amount);
    }

    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        Entity entity = raytraceResultIn.entityHit;

        if (entity != null) {
            float speed = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            int damage = MathHelper.ceil(speed * getDamage());

            if (getIsCritical()) {
                damage += rand.nextInt(damage / 2 + 2);
            }

            DamageSource damagesource = MagicalDamageSource.causeIndirect("spear", this, shootingEntity == null ? this : shootingEntity);

            if (isBurning() && !(entity instanceof EntityEnderman)) {
                entity.setFire(5);
            }

            if (entity.attackEntityFrom(damagesource, damage)) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase)entity;

                    if (!world.isRemote) {
                        entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
                    }

                    int knockback = getDataManager().get(KNOCKBACK);

                    if (knockback > 0) {
                        float f1 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);

                        if (f1 > 0.0F) {
                            entitylivingbase.addVelocity(
                                    motionX * knockback * 0.6000000238418579D / f1,
                                    0.1D,
                                    motionZ * knockback * 0.6000000238418579D / f1);
                        }
                    }

                    if (shootingEntity instanceof EntityLivingBase) {
                        EnchantmentHelper.applyThornEnchantments(entitylivingbase, shootingEntity);
                        EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)shootingEntity, entitylivingbase);
                    }

                    arrowHit(entitylivingbase);

                    if (shootingEntity != null && entitylivingbase != shootingEntity && entitylivingbase instanceof EntityPlayer && shootingEntity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0));
                    }
                }

                playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));

                if (!(entity instanceof EntityEnderman)) {
                    setDead();
                }

                return;
            }
        }

        super.onHit(raytraceResultIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return getDataManager().get(ITEM);
    }

    @Override
    public void setItem(ItemStack stack) {
        getDataManager().set(ITEM, new ItemStack(stack.getItem(), 1, stack.getMetadata()));
    }

    @Override
    public void setThrowDamage(float damage) {
        setDamage(damage);
    }

    @Override
    public float getThrowDamage() {
        return (float)getDamage();
    }

    @Override
    public void setHydrophobic() {

    }

    @Override
    public boolean getHydrophobic() {
        return false;
    }

    @Override
    public void launch(Entity shooter, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        shoot(shooter, pitch, yaw, pitchOffset, velocity, inaccuracy);
    }
}
