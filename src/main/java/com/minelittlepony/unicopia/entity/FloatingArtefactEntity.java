package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FloatingArtefactEntity extends Entity {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Byte> STATE = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> SPIN = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private float bobAmount;
    private float spinAmount;

    private float health = 1;
    public final float positionSeed;

    private int spinupDuration;

    private float sourceSpin = 1;
    private float targetSpin = 1;
    private float spinChange;
    private float spinChangeProgress;

    public FloatingArtefactEntity(EntityType<?> entityType, World world) {
        super(entityType, world);

        positionSeed = (float)(Math.random() * Math.PI * 2);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(ITEM, ItemStack.EMPTY);
        dataTracker.startTracking(STATE, (byte)0);
        dataTracker.startTracking(SPIN, 1F);
    }

    public ItemStack getStack() {
        return dataTracker.get(ITEM);
    }

    public void setStack(ItemStack stack) {
        dataTracker.set(ITEM, stack);
    }

    public State getState() {
        return State.valueOf(dataTracker.get(STATE));
    }

    public void setState(State state) {
        dataTracker.set(STATE, (byte)state.ordinal());
    }

    public void addSpin(float spin, int duration) {
        if (spin >= getSpin()) {
            setSpin(spin);
            spinupDuration = duration;
        }
    }

    public void setSpin(float spin) {
        dataTracker.set(SPIN, spin);
    }

    public float getSpin() {
        return dataTracker.get(SPIN);
    }

    @Override
    public void tick() {
        Vec3d pos = Vec3d.ofBottomCenter(getBlockPos());

        setPos(pos.x, pos.y, pos.z);

        super.tick();

        ItemStack stack = getStack();

        if (stack.isEmpty()) {
            setStack(UItems.EMPTY_JAR.getDefaultStack());
        }

        if (world.isClient) {
            float spin = getSpin();
            if (Math.abs(spin - targetSpin) > 1.0E-5F) {
                spinChange = spin - targetSpin;
                targetSpin = spin;
                spinChangeProgress = 0;
            }

            if (spinChange != 0) {
                if (spinChangeProgress < 1) {
                    spinChangeProgress += 0.05F;
                } else {
                    sourceSpin = targetSpin;
                    spinChange = 0;
                    spinChangeProgress = 0;
                }
            }

            spinAmount += sourceSpin + (spinChange * spinChangeProgress);
            bobAmount++;
        } else {
            spinupDuration = Math.max(0, spinupDuration - 1);
            if (spinupDuration <= 0) {
                setSpin(1);
            }
        }

        if (stack.getItem() instanceof Artifact) {
            ((Artifact)stack.getItem()).onArtifactTick(this);
        }

        if (world.getTime() % 80 == 0) {
            State state = getState();
            playSound(USounds.ENTITY_ARTEFACT_AMBIENT, state.getVolume(), state.getPitch());
        }
    }

    public float getVerticalOffset(float tickDelta) {
        return MathHelper.sin((bobAmount + tickDelta) / 10F + positionSeed) * 0.025F + 0.05F;
    }

    public float getRotation(float tickDelta) {
        return (spinAmount + tickDelta) / 20 + positionSeed;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        ItemStack itemStack = ItemStack.fromNbt(compound.getCompound("Item"));
        setStack(itemStack);
        setState(State.valueOf(compound.getInt("State")));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            compound.put("Item", stack.writeNbt(new NbtCompound()));
        }
        compound.putInt("State", getState().ordinal());
    }

    @Override
    public boolean damage(DamageSource damageSource, float amount) {
        if (isInvulnerableTo(damageSource) || !getStack().getItem().damage(damageSource)) {
            return false;
        }

        scheduleVelocityUpdate();

        health -= amount;
        if (health <= 0) {
            remove(RemovalReason.KILLED);

            ItemStack stack = getStack();

            if (!(stack.getItem() instanceof Artifact) || ((Artifact)stack.getItem()).onArtifactDestroyed(this) != ActionResult.SUCCESS) {
                dropStack(stack);
            }
        }

        return false;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }

    public enum State {
        INITIALISING,
        RUNNING,
        SHUTTING_DOWN;

        static final State[] VALUES = values();

        public float getVolume() {
            return this == SHUTTING_DOWN ? 1 : 0.2F;
        }

        public float getPitch() {
            return this == INITIALISING ? 1 : this == RUNNING ? 2 : 0.5F;
        }

        static State valueOf(int state) {
            return state <= 0 || state >= VALUES.length ? INITIALISING : VALUES[state];
        }
    }

    public interface Artifact {
        void onArtifactTick(FloatingArtefactEntity entity);

        ActionResult onArtifactDestroyed(FloatingArtefactEntity entity);
    }
}
