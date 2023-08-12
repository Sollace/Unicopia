package com.minelittlepony.unicopia.entity;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.item.HotAirBalloonItem;
import com.minelittlepony.unicopia.server.world.WeatherConditions;

public class AirBalloonEntity extends FlyingEntity implements EntityCollisions.ComplexCollidable, MultiBoundingBoxEntity {
    private static final byte HAS_BURNER = 2;
    private static final byte BURNER_ACTIVE = 4;
    private static final TrackedData<Integer> BURNER_FLAGS = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> BOOSTING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> INFLATION = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> BASKET_TYPE = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> BALLOON_DESIGN = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private boolean prevBoosting;
    private int prevInflation;
    private Vec3d oldPosition = Vec3d.ZERO;
    private Vec3d manualVelocity = Vec3d.ZERO;

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
        intersectionChecked = true;
        setPersistent();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(BURNER_FLAGS, 0);
        dataTracker.startTracking(BOOSTING, 0);
        dataTracker.startTracking(INFLATION, 0);
        dataTracker.startTracking(BASKET_TYPE, 0);
        dataTracker.startTracking(BALLOON_DESIGN, 0);
    }

    public BoatEntity.Type getBasketType() {
        return BoatEntity.Type.getType(dataTracker.get(BASKET_TYPE));
    }

    public void setBasketType(BoatEntity.Type type) {
        dataTracker.set(BASKET_TYPE, type.ordinal());
    }

    public BalloonDesign getDesign() {
        return BalloonDesign.getType(dataTracker.get(BALLOON_DESIGN));
    }

    public void setDesign(BalloonDesign design) {
        dataTracker.set(BALLOON_DESIGN, design.ordinal());
    }

    public boolean hasBalloon() {
        return getDesign() != BalloonDesign.NONE;
    }

    public boolean hasBurner() {
        return getFlag(HAS_BURNER);
    }

    public void setHasBurner(boolean hasBurner) {
        setFlag(HAS_BURNER, hasBurner);
    }

    public float getInflation(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevInflation, getInflation()) / (float)getMaxInflation();
    }

    private void setInflation(int inflation) {
        dataTracker.set(INFLATION, MathHelper.clamp(inflation, 0, getMaxInflation()));
    }

    private int getInflation() {
        return dataTracker.get(INFLATION);
    }

    protected int getMaxInflation() {
        return 100;
    }

    public boolean isBurnerActive() {
        return hasBalloon() && getFlag((byte)(HAS_BURNER | BURNER_ACTIVE));
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
        return (dataTracker.get(BURNER_FLAGS).intValue() & flag) == flag;
    }

    private void setFlag(byte flag, boolean val) {
        int v = dataTracker.get(BURNER_FLAGS);
        dataTracker.set(BURNER_FLAGS, val ? (v | flag) : (v & ~flag));
    }

    private boolean isAirworthy() {
        return hasBalloon() && isBurnerActive();
    }

    @Override
    public List<Box> getBoundingBoxes() {
        if (hasBalloon() && getInflation(1) > 0.999F) {
            return List.of(getInteriorBoundingBox(), getBalloonBoundingBox());
        }
        return List.of(getInteriorBoundingBox());
    }

    @Override
    public void tick() {
        this.shouldSave();
        setAir(getMaxAir());
        int boostTicks = getBoostTicks();

        int inflation = getInflation();
        prevInflation = inflation;

        if (boostTicks > 0) {
            boostTicks--;
            if (inflation < getMaxInflation()) {
                boostTicks--;
            }
            setBoostTicks(boostTicks);
        }

        boolean boosting = boostTicks > 0;

        if (hasBurner() && isBurnerActive()) {
            if (inflation < getMaxInflation()) {
                inflation++;
                if (boosting) {
                    inflation++;
                }
                setInflation(inflation);
            }
        } else {
            if (inflation < getMaxInflation() && inflation > 0) {
                setInflation(--inflation);
            }
        }

        addVelocity(0, isBurnerActive() && inflation >= getMaxInflation() ? 0.005 : -0.013, 0);
        addVelocity(manualVelocity.multiply(0.1));
        manualVelocity = manualVelocity.multiply(0.9);

        if (!isAirworthy() && isSubmergedInWater()) {
            setVelocity(getVelocity().multiply(0.9, 0.4, 0.9).add(0, 0.02, 0));
        }

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
        } else if (inflation >= getMaxInflation()) {
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

        if (getFireTicks() > 0) {
            setFireTicks(1);
        }

        updatePassengers();
        super.tick();
        setBoundingBox(MultiBox.of(getBoundingBox(), getBoundingBoxes()));
    }

    private void updatePassengers() {
        for (Box box : getBoundingBoxes()) {
            for (Entity e : getWorld().getOtherEntities(this, box.expand(getVelocity().length()).expand(0, 0.5, 0))) {
                updatePassenger(e, box, e.getY() > getY() + 3);
            }
        }
    }

    private void updatePassenger(Entity e, Box box, boolean inBalloon) {

        double height = box.getYLength();

        if (height < 3 || e.getBoundingBox().minY > box.minY + height / 2D) {
            if (getVelocity().y > 0 && e.getBoundingBox().minY < box.maxY + 0.02) {
                e.setPos(e.getX(), box.maxY, e.getZ());
            }
            if (getVelocity().y < 0 && e.getBoundingBox().minY > box.maxY) {
                e.setPos(e.getX(), box.maxY, e.getZ());
            }
        }

        if (manualVelocity.length() > 0.01 || getVelocity().length() > 0.3) {
            e.setVelocity(e.getVelocity().multiply(0.1, 0.5, 0.1));
        }

        if (getVelocity().y < 0) {
            e.addVelocity(0, getVelocity().y, 0);
            Living.updateVelocity(e);
        }

        if (inBalloon && !e.isSneaky() && Math.abs(e.getVelocity().y) > 0.079) {
            e.setVelocity(e.getVelocity().multiply(1, e.getVelocity().y < 0 ? -0.9 : 1.2, 1).add(0, 0.8, 0));
            if (Math.abs(e.getVelocity().y) > 2) {
                e.setVelocity(e.getVelocity().x, MathHelper.clamp(e.getVelocity().y, -2, 2), e.getVelocity().z);
            }
        }

        Living.getOrEmpty(e).ifPresent(living -> {
            living.setSupportingEntity(this);
            living.setPositionOffset(e.getPos().subtract(oldPosition));
            living.updateRelativePosition(box);
        });

        if (getWorld().isClient) {
            if (e.distanceTraveled > ((EntityDuck)e).getNextStepSoundDistance()) {
                e.distanceTraveled--;
                e.playSound(inBalloon ? SoundEvents.BLOCK_WOOL_STEP : SoundEvents.BLOCK_BAMBOO_STEP, 0.5F, 1);
                if (!e.isSneaky()) {
                    getWorld().emitGameEvent(e, GameEvent.STEP, getBlockPos());
                }
            }
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (hitPos.y > (3 * getInflation(1))) {
            if (hasBalloon() && hasBurner()) {
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    setBurnerActive(!isBurnerActive());
                    if (isBurnerActive()) {
                        playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1, 1);
                    }
                    stack.damage(1, player, p -> p.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
                    playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1, 1);
                    getWorld().emitGameEvent(this, GameEvent.ENTITY_INTERACT, getBlockPos());
                    return ActionResult.SUCCESS;
                }

                if (stack.isEmpty() && Math.abs(hitPos.x) > 1 && Math.abs(hitPos.z) > 1) {
                    double xPush = Math.signum(hitPos.x);
                    double zPush = Math.signum(hitPos.z);
                    if (!getWorld().isClient) {
                        manualVelocity = manualVelocity.add(0.3 * xPush, 0, 0.3 * zPush);
                    }
                } else if (stack.isEmpty() && isBurnerActive()) {
                    setBoostTicks(50);
                    getWorld().emitGameEvent(this, GameEvent.ENTITY_INTERACT, getBlockPos());
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof HotAirBalloonItem balloon && !hasBalloon()) {
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            getWorld().emitGameEvent(this, GameEvent.ENTITY_INTERACT, getBlockPos());
            setDesign(HotAirBalloonItem.getDesign(getWorld(), stack));
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(Items.LANTERN) && !hasBurner()) {
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 0.2F, 1);
            getWorld().emitGameEvent(this, GameEvent.ENTITY_INTERACT, getBlockPos());
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
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }


    @Override
    public Box getVisibilityBoundingBox() {
        if (hasBalloon()) {
            return MultiBox.unbox(getBoundingBox()).union(getBalloonBoundingBox());
        }
        return MultiBox.unbox(getBoundingBox());
    }

    protected Box getInteriorBoundingBox() {
        Box box = MultiBox.unbox(getBoundingBox());
        return box.withMinY(box.minY - 0.2).contract(0.2, 0, 0.2);
    }

    protected Box getBalloonBoundingBox() {
        float inflation = getInflation(1);
        return MultiBox.unbox(getBoundingBox())
                .offset(0.125, 7.3 * inflation, 0.125)
                .expand(2.25, 3.7 * inflation, 2.25);
    }

    @Override
    public void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {

        Box box = MultiBox.unbox(getBoundingBox()).expand(0.3, 0, 0.3);

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
            output.accept(VoxelShapes.cuboid(getBalloonBoundingBox()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setBasketType(BoatEntity.Type.getType(compound.getString("basketType")));
        setDesign(BalloonDesign.getType(compound.getString("design")));
        setHasBurner(compound.getBoolean("hasBurner"));
        setBurnerActive(compound.getBoolean("burnerActive"));
        setBoostTicks(compound.getInt("boostTicks"));
        prevInflation = compound.getInt("inflationAmount");
        setInflation(prevInflation);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putString("design", getDesign().asString());
        compound.putString("basket", getBasketType().asString());
        compound.putBoolean("hasBurner", hasBurner());
        compound.putBoolean("burnerActive", isBurnerActive());
        compound.putInt("boostTicks", getBoostTicks());
        compound.putInt("inflationAmount", getInflation());
    }

    @SuppressWarnings("deprecation")
    public enum BalloonDesign implements StringIdentifiable {
        NONE,
        LUNA;

        public static final StringIdentifiable.Codec<BalloonDesign> CODEC = StringIdentifiable.createCodec(BalloonDesign::values);
        private static final IntFunction<BalloonDesign> BY_ID = ValueLists.<BalloonDesign>createIdToValueFunction(Enum::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return name;
        }

        public static BalloonDesign getType(int type) {
            return BY_ID.apply(type);
        }

        public static BalloonDesign getType(String name) {
            return CODEC.byId(name, LUNA);
        }
    }
}









