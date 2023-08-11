package com.minelittlepony.unicopia.entity;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.WeatherConditions;

public class AirBalloonEntity extends FlyingEntity implements EntityCollisions.ComplexCollidable, MultiBoundingBoxEntity {
    private static final byte HAS_BALLOON = 1;
    private static final byte HAS_BURNER = 2;
    private static final byte BURNER_ACTIVE = 4;
    private static final TrackedData<Integer> FLAGS = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> BOOSTING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private boolean prevBoosting;

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(FLAGS, 0);
        dataTracker.startTracking(BOOSTING, 0);
    }

    public boolean hasBalloon() {
        return getFlag(HAS_BALLOON);
    }

    public void setHasBalloon(boolean hasBalloon) {
        setFlag(HAS_BALLOON, hasBalloon);
    }

    public boolean hasBurner() {
        return getFlag((byte)(HAS_BURNER | HAS_BALLOON));
    }

    public void setHasBurner(boolean hasBurner) {
        setFlag(HAS_BURNER, hasBurner);
    }

    public boolean isBurnerActive() {
        return getFlag((byte)(HAS_BURNER | BURNER_ACTIVE | HAS_BALLOON));
    }

    public void setBurnerActive(boolean burnerActive) {
        setFlag(BURNER_ACTIVE, burnerActive);
    }

    public int getBoostTicks() {
        return dataTracker.get(BOOSTING);
    }

    protected void setBoostTicks(int ticks) {
        dataTracker.set(BOOSTING, ticks);
    }

    private boolean getFlag(byte flag) {
        return (dataTracker.get(FLAGS).intValue() & flag) == flag;
    }

    private void setFlag(byte flag, boolean val) {
        int v = dataTracker.get(FLAGS);
        dataTracker.set(FLAGS, val ? (v | flag) : (v & ~flag));
    }

    private boolean isAirworthy() {
        return hasBalloon() && isBurnerActive();
    }

    @Override
    public List<Box> getBoundingBoxes() {
        if (hasBalloon()) {
            Box balloonBox = getBalloonBoundingBox();
            return List.of(getBoundingBox(), balloonBox.withMinY(balloonBox.minY - 0.5));
        }
        return List.of(getBoundingBox());
    }

    private Vec3d oldPosition = Vec3d.ZERO;
    private Vec3d oldServerPosition = Vec3d.ZERO;

    @Override
    public void tick() {
        setAir(getMaxAir());
        int boostTicks = getBoostTicks();

        if (boostTicks > 0) {
            boostTicks--;
            setBoostTicks(boostTicks);
        }

        addVelocity(0, isBurnerActive() ? 0.005 : -0.03, 0);

        if (!isAirworthy() && isSubmergedInWater()) {
            double yy = getVelocity().y;
            setVelocity(getVelocity().multiply(0.9, 0.4, 0.9).add(0, Math.abs(yy) / 2F, 0));
        }

        boolean boosting = boostTicks > 0;

        Random rng = getWorld().random;

        if (getWorld().isClient()) {
            if (hasBurner() && isBurnerActive()) {
                Vec3d burnerPos = getPos().add(0, 3, 0);
                for (int i = 0; i < (boosting ? 6 : 1); i++) {
                    getWorld().addParticle(ParticleTypes.FLAME,
                            rng.nextTriangular(burnerPos.x, 0.25),
                            rng.nextTriangular(burnerPos.y, 1),
                            rng.nextTriangular(burnerPos.z, 0.25),
                            0,
                            Math.max(0, getVelocity().y + (boosting ? 0.1 : 0)),
                            0
                    );
                }
            }
        } else {
            if (hasBurner() && isBurnerActive()) {
                addVelocity(WeatherConditions.getAirflow(getBlockPos(), getWorld()).multiply(0.2));
                setVelocity(getVelocity().multiply(0.3, 1, 0.3));
            }

            if (boosting) {
                addVelocity(0, 0.02, 0);
            }
        }

        if (boosting && !prevBoosting) {
            playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1, 1);
        }

        if (isBurnerActive() && age % 15 + rng.nextInt(5) == 0) {
            playSound(SoundEvents.ENTITY_GHAST_SHOOT, 0.2F, 1);
        }

        if (isLeashed()) {
            Vec3d leashPost = getHoldingEntity().getPos();
            Vec3d pos = getPos();

            if (leashPost.distanceTo(pos) >= 5) {
                Vec3d newVel = leashPost.subtract(pos).multiply(0.01);
                setVelocity(newVel.lengthSquared() < 0.03 ? Vec3d.ZERO : newVel);
            }
        }

        prevBoosting = boosting;
        oldPosition = getPos();
        oldServerPosition = LivingEntityDuck.serverPos(this);

        for (Box box : getBoundingBoxes()) {
            for (Entity e : getWorld().getOtherEntities(this, box.expand(0, 0.5, 0))) {
                updatePassenger(e, box, e.getY() > getY() + 3);
            }
        }
        super.tick();
    }

    private void updatePassenger(Entity e, Box box, boolean inBalloon) {

        if (getVelocity().y > 0 && e.getBoundingBox().minY < box.maxY) {
            e.setPos(e.getX(), box.maxY, e.getZ());
        }
        if (getVelocity().y < 0 && e.getBoundingBox().minY > box.maxY) {
            e.setPos(e.getX(), box.maxY, e.getZ());
        }

        if (inBalloon && !e.isSneaky() && Math.abs(e.getVelocity().y) > 0.079) {
            e.setVelocity(e.getVelocity().multiply(1, e.getVelocity().y < 0 ? -0.9 : 1.2, 1).add(0, 0.8, 0));
            if (Math.abs(e.getVelocity().y) > 2) {
                e.setVelocity(e.getVelocity().x, MathHelper.clamp(e.getVelocity().y, -2, 2), e.getVelocity().z);
            }
        }

        Living.getOrEmpty(e).ifPresent(living -> {
            living.setSupportingEntity(this);
            living.setPositionOffset(
                    e.getPos().subtract(oldPosition),
                    LivingEntityDuck.serverPos(living.asEntity()).subtract(oldServerPosition)
            );
            living.updateRelativePosition();
        });

        if (getWorld().isClient) {
            if (e.distanceTraveled > ((EntityDuck)e).getNextStepSoundDistance()) {
                e.distanceTraveled--;

                e.playSound(inBalloon ? SoundEvents.BLOCK_WOOL_STEP : SoundEvents.BLOCK_BAMBOO_STEP, 0.5F, 1);
            }
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (hasBalloon() && hasBurner()) {
            if (stack.isOf(Items.FLINT_AND_STEEL)) {
                setBurnerActive(!isBurnerActive());
                if (isBurnerActive()) {
                    playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1, 1);
                }
                stack.damage(1, player, p -> p.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
                playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1, 1);
                return ActionResult.SUCCESS;
            }

            if (stack.isEmpty() && isBurnerActive()) {
                setBoostTicks(50);
            }
        }

        if (stack.isOf(UItems.LARGE_BALLOON) && !hasBalloon()) {
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            setHasBalloon(true);
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(Items.LANTERN) && !hasBurner()) {
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 0.2F, 1);
            setHasBurner(true);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public void pushAway(Entity entity) {

    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (hasBalloon()) {
            return getBoundingBox().union(getBalloonBoundingBox());
        }
        return getBoundingBox();
    }

    protected Box getInteriorBoundingBox() {
        return getBoundingBox().contract(0.7, 0, 0.7);
    }

    protected Box getBalloonBoundingBox() {
        return getBoundingBox().offset(0.125, 11, 0).expand(2.25, 0, 2);
    }

    @Override
    public void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {

        Box box = getBoundingBox().expand(0.3, 0, 0.3);

        double wallheight = box.maxY + 0.7;
        double wallThickness = 0.7;

        // front left (next to door)
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness + 0.2, wallheight, box.minZ + wallThickness)));
        // front right (next to door)
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness - 0.2, box.minY, box.minZ, box.maxX, wallheight, box.minZ + wallThickness)));

        // back
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.maxZ - wallThickness, box.maxX, wallheight, box.maxZ)));

        // left
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness, box.minY, box.minZ, box.maxX, wallheight, box.maxZ)));
        // right
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness, wallheight, box.maxZ)));

        // top of balloon
        if (hasBalloon()) {
            output.accept(VoxelShapes.cuboid(
                    getBoundingBox().offset(0.12, 7.5, 0.12).expand(2.4, 3.5, 2.4)
            ));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setHasBalloon(compound.getBoolean("hasBalloon"));
        setHasBurner(compound.getBoolean("hasBurner"));
        setBurnerActive(compound.getBoolean("burnerActive"));
        setBoostTicks(compound.getInt("boostTicks"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("hasBalloon", hasBalloon());
        compound.putBoolean("hasBurner", hasBurner());
        compound.putBoolean("burnerActive", isBurnerActive());
        compound.putInt("boostTicks", getBoostTicks());
    }

    static Vec3d getWind(World world, BlockPos pos) {
        return Vec3d.ofCenter(pos).normalize().multiply(1, 0, 1).multiply(0.002);//.multiply((world.getRandom().nextFloat() - 0.5) * 0.2);
    }
}









