package com.minelittlepony.unicopia.entity.mob;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.item.BasketItem;
import com.minelittlepony.unicopia.item.HotAirBalloonItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.terraformersmc.terraform.boat.api.TerraformBoatType;

public class AirBalloonEntity extends MobEntity implements EntityCollisions.ComplexCollidable, MultiBoundingBoxEntity, MagicImmune, EquineContext {
    private static final TrackedData<Boolean> ASCENDING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BOOSTING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> INFLATION = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> BASKET_TYPE = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> BALLOON_DESIGN = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final byte STATUS_BURNER_INTERACT = (byte)105;

    private static final Predicate<Entity> RIDER_PREDICATE = EntityPredicates.EXCEPT_SPECTATOR.and(e -> {
        return !(e instanceof PlayerEntity p && p.getAbilities().flying);
    });

    private boolean prevBoosting;
    private int prevInflation;
    private Vec3d manualVelocity = Vec3d.ZERO;

    private int maxFuel = 10000;
    private int fuel;

    private final Animatable[] sandbags = IntStream.range(0, 5).mapToObj(Animatable::new).toArray(Animatable[]::new);
    private final Animatable burner = new Animatable(5);

    private double prevXDelta;
    private double xDelta;
    private double prevZDelta;
    private double zDelta;

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
        intersectionChecked = true;
        setPersistent();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(ASCENDING, false);
        dataTracker.startTracking(BOOSTING, 0);
        dataTracker.startTracking(INFLATION, 0);
        dataTracker.startTracking(BASKET_TYPE, BasketType.DEFAULT.id().toString());
        dataTracker.startTracking(BALLOON_DESIGN, 0);
    }

    public BasketType getBasketType() {
        return BasketType.of(dataTracker.get(BASKET_TYPE));
    }

    public void setBasketType(BasketType type) {
        dataTracker.set(BASKET_TYPE, type.id().toString());
    }

    public BalloonDesign getDesign() {
        return BalloonDesign.getType(dataTracker.get(BALLOON_DESIGN));
    }

    public void setDesign(BalloonDesign design) {
        dataTracker.set(BALLOON_DESIGN, design.ordinal());
    }

    public Animatable getSandbag(int index) {
        return sandbags[MathHelper.clamp(index, 0, sandbags.length - 1)];
    }

    public Animatable getBurner() {
        return burner;
    }

    public boolean hasBalloon() {
        return getDesign() != BalloonDesign.NONE;
    }

    public boolean hasBurner() {
        return getHandItems() != null && !getStackInHand(Hand.MAIN_HAND).isEmpty();
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

    public boolean isAscending() {
        return hasBalloon() && dataTracker.get(ASCENDING);
    }

    public void setAscending(boolean ascending) {
        dataTracker.set(ASCENDING, ascending);
    }

    public int getBoostTicks() {
        return dataTracker.get(BOOSTING);
    }

    protected void setBoostTicks(int ticks) {
        dataTracker.set(BOOSTING, ticks);
    }

    private boolean isAirworthy() {
        return hasBalloon() && hasBurner() && getInflation() >= getMaxInflation();
    }

    public float getXVelocity(float tickDelta) {
        return (float)MathHelper.lerp(tickDelta, prevXDelta, xDelta);
    }

    public float getZVelocity(float tickDelta) {
        return (float)MathHelper.lerp(tickDelta, prevZDelta, zDelta);
    }

    @Override
    public void tick() {
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

        if (hasBurner() && isAscending()) {
            if (inflation < getMaxInflation()) {
                inflation++;
                if (boosting) {
                    inflation++;
                }
                setInflation(inflation);
            }

            if (fuel > -6 && age % 2 == 0) {
                fuel -= boosting ? 50 : 1;
                if (fuel <= -6) {
                    setBoostTicks(0);
                    setAscending(false);
                }
            }
        } else {
            if (inflation < getMaxInflation() && inflation > 0) {
                setInflation(--inflation);
            }
        }

        if (isAirworthy()) {
            addVelocity(0, isAscending() && inflation >= getMaxInflation() ? 0.005 : -0.013, 0);
            addVelocity(manualVelocity.multiply(this.getVelocity().y > 0.01F ? 0.1 : 0.01));
        }
        manualVelocity = manualVelocity.multiply(0.9);

        if (!(hasBalloon() && isAscending()) && isSubmergedInWater()) {
            setVelocity(getVelocity().multiply(0.9, 0.4, 0.9).add(0, 0.02, 0));
        }

        Random rng = getWorld().random;

        if (getWorld().isClient()) {
            if (hasBurner() && isAscending()) {
                Vec3d burnerPos = getPos().add(0, 3, 0);
                for (int i = 0; i < (boosting ? 6 : 1); i++) {
                    getWorld().addParticle(fuel <= 0
                                ? ParticleTypes.SMOKE
                                : getStackInHand(Hand.MAIN_HAND).isOf(Items.SOUL_LANTERN)
                                    ? ParticleTypes.SOUL_FIRE_FLAME
                                    : ParticleTypes.FLAME,
                            rng.nextTriangular(burnerPos.x, 0.25),
                            rng.nextTriangular(burnerPos.y, 1),
                            rng.nextTriangular(burnerPos.z, 0.25),
                            0,
                            (boosting ? 0.1 : 0),
                            0
                    );
                }
            }
        } else if (inflation >= getMaxInflation()) {
            if (hasBurner() && isAscending()) {
                addVelocity(WeatherConditions.getAirflow(getBlockPos(), getWorld()).multiply(0.2));
                setVelocity(getVelocity().multiply(0.3, 1, 0.3));
            }

            if (boosting) {
                addVelocity(0, 0.02, 0);
            }
        }

        if (boosting && !prevBoosting) {
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
        }

        if (isAscending() && age % 15 + rng.nextInt(5) == 0) {
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_BURNER_FIRE, 0.2F, 1);
            getWorld().emitGameEvent(null, GameEvent.FLAP, getBlockPos());
        }

        if (isLeashed()) {
            Vec3d leashPost = getHoldingEntity().getPos();
            Vec3d pos = getPos();

            if (leashPost.distanceTo(pos) >= 5) {
                Vec3d newVel = leashPost.subtract(pos).multiply(0.01);
                if (isAirworthy()) {
                    setVelocity(newVel.lengthSquared() < 0.0001 ? Vec3d.ZERO : newVel);
                } else {
                    setVelocity(getVelocity().multiply(0.9).add(newVel));
                }
            }
        }

        prevBoosting = boosting;

        if (getFireTicks() > 0) {
            setFireTicks(1);
        }

        for (Animatable bag : sandbags) {
            bag.tick();
        }
        burner.tick();

        super.tick();

        prevXDelta = xDelta;
        prevZDelta = zDelta;
        xDelta = getX() - prevX;
        zDelta = getZ() - prevZ;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (hasBalloon() && hasBurner()) {

            if (getBurnerBoundingBox().expand(0.7).contains(getPos().add(hitPos))) {
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    setAscending(!isAscending());
                    if (isAscending()) {
                        playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
                    }
                    stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                    playSound(USounds.Vanilla.ITEM_FLINTANDSTEEL_USE, 1, 1);
                    if (!player.isSneaky()) {
                        getWorld().emitGameEvent(player, GameEvent.ENTITY_INTERACT, getBlockPos());
                    }
                    burner.setPulling();
                    return ActionResult.SUCCESS;
                }

                if (stack.isEmpty() && isAscending()) {
                    setBoostTicks(50);
                    playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
                    burner.setPulling();
                    if (!player.isSneaky()) {
                        getWorld().emitGameEvent(player, GameEvent.ENTITY_INTERACT, getBlockPos());
                    }
                    return ActionResult.SUCCESS;
                }
            }

            if (getInflation(1) >= 1) {
                int xPush = (int)Math.signum(hitPos.x);
                int zPush = (int)Math.signum(hitPos.z);

                Vec3d absHitPos = getPos().add(hitPos);

                if (stack.isEmpty() && MultiBox.unbox(getBoundingBox()).expand(0.5, 1, 0.5).offset(2 * xPush, 3, 2 * zPush).contains(absHitPos)) {
                    if (!getWorld().isClient) {
                        manualVelocity = manualVelocity.add(1.7 * xPush, 0, 1.7 * zPush);
                    }
                    getWorld().playSound(null, getX() + hitPos.getX(), getY() + hitPos.getY(), getZ() + hitPos.getZ(), USounds.Vanilla.ENTITY_LEASH_KNOT_PLACE, getSoundCategory(), 1, 1);
                    if (!player.isSneaky()) {
                        getWorld().emitGameEvent(player, GameEvent.ENTITY_INTERACT, getBlockPos());
                    }

                    Vec3d interactCoordinate = new Vec3d(xPush, 0, zPush)
                            .rotateY((180 + getHorizontalFacing().asRotation()) * MathHelper.RADIANS_PER_DEGREE)
                    ;

                    getSandbag(MathHelper.clamp((int)interactCoordinate.getX(), 0, 1) + MathHelper.clamp((int)interactCoordinate.getZ(), 0, 1) * 2).setPulling();

                    return ActionResult.SUCCESS;
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
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY, 1, 1);
            if (!player.isSneaky()) {
                getWorld().emitGameEvent(player, GameEvent.EQUIP, getBlockPos());
            }
            setDesign(HotAirBalloonItem.getDesign(getWorld(), stack));
            if (hasBurner() && hasBalloon()) {
                UCriteria.CONSTRUCT_BALLOON.trigger(player);
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isIn(ConventionalItemTags.SHEARS) && hasBalloon()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            setDesign(BalloonDesign.NONE);
            dropItem(UItems.GIANT_BALLOON);
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY, 1, 1);
            if (!player.isSneaky()) {
                getWorld().emitGameEvent(player, GameEvent.EQUIP, getBlockPos());
            }
            return ActionResult.SUCCESS;
        }

        if ((stack.isOf(Items.LANTERN) || stack.isOf(Items.SOUL_LANTERN)) && !hasBurner()) {
            setStackInHand(Hand.MAIN_HAND, stack.copyWithCount(1));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_BURNER, 0.2F, 1);
            if (!player.isSneaky()) {
                getWorld().emitGameEvent(player, GameEvent.EQUIP, getBlockPos());
            }
            if (hasBurner() && hasBalloon()) {
                UCriteria.CONSTRUCT_BALLOON.trigger(player);
            }
            return ActionResult.SUCCESS;
        }

        if (hasBurner()) {
            int fuel = FurnaceBlockEntity.createFuelTimeMap().getOrDefault(stack.getItem(), 0);
            if (fuel > 0) {
                if (this.fuel < maxFuel) {
                    if (this.fuel < 0) {
                        this.fuel = fuel;
                    } else {
                        this.fuel += fuel;
                    }
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    burner.setPulling();
                    playSound(USounds.Vanilla.ENTITY_VILLAGER_YES, 1, 1);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    protected void dropInventory() {
        ItemStack lantern = getStackInHand(Hand.MAIN_HAND);
        setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        dropStack(lantern);
        dropStack(getPickBlockStack());
    }

    @Override
    public ItemStack getPickBlockStack() {
        return asItem().getDefaultStack();
    }

    public Item asItem() {
        return Objects.requireNonNull(BasketItem.REGISTRY.get(getBasketType()));
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (entity instanceof AirBalloonEntity) {
            super.pushAwayFrom(entity);
        }
    }

    @Override
    public void pushAway(Entity entity) {
        if (entity instanceof AirBalloonEntity) {
            super.pushAway(entity);
        }
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    public Race getSpecies() {
        return Race.UNSET;
    }

    @Override
    public SoundEvent getWalkedOnSound(double y) {
        if (y >= getBalloonBoundingBox().minY) {
            return USounds.ENTITY_HOT_AIR_BALLOON_STEP;
        }
        return USounds.ENTITY_HOT_AIR_BALLOON_BASKET_STEP;
    }

    @Override
    public boolean collidesWithClouds() {
        return isAirworthy() && !isAscending();
    }


    @Override
    public float getCloudWalkingStrength() {
        return isAirworthy() ? 2 : 0;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!isAirworthy()) {
            super.travel(movementInput);
        } else {
            final float speed = 0.02F;
            final float momentum = 0.91F;
            if (isLogicalSideForUpdatingMovement()) {
                if (isTouchingWater() || isInLava()) {
                    updateVelocity(speed, movementInput);
                    move(MovementType.SELF, getVelocity());
                    setVelocity(getVelocity().multiply(isTouchingWater() ? 0.8 : 0.5));
                } else {
                    float slipperyness = (isOnGround() ? getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() : 1) * momentum;
                    float drag = isOnGround() ? 0.1F * (0.16277137F / (slipperyness * slipperyness * slipperyness)) : speed;
                    updateVelocity(drag, movementInput);
                    move(MovementType.SELF, getVelocity());
                    setVelocity(getVelocity().multiply(slipperyness));
                }
            } else {
                Map<Box, List<Entity>> collidingEntities = getCollidingEntities(getBoundingBoxes().stream());

                for (Map.Entry<Box, List<Entity>> passengers : collidingEntities.entrySet()) {
                    for (Entity passenger : passengers.getValue()) {
                        Living<?> living = Living.living(passenger);
                        if (living != null) {
                            living.getTransportation().setVehicle(this);
                        }

                    }
                }
            }
            updateLimbs(false);
        }
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    protected Box calculateBoundingBox() {
        List<Box> boxes = getBoundingBoxes();
        Box box = super.calculateBoundingBox();

        if (hasBalloon() && getInflation(1) > 0.999F) {
            double horScale = -0.5;
            // x+ z+
            boxes.add(box.expand(horScale, 1, horScale).offset(2, 3, 2));
            // x- z+
            boxes.add(box.expand(horScale, 1, horScale).offset(-2, 3, 2));

            // x+ z-
            boxes.add(box.expand(horScale, 1, horScale).offset(2, 3, -2));
            // x- z-
            boxes.add(box.expand(horScale, 1, horScale).offset(-2, 3, -2));
        }
        if (hasBurner()) {
            boxes.add(getBurnerBoundingBox());
        }

        return MultiBox.of(box, boxes);
    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (hasBalloon()) {
            return getBalloonBoundingBox().withMinY(getY());
        }
        return getInteriorBoundingBox();
    }

    protected Box getInteriorBoundingBox() {
        Box box = MultiBox.unbox(getBoundingBox());
        return box.withMinY(box.minY - 0.05).contract(0.15, 0, 0.15);
    }

    protected Box getBalloonBoundingBox() {
        float inflation = getInflation(1);
        return MultiBox.unbox(getBoundingBox())
                .offset(0.125, 7.3 * inflation, 0.125)
                .expand(2.25, 3.7 * inflation, 2.25);
    }

    protected Box getBurnerBoundingBox() {
        float inflation = getInflation(1);
        float horScale = -0.9F;
        return MultiBox.unbox(getBoundingBox())
                .offset(0, 2.6F * inflation + 0.4F, 0)
                .expand(horScale, 0.4, horScale);
    }

    @Override
    public List<Box> getGravityZoneBoxes() {
        Box balloon = getBalloonBoundingBox().expand(0.001);
        Box interior = getInteriorBoundingBox().expand(0.001);
        if (hasBalloon() && getInflation(1) > 0.999F) {
            return List.of(
                    // interior - basket to top of balloon
                    interior.withMaxY(balloon.minY).withMinY(interior.maxY),
                    // balloon
                    balloon.withMaxY(balloon.maxY + 0.5).withMinY(balloon.maxY)
            );
        }
        return List.of(interior.withMaxY(balloon.minY).withMinY(interior.maxY));
    }

    @Override
    public List<Box> getBoundingBoxes() {
        List<Box> boxes = new ArrayList<>();
        Box box = getInteriorBoundingBox();
        Box mainBox = MultiBox.unbox(getBoundingBox());

        boxes.add(box);

        double wallheight = box.maxY + 0.72;
        double wallThickness = 0.3;
        double halfDoorWidth = 0.5;

        if (!getBasketType().isOf(BoatEntity.Type.BAMBOO)) {

            // front left (next to door)
            boxes.add(new Box(mainBox.minX, mainBox.minY, mainBox.minZ, mainBox.minX + wallThickness + halfDoorWidth, wallheight, box.minZ + wallThickness));
            // front right (next to door)
            boxes.add(new Box(mainBox.maxX - wallThickness - halfDoorWidth, mainBox.minY, mainBox.minZ, mainBox.maxX, wallheight, box.minZ + wallThickness));

            // back
            boxes.add(new Box(mainBox.minX, mainBox.minY, box.maxZ - wallThickness, mainBox.maxX, wallheight, mainBox.maxZ));

            // left
            boxes.add(new Box(box.maxX - wallThickness, mainBox.minY, mainBox.minZ, mainBox.maxX, wallheight, mainBox.maxZ));
            // right
            boxes.add(new Box(mainBox.minX, mainBox.minY, mainBox.minZ, box.minX + wallThickness, wallheight, mainBox.maxZ));
        }

        if (hasBalloon() && getInflation(1) >= 1) {
            Box balloonBox = getBalloonBoundingBox();
            boxes.add(balloonBox.withMinY(balloonBox.maxY - 0.5));
            boxes.add(balloonBox.withMaxX(balloonBox.minX + 0.5));
            boxes.add(balloonBox.withMinX(balloonBox.maxX - 0.5));
            boxes.add(balloonBox.withMaxZ(balloonBox.minZ + 0.5));
            boxes.add(balloonBox.withMinZ(balloonBox.maxZ - 0.5));

            boxes.add(balloonBox.withMaxX(balloonBox.minX + 2).withMaxY(balloonBox.minY + 0.2));
            boxes.add(balloonBox.withMinX(balloonBox.maxX - 2).withMaxY(balloonBox.minY + 0.2));
            boxes.add(balloonBox.withMaxZ(balloonBox.minZ + 2).withMaxY(balloonBox.minY + 0.2));
            boxes.add(balloonBox.withMinZ(balloonBox.maxZ - 2).withMaxY(balloonBox.minY + 0.2));
        }

        float yaw = (180 - getHorizontalFacing().asRotation()) * MathHelper.RADIANS_PER_DEGREE;
        if (yaw != 0) {
            Vec3d center = getPos();
            for (int i = 0; i < boxes.size(); i++) {
                Box b = boxes.get(i);
                Vec3d min = new Vec3d(b.minX, b.minY, b.minZ).subtract(center).rotateY(yaw).add(center);
                Vec3d max = new Vec3d(b.maxX, b.maxY, b.maxZ).subtract(center).rotateY(yaw).add(center);
                boxes.set(i, new Box(min.x, min.y, min.z, max.x, max.y, max.z));
            }
        }

        return boxes;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        Vec3d oldPos = this.getPos();
        List<Box> boundingBoxes = getGravityZoneBoxes();
        super.move(movementType, movement);
        if (movementType == MovementType.SELF) {
            Vec3d actualMovement = getPos().subtract(oldPos);
            Map<Box, List<Entity>> collidingEntities = getCollidingEntities(
                    boundingBoxes.stream().map(box -> box.stretch(actualMovement))
            );

            for (Map.Entry<Box, List<Entity>> passengers : collidingEntities.entrySet()) {
                for (Entity passenger : passengers.getValue()) {
                    movePassenger(passenger, actualMovement);
                }
            }
        }
    }

    private void movePassenger(Entity passenger, Vec3d movement) {
        if (!EntityPredicates.EXCEPT_SPECTATOR.test(passenger)) {
            return;
        }

        Living<?> living = Living.living(passenger);
        if (living != null) {
            if (living.getPhysics().isGravityNegative()) {
                movement = movement.multiply(1, -1, 1);
            }
            living.getTransportation().setVehicle(this);
        }

        List<VoxelShape> shapes = new ArrayList<>();
        getCollissionShapes(ShapeContext.of(passenger), shapes::add);
        movement = Entity.adjustMovementForCollisions(passenger, movement, passenger.getBoundingBox(), getWorld(), shapes);

        passenger.setPosition(passenger.getPos().add(movement));
        passenger.updateTrackedPosition(passenger.getX(), passenger.getY(), passenger.getZ());
    }

    @Override
    public Map<Box, List<Entity>> getCollidingEntities(Stream<Box> boundingBoxes) {
        return boundingBoxes.collect(Collectors.toMap(Function.identity(), box -> {
            return getWorld().getOtherEntities(this, box.expand(0.001).stretch(getVelocity().multiply(1)), RIDER_PREDICATE).stream().distinct().toList();
        }));
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }


    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setBasketType(BasketType.of(compound.getString("basket")));
        setDesign(BalloonDesign.getType(compound.getString("design")));
        setAscending(compound.getBoolean("burnerActive"));
        setBoostTicks(compound.getInt("boostTicks"));
        prevInflation = compound.getInt("inflationAmount");
        setInflation(prevInflation);
        fuel = MathHelper.clamp(compound.getInt("fuel"), 0, maxFuel);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putString("design", getDesign().asString());
        compound.putString("basket", getBasketType().id().toString());
        compound.putBoolean("burnerActive", isAscending());
        compound.putInt("boostTicks", getBoostTicks());
        compound.putInt("inflationAmount", getInflation());
        compound.putInt("fuel", fuel);
    }

    @Override
    public void handleStatus(byte status) {
        if (status >= 100 && status < 100 + sandbags.length) {
            getSandbag(status % sandbags.length).setPulling();
        } else if (status == STATUS_BURNER_INTERACT) {
        } else {
            super.handleStatus(status);
        }
    }

    static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public class Animatable {
        private final int id;
        private int pullTicks;
        private int prevPullTicks;
        private boolean pulling;

        public Animatable(int id) {
            this.id = id;
        }

        public void setPulling() {
            if (!getWorld().isClient) {
                getWorld().sendEntityStatus(AirBalloonEntity.this, (byte)(100 + id));
            }
            pulling = true;
        }

        public float getPullProgress(float tickDelta) {
            return MathHelper.lerp(tickDelta, (float)prevPullTicks, pullTicks) / 6F;
        }

        public void tick() {
            prevPullTicks = pullTicks;
            if (pulling && pullTicks < 6) {
                pullTicks++;
            } else {
                pulling = false;
                if (pullTicks > 0) {
                    pullTicks--;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public enum BalloonDesign implements StringIdentifiable {
        NONE,
        LUNA,
        DAWN,
        EQUALITY,
        STORM,
        TALE;

        public static final EnumCodec<BalloonDesign> CODEC = StringIdentifiable.createCodec(BalloonDesign::values);
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

    public record BasketType(Identifier id, @Nullable BoatEntity.Type boatType) {
        private static final Map<Identifier, BasketType> REGISTRY = new HashMap<>();
        public static final BasketType DEFAULT = of(BoatEntity.Type.OAK);
        static {
            Arrays.stream(BoatEntity.Type.values()).forEach(BasketType::of);
        }

        public boolean isOf(BoatEntity.Type boatType) {
            return this.boatType == boatType;
        }

        public static BasketType of(String name) {
            Identifier id = name == null || name.isEmpty() ? null : Identifier.tryParse(name);
            if (id == null) {
                return of(BoatEntity.Type.OAK);
            }
            return REGISTRY.get(id);
        }

        public static BasketType of(BoatEntity.Type boatType) {
            return REGISTRY.computeIfAbsent(new Identifier(boatType.asString()), id -> new BasketType(id, boatType));
        }

        public static BasketType of(RegistryKey<TerraformBoatType> id) {
            return REGISTRY.computeIfAbsent(id.getValue(), i -> new BasketType(i, null));
        }
    }
}









