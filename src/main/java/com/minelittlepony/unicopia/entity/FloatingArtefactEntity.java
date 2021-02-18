package com.minelittlepony.unicopia.entity;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FloatingArtefactEntity extends Entity {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Byte> STATE = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.BYTE);

    private int age;
    private float health = 1;
    public final float positionSeed;

    public FloatingArtefactEntity(EntityType<?> entityType, World world) {
        super(entityType, world);

        positionSeed = (float)(Math.random() * Math.PI * 2);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(ITEM, ItemStack.EMPTY);
        dataTracker.startTracking(STATE, (byte)0);
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

    public void addSpin(int spin) {
        if (age != -32768) {
            age += spin;
        }
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

        if (stack.getItem() instanceof Artifact) {
            ((Artifact)stack.getItem()).onArtifactTick(this);
        }

        if (world.getTime() % 80 == 0) {
            State state = getState();
            playSound(SoundEvents.BLOCK_BEACON_AMBIENT, state.getVolume(), state.getPitch());
        }

        addSpin(1);
    }

    public float getVerticalOffset(float tickDelta) {
        return MathHelper.sin((age + tickDelta) / 10F + positionSeed) * 0.025F + 0.05F;
    }

    public float getRotation(float tickDelta) {
        return (age + tickDelta) / 20 + positionSeed;
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag compound) {
        ItemStack itemStack = ItemStack.fromTag(compound.getCompound("Item"));
        setStack(itemStack);
        setState(State.valueOf(compound.getInt("State")));
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag compound) {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            compound.put("Item", stack.toTag(new CompoundTag()));
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
            remove();

            ItemStack stack = getStack();

            if (!(stack.getItem() instanceof Artifact) || ((Artifact)stack.getItem()).onArtifactDestroyed(this) != ActionResult.SUCCESS) {
                dropStack(stack);
            }
        }

        return false;
    }

    @Override
    public boolean collides() {
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
