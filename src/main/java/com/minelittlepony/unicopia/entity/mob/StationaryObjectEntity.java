package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.damage.UDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StationaryObjectEntity extends Entity implements UDamageSources, MagicImmune {
    private static final TrackedData<Float> HEALTH = DataTracker.registerData(StationaryObjectEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public StationaryObjectEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(HEALTH, getMaxHealth());
    }

    public abstract float getMaxHealth();

    public final float setHealth(float health) {
        health = MathHelper.clamp(health, 0, getMaxHealth());
        dataTracker.set(HEALTH, health);
        return health;
    }

    public final float getHealth() {
        return dataTracker.get(HEALTH);
    }

    public final boolean isDead() {
        return isRemoved() || getHealth() <= 0;
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        if (!isDead()) {
            if (setHealth(getHealth() - damage) <= 0) {
                kill();
                onKilled(source);
            } else {
                onHurt();
            }
        }
        return false;
    }

    protected void onKilled(DamageSource source) {

    }

    protected void onHurt() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        if (compound.contains("health", NbtElement.FLOAT_TYPE)) {
            setHealth(compound.getFloat("health"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        compound.putFloat("health", getHealth());
    }

    @Override
    public final boolean canHit() {
        return true;
    }

    @Override
    public final World asWorld() {
        return getWorld();
    }
}
