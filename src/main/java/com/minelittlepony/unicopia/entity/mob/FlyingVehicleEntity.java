package com.minelittlepony.unicopia.entity.mob;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class FlyingVehicleEntity extends MobEntity implements MultiBoundingBoxEntity, MagicImmune, EquineContext {

    private final UUID[] seats = new UUID[getSeatCount()];

    protected FlyingVehicleEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    protected abstract int getSeatCount();

    protected abstract Vec3d getSeatPosition(int seatIndex);

    protected int getSeatFor(Entity passenger) {
        double closestDistance = Double.MAX_VALUE;
        int closestSeat = -1;

        for (int i = 0; i < seats.length; i++) {
            if (seats[i] != null) {
                if (seats[i].equals(passenger.getUuid())) {
                    return i;
                }
            } else {
                double distance = passenger.squaredDistanceTo(getSeatPosition(i).add(getPos()));
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSeat = i;
                }

            }
        }

        return closestSeat;
    }

    @Override
    protected final Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    public final Race getSpecies() {
        return Race.UNSET;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public final boolean isCollidable() {
        return true;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player && player.getAbilities().creativeMode) {
            dropInventory(world);
            remove(RemovalReason.KILLED);
            return true;
        }
        if (super.damage(world, source, amount)) {
            hurtTime = 0;
            maxHurtTime = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void updatePostDeath() {
        if (!getWorld().isClient() && !isRemoved()) {
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < seats.length && getSeatFor(passenger) != -1;
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seat = getSeatFor(passenger);
        if (seat == -1) {
            return Vec3d.ZERO;
        }
        return getSeatPosition(seat).multiply(scaleFactor);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        NbtList seats = compound.getList("seats", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < this.seats.length; i++) {
            this.seats[i] = i < seats.size() && seats.getCompound(i).containsUuid("id") ? seats.getCompound(i).getUuid("id") : null;
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        NbtList seats = new NbtList();
        for (int i = 0; i < this.seats.length; i++) {
            NbtCompound seat = new NbtCompound();
            if (this.seats[i] != null) {
                seat.putUuid("id", this.seats[i]);
            }
            seats.add(seat);
        }
        compound.put("seats", seats);
    }

}
