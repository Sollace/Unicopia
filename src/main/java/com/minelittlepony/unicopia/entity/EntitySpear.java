package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.projectile.IAdvancedProjectile;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpear extends ArrowEntity implements IAdvancedProjectile {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(EntitySpear.class, TrackedDataHandlerRegistry.ITEM_STACK);

    private static final TrackedData<Integer> KNOCKBACK = DataTracker.registerData(EntitySpear.class, TrackedDataHandlerRegistry.INTEGER);

    public EntitySpear(World world) {
        super(world);
    }

    public EntitySpear(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntitySpear(World world, LivingEntity shooter) {
        super(world, shooter);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
        getDataTracker().startTracking(KNOCKBACK, 0);
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
            Vec3d vel = getVelocity();

            double speed = vel.length();
            int damage = MathHelper.ceil(speed * getDamage());

            if (getIsCritical()) {
                damage += random.nextInt(damage / 2 + 2);
            }

            DamageSource damagesource = MagicalDamageSource.causeIndirect("spear", this, shootingEntity == null ? this : shootingEntity);

            if (isBurning() && !(entity instanceof EntityEnderman)) {
                entity.setFire(5);
            }

            if (entity.attackEntityFrom(damagesource, damage)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity entitylivingbase = (LivingEntity)entity;

                    if (!world.isClient) {
                        entitylivingbase.setStuckArrows(entitylivingbase.getStuckArrows() + 1);
                    }

                    int knockback = getDataTracker().get(KNOCKBACK);

                    if (knockback > 0) {

                        double f1 = MathHelper.sqrt(vel.x * vel.x + vel.z * vel.z);

                        if (f1 > 0) {
                            entitylivingbase.addVelocity(
                                    vel.x * knockback * 0.6000000238418579D / f1,
                                    0.1D,
                                    vel.z * knockback * 0.6000000238418579D / f1);
                        }
                    }

                    if (shootingEntity instanceof LivingEntity) {
                        EnchantmentHelper.applyThornEnchantments(entitylivingbase, shootingEntity);
                        EnchantmentHelper.applyArthropodEnchantments((LivingEntity)shootingEntity, entitylivingbase);
                    }

                    arrowHit(entitylivingbase);

                    if (shootingEntity != null && entitylivingbase != shootingEntity && entitylivingbase instanceof PlayerEntity && shootingEntity instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0));
                    }
                }

                playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));

                if (!(entity instanceof EndermanEntity)) {
                    remove();
                }

                return;
            }
        }

        super.onHit(raytraceResultIn);
    }

    @Override
    protected ItemStack asItemStack() {
        return getDataTracker().get(ITEM);
    }

    @Override
    public void setItem(ItemStack stack) {
        getDataTracker().set(ITEM, new ItemStack(stack.getItem(), 1, stack.getMetadata()));
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
