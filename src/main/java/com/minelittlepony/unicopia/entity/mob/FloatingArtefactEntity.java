package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Altar;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FloatingArtefactEntity extends StationaryObjectEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Byte> STATE = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> TARGET_ROTATION_SPEED = DataTracker.registerData(FloatingArtefactEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final int REGEN_GAP_TICKS = 5;
    private static final int REGEN_PAUSE_TICKS = 200;

    public static final int INFINITE_BOOST_DURATION = -1;

    private float bobAmount;

    private float prevRotationSpeed;
    private float rotationSpeed;
    private float prevRotation;
    private float rotation;

    private int boostDuration;

    private int ticksUntilRegen;
    public final float positionSeed;

    private Optional<Altar> altar = Optional.empty();

    public FloatingArtefactEntity(EntityType<?> entityType, World world) {
        super(entityType, world);

        positionSeed = (float)(Math.random() * Math.PI * 2);
    }

    @Override
    protected Text getDefaultName() {
        return getStack().getName();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(ITEM, ItemStack.EMPTY);
        dataTracker.startTracking(STATE, (byte)0);
        dataTracker.startTracking(TARGET_ROTATION_SPEED, 1F);
    }

    public void setAltar(Altar altar) {
        this.altar = Optional.of(altar);
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

    public void setRotationSpeed(float spin, int duration) {
        dataTracker.set(TARGET_ROTATION_SPEED, Math.max(spin, 0));
        boostDuration = duration;
    }

    public float getRotationSpeed() {
        return dataTracker.get(TARGET_ROTATION_SPEED);
    }

    public float getRotationSpeed(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevRotationSpeed, rotationSpeed);
    }

    @Override
    public float getMaxHealth() {
        return 20F;
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

        if (getWorld().isClient) {
            bobAmount++;
        }

        float targetRotationSpeed = getRotationSpeed();

        if (rotationSpeed != targetRotationSpeed) {
            float difference = targetRotationSpeed - rotationSpeed;
            rotationSpeed = Math.abs(difference) < 0.02F ? targetRotationSpeed : (rotationSpeed + difference * (difference > 0 ? 0.5F : 0.1F));
        } else {
            if (boostDuration > 0 && --boostDuration <= 0) {
                setRotationSpeed(1, 0);
            }
        }

        rotation %= 360;
        prevRotation = rotation;
        rotation += rotationSpeed;

        if (stack.getItem() instanceof Artifact) {
            ((Artifact)stack.getItem()).onArtifactTick(this);
        }

        if (getHealth() < getMaxHealth() && --ticksUntilRegen <= 0) {
            setHealth(getHealth() + 1);
            ticksUntilRegen = REGEN_GAP_TICKS;
        }

        if (getWorld().getTime() % 80 == 0) {
            State state = getState();
            playSound(USounds.ENTITY_ARTEFACT_AMBIENT, state.getVolume(), state.getPitch());
        }
    }

    public float getVerticalOffset(float tickDelta) {
        return MathHelper.sin((bobAmount + tickDelta) / 10F + positionSeed) * 0.025F + 0.05F;
    }

    public float getRotation(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevRotation, rotation) % 360;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setStack(ItemStack.fromNbt(compound.getCompound("Item")));
        setState(State.valueOf(compound.getInt("State")));
        setRotationSpeed(compound.getFloat("spin"), compound.getInt("spinDuration"));
        ticksUntilRegen = compound.getInt("regen");
        altar = Altar.SERIALIZER.readOptional("altar", compound);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            compound.put("Item", stack.writeNbt(new NbtCompound()));
        }
        compound.putInt("State", getState().ordinal());
        compound.putFloat("spin", getRotationSpeed());
        compound.putInt("spinDuration", boostDuration);
        compound.putInt("regen", ticksUntilRegen);
        Altar.SERIALIZER.writeOptional("altar", compound, altar);
    }

    @Override
    public boolean damage(DamageSource source, float damage) {

        if (getWorld().isClient || isInvulnerable()) {
            return false;
        }

        if (isInvulnerableTo(source) || !getStack().getItem().damage(source)) {
            return false;
        }

        ticksUntilRegen = REGEN_PAUSE_TICKS;

        if (source.isSourceCreativePlayer()) {
            damage = getHealth();
        }

        return super.damage(source, damage);
    }

    @Override
    protected void onKilled(DamageSource source) {
        ItemStack stack = getStack();

        if (altar.isEmpty()) {
            if (!(stack.getItem() instanceof Artifact) || ((Artifact)stack.getItem()).onArtifactDestroyed(this) != ActionResult.SUCCESS) {
                if (!source.isSourceCreativePlayer()) {
                    dropStack(stack);
                }
            }
        }
    }

    @Override
    protected void onHurt() {
        playSound(USounds.ITEM_ICARUS_WINGS_WARN, 1, 1);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        altar.ifPresent(altar -> altar.tearDown(this, getWorld()));
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
