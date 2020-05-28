package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.magic.ThrowableSpell;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.projectile.AdvancedProjectile;

import net.minecraft.client.network.packet.GameStateChangeS2CPacket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpearEntity extends ArrowEntity implements AdvancedProjectile {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(SpearEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    private static final TrackedData<Integer> KNOCKBACK = DataTracker.registerData(SpearEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public SpearEntity(EntityType<SpearEntity> type, World world) {
        super(type, world);
    }

    public SpearEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public SpearEntity(World world, LivingEntity shooter) {
        super(world, shooter);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
        getDataTracker().startTracking(KNOCKBACK, 0);
    }

    @Override
    public void setVelocity(double x, double y, double z, float velocity, float inaccuracy) {
        setDamage(0);

        super.setVelocity(x, y, z, velocity, inaccuracy);
    }

    @Override
    public void move(MovementType type, Vec3d delta) {
        super.move(type, delta);

        if (type == MovementType.SELF && !inGround) {
            setDamage(getDamage() + 0.02);
        }
    }

    @Override
    public void setPunch(int amount) {
        super.setPunch(amount);
        getDataTracker().set(KNOCKBACK, amount);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {

        Entity entity = hit.getEntity();

        if (entity != null) {
            Vec3d vel = getVelocity();

            double speed = vel.length();
            int damage = MathHelper.ceil(Math.max(speed * this.getDamage(), 0));

            if (isCritical()) {
                damage += random.nextInt(damage / 2 + 2);
            }

            Entity archer = getOwner();
            DamageSource damagesource = MagicalDamageSource.causeIndirect("spear", this, archer == null ? this : archer);

            if (isOnFire() && !(entity instanceof EndermanEntity)) {
                entity.setOnFireFor(5);
            }

            if (entity.damage(damagesource, damage)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity)entity;

                    if (!world.isClient) {
                        target.setStuckArrowCount(target.getStuckArrowCount() + 1);
                    }

                    int knockback = getDataTracker().get(KNOCKBACK);

                    if (knockback > 0) {

                        double f1 = MathHelper.sqrt(vel.x * vel.x + vel.z * vel.z);

                        if (f1 > 0) {
                            target.addVelocity(
                                    vel.x * knockback * 0.6000000238418579D / f1,
                                    0.1D,
                                    vel.z * knockback * 0.6000000238418579D / f1);
                        }
                    }

                    if (!this.world.isClient && archer instanceof LivingEntity) {
                        EnchantmentHelper.onUserDamaged(target, archer);
                        EnchantmentHelper.onTargetDamaged((LivingEntity)archer, target);
                    }

                    onHit(target);

                    if (archer != null && target != archer && target instanceof PlayerEntity && archer instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)archer).networkHandler.sendPacket(new GameStateChangeS2CPacket(6, 0));
                    }
                }

                playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));

                if (!(entity instanceof EndermanEntity)) {
                    remove();
                }

                return;
            }
        }
    }

    @Override
    protected ItemStack asItemStack() {
        return getDataTracker().get(ITEM);
    }

    @Override
    public void setItem(ItemStack stack) {
        getDataTracker().set(ITEM, stack.copy());
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
        setProperties(shooter, pitch, yaw, pitchOffset, velocity, inaccuracy);
    }

    @Override
    public void setGravity(boolean gravity) {
    }

    @Override
    public void setOwner(LivingEntity owner) {
        setOwner((Entity)owner);
    }

    @Override
    public void setEffect(ThrowableSpell effect) {
    }
}
