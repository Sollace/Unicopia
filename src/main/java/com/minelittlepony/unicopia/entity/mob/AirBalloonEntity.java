package com.minelittlepony.unicopia.entity.mob;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Transportation;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.item.BasketItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public class AirBalloonEntity extends FlyingVehicleEntity {
    private static final TrackedData<Boolean> ASCENDING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BOOSTING = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> INFLATION = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> BASKET_TYPE = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> BALLOON_DESIGN = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final Codec<List<ItemStack>> FUEL_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    private static final int BASKET_GRID_SIZE = 2;
    private static final int TOP_GRID_SIZE = 4;

    public static final byte STATUS_BURNER_INTERACT = (byte)105;

    private static final Predicate<Entity> RIDER_PREDICATE = EntityPredicates.EXCEPT_SPECTATOR.and(e -> {
        return !(e instanceof PlayerEntity p && p.getAbilities().flying);
    }).and(e -> !(e instanceof FlyingVehicleEntity));

    private boolean prevBoosting;
    private int prevInflation;
    private Vec3d manualVelocity = Vec3d.ZERO;
    private Vec3d lastPos = Vec3d.ZERO;

    private int maxFuel = 10000;
    private int activeFuel;
    private List<ItemStack> fuelItems = new ArrayList<>();

    private final Animatable[] sandbags = IntStream.range(0, 5).mapToObj(Animatable::new).toArray(Animatable[]::new);
    private final Animatable burner = new Animatable(5);

    private double prevXDelta;
    private double xDelta;
    private double prevZDelta;
    private double zDelta;

    private List<Transportation<Entity>> passengers = List.of();

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
        intersectionChecked = true;
        setPersistent();
    }

    @Override
    protected void initDataTracker(Builder builder) {
        super.initDataTracker(builder);
        builder.add(ASCENDING, false);
        builder.add(BOOSTING, 0);
        builder.add(INFLATION, 0);
        builder.add(BASKET_TYPE, BasketType.DEFAULT.id().toString());
        builder.add(BALLOON_DESIGN, 0);
    }

    @Override
    protected int getSeatCount() {
        return (BASKET_GRID_SIZE * BASKET_GRID_SIZE) + (TOP_GRID_SIZE * TOP_GRID_SIZE);
    }

    @Override
    protected Vec3d getSeatPosition(int seatIndex) {
        double y = 0.2;
        double alignment = -0.5;
        int gridSize = BASKET_GRID_SIZE;
        if (seatIndex >= 4) {
            seatIndex -= 4;
            y = 11.125;
            alignment = -1.75;
            gridSize = TOP_GRID_SIZE;
        }
        return new Vec3d(alignment + seatIndex % gridSize, y, alignment + seatIndex / gridSize);
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

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return equipment == null ? ItemStack.EMPTY : super.getEquippedStack(slot);
    }

    public boolean hasBalloon() {
        return getDesign() != BalloonDesign.NONE;
    }

    public boolean hasBurner() {
        return hasStackEquipped(EquipmentSlot.MAINHAND);
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
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_BAMBOO_WOOD_BREAK;
    }

    @Override
    public void tick() {
        updatePassengers();

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

        if (!getWorld().isClient()) {
            if (hasBurner() && isAscending()) {
                if (inflation < getMaxInflation()) {
                    inflation++;
                    if (boosting) {
                        inflation++;
                    }
                    setInflation(inflation);
                }

                if (getWorld() instanceof ServerWorld sw) {
                    if (activeFuel <= 0 && !fuelItems.isEmpty()) {
                        activeFuel = sw.getFuelRegistry().getFuelTicks(fuelItems.remove(0));
                    }
                }

                if (activeFuel > 0 && age % 2 == 0) {
                    activeFuel -= boosting ? 50 : 1;
                }

                if (activeFuel <= 0) {
                    setBoostTicks(0);
                    setAscending(false);
                }
            } else {
                if (inflation < getMaxInflation() && inflation > 0) {
                    setInflation(--inflation);
                }
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
                    getWorld().addParticleClient(activeFuel <= 0
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
            Vec3d leashPost = getLeashHolder().getPos();
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
        xDelta = 0;//getX() - lastX;
        zDelta = 0;//getZ() - lastZ;

        movePassengers(getPos().subtract(lastPos));
        lastPos = getPos();
    }

    @Override
    public void breakLongLeash() {
        Entity holder = getLeashHolder();
        float distance = distanceTo(holder);

        if (distance > 60.0) {
            detachLeash();
        } else {
            applyLeashElasticity(holder, distance);
            limitFallDistance();
        }
    }

    private void updatePassengers() {
        this.passengers.forEach(passenger -> passenger.setVehicle(null));
        this.passengers = getCollidingEntities(getGravityZoneBoxes().stream()).entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .map(Transportation::of)
                .filter(passenger -> passenger.setVehicle(this))
                .toList();
    }

    private void movePassengers(Vec3d positionChange) {
        if (isAirworthy() && !isOnGround()) {
            passengers.forEach(passenger -> {
                passenger.move(positionChange);
            });
        }
        passengers.forEach(passenger -> {
            passenger.tickRiding();
        });
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d relativePositionOffset, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (hasBalloon() && hasBurner()) {

            if (getBurnerBoundingBox(getBoundingBox()).expand(0.7).contains(getPos().add(relativePositionOffset))) {
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    setAscending(!isAscending());
                    if (isAscending()) {
                        playSound(USounds.ENTITY_HOT_AIR_BALLOON_BOOST, 1, 1);
                    }
                    stack.damage(1, player, getSlotForHand(hand));
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
                int xPush = (int)Math.signum(relativePositionOffset.x);
                int zPush = (int)Math.signum(relativePositionOffset.z);

                Vec3d absHitPos = getPos().add(relativePositionOffset);

                if (stack.isEmpty() && MultiBox.unbox(getBoundingBox()).expand(0.5, 1, 0.5).offset(2 * xPush, 3, 2 * zPush).contains(absHitPos)) {
                    if (!getWorld().isClient) {
                        manualVelocity = manualVelocity.add(1.7 * xPush, 0, 1.7 * zPush);
                    }
                    getWorld().playSound(null, getX() + relativePositionOffset.getX(), getY() + relativePositionOffset.getY(), getZ() + relativePositionOffset.getZ(), USounds.Vanilla.ENTITY_LEASH_KNOT_PLACE, getSoundCategory(), 1, 1);
                    if (!player.isSneaky()) {
                        getWorld().emitGameEvent(player, GameEvent.ENTITY_INTERACT, getBlockPos());
                    }

                    int sandbagId = MathHelper.clamp(-xPush, 0, 1) + MathHelper.clamp(-zPush, 0, 1) * 2;

                    getSandbag(sandbagId).setPulling();

                    return ActionResult.SUCCESS;
                }

                if (stack.isEmpty()) {
                    if (MultiBox.unbox(getBoundingBox()).expand(0.5, 1, 0.5).contains(absHitPos)) {
                        return player.startRiding(this) ? ActionResult.SUCCESS : ActionResult.FAIL;
                    }
                    Box balloonBox = getBalloonBoundingBox(getBoundingBox());
                    if (balloonBox.expand(0.5).withMinY(balloonBox.maxY - 0.25).contains(absHitPos)) {
                        return player.startRiding(this) ? ActionResult.SUCCESS : ActionResult.FAIL;
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.contains(UDataComponentTypes.BALLOON_DESIGN) && !hasBalloon()) {
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY.value(), 1, 1);
            if (!player.isSneaky()) {
                getWorld().emitGameEvent(player, GameEvent.EQUIP, getBlockPos());
            }
            setDesign(AirBalloonEntity.BalloonDesign.of(getWorld(), stack));
            if (hasBurner() && hasBalloon()) {
                UCriteria.CONSTRUCT_BALLOON.trigger(player);
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isIn(ConventionalItemTags.SHEAR_TOOLS) && hasBalloon()) {
            stack.damage(1, player, getSlotForHand(hand));
            if (getWorld() instanceof ServerWorld sw) {
                dropStack(sw, BalloonDesignComponent.set(UItems.GIANT_BALLOON.getDefaultStack(), new BalloonDesignComponent(getDesign(), true)));
            }
            setDesign(BalloonDesign.NONE);
            playSound(USounds.ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY.value(), 1, 1);
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

        if (hasBurner() && getWorld() instanceof ServerWorld sw) {
            int fuel = sw.getFuelRegistry().getFuelTicks(stack);
            if (fuel > 0) {
                if (fuelItems.size() < 64) {
                    fuelItems.add(stack.splitUnlessCreative(1, player));
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
    protected void dropInventory(ServerWorld world) {
        dropStack(world, getPickBlockStack());
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack lantern = getStackInHand(Hand.MAIN_HAND);
            setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            dropStack(world, lantern);
            if (hasBalloon()) {
                dropStack(world, BalloonDesignComponent.set(UItems.GIANT_BALLOON.getDefaultStack(), new BalloonDesignComponent(getDesign(), true)));
                setDesign(BalloonDesign.NONE);
            }
            fuelItems.forEach(s -> dropStack(world, s));
            fuelItems.clear();
        }
    }

    @Override
    public ItemStack getPickBlockStack() {
        return asItem().getDefaultStack();
    }

    public Item asItem() {
        return Objects.requireNonNull(BasketItem.REGISTRY.get(getBasketType()), "BasketItem.REGISTRY.get(" + getBasketType() + ") != null");
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
    public Optional<BlockState> getSteppingState(Box entityBox) {
        Box mainBox = getBoundingBox();
        if (getInteriorBoundingBox(mainBox).intersects(entityBox)) {
            return Optional.of(getBasketType().representative().getDefaultState());
        }

        if (hasBalloon() && getBalloonBoundingBox(mainBox).intersects(entityBox)) {
            return Optional.of(Blocks.WHITE_WOOL.getDefaultState());
        }

        return Optional.empty();
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
            updateLimbs(false);
        }
    }

    @Override
    protected Box calculateDefaultBoundingBox(Vec3d pos) {
        Box mainBox = super.calculateDefaultBoundingBox(pos);
        return MultiBox.of(mainBox, getBoundingBoxes(mainBox), getInteractionZones(mainBox));
    }

    public Box getInteriorBoundingBox(Box mainBox) {
        Box box = MultiBox.unbox(mainBox);
        return box.withMinY(box.minY - 0.5).contract(0.15, 0, 0.15);
    }

    public Box getBalloonBoundingBox(Box mainBox) {
        float inflation = getInflation(1);
        return MultiBox.unbox(mainBox)
                .offset(0.125, 7.3 * inflation, 0.125)
                .expand(2.25, 3.7 * inflation, 2.25);
    }

    protected Box getBurnerBoundingBox(Box mainBox) {
        float inflation = getInflation(1);
        float horScale = -0.9F;
        return MultiBox.unbox(mainBox)
                .offset(0, 2.6F * inflation + 0.4F, 0)
                .expand(horScale, 0.4, horScale);
    }

    @Override
    public List<Box> getGravityZoneBoxes() {
        Box mainBox = getBoundingBox();
        Box balloon = getBalloonBoundingBox(mainBox).expand(0.001).offset(-0.25, 0, 0);
        Box interior = getInteriorBoundingBox(mainBox).expand(0.001);
        if (hasBalloon() && getInflation(1) > 0.999F) {
            return List.of(
                    // interior - slim layer above the bottom of the basket
                    interior.withMaxY(interior.maxY + 0.75).withMinY(interior.minY - 0.25),
                    // balloon - slim layer on top of the balloon
                    balloon.withMaxY(balloon.maxY + 0.5).withMinY(balloon.maxY - 2)
            );
        }
        return List.of(interior.withMaxY(interior.maxY + 1.5).withMinY(interior.maxY - 2));
    }

    @Override
    public List<Box> getBoundingBoxes(Box mainBox) {
        mainBox = MultiBox.unbox(mainBox);
        List<Box> boxes = new ArrayList<>();
        Box box = getInteriorBoundingBox(mainBox);

        double wallheight = box.maxY + 0.72;
        double wallThickness = 0.15;
        double halfDoorWidth = 0.5;
        double balloonWallThickness = 0.25;

        if (!getBasketType().isOf(WoodType.BAMBOO)) {

            // front left (next to door)
            boxes.add(new Box(mainBox.minX + wallThickness + 0.15, mainBox.maxY, mainBox.minZ, mainBox.minX + wallThickness + halfDoorWidth, wallheight, box.minZ + wallThickness));
            // front right (next to door)
            boxes.add(new Box(mainBox.maxX - wallThickness - halfDoorWidth, mainBox.maxY, mainBox.minZ, mainBox.maxX - wallThickness - 0.15, wallheight, box.minZ + wallThickness));

            // back
            boxes.add(new Box(mainBox.minX + wallThickness + 0.15, mainBox.maxY, box.maxZ - wallThickness, mainBox.maxX - wallThickness - 0.15, wallheight, mainBox.maxZ));

            // left
            boxes.add(new Box(box.maxX - wallThickness, mainBox.maxY, mainBox.minZ, mainBox.maxX, wallheight, mainBox.maxZ));
            // right
            boxes.add(new Box(mainBox.minX, mainBox.maxY, mainBox.minZ, box.minX + wallThickness, wallheight, mainBox.maxZ));
        }

        if (hasBalloon() && getInflation(1) >= 1) {
            Box balloonBox = getBalloonBoundingBox(mainBox);
            boxes.add(balloonBox.withMinY(balloonBox.maxY - balloonWallThickness));
            boxes.add(balloonBox.withMaxX(balloonBox.minX + balloonWallThickness).withMaxY(balloonBox.maxY - balloonWallThickness).withMinZ(balloonBox.minZ + balloonWallThickness).withMaxZ(balloonBox.maxZ - balloonWallThickness));
            boxes.add(balloonBox.withMinX(balloonBox.maxX - balloonWallThickness).withMaxY(balloonBox.maxY - balloonWallThickness).withMinZ(balloonBox.minZ + balloonWallThickness).withMaxZ(balloonBox.maxZ - balloonWallThickness));
            boxes.add(balloonBox.withMaxZ(balloonBox.minZ + balloonWallThickness).withMaxY(balloonBox.maxY - balloonWallThickness));
            boxes.add(balloonBox.withMinZ(balloonBox.maxZ - balloonWallThickness).withMaxY(balloonBox.maxY - balloonWallThickness));
        }

        float yaw = (180 - getHorizontalFacing().getPositiveHorizontalDegrees()) * MathHelper.RADIANS_PER_DEGREE;
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

    private List<Box> getInteractionZones(Box mainBox) {
        List<Box> boxes = new ArrayList<>();
        Box box = getInteriorBoundingBox(mainBox);

        if (hasBalloon() && getInflation(1) >= 1) {
            double horScale = -0.8;
            double verScale = 0.2;
            double horOutset = 2.5;
            // x+ z+
            boxes.add(box.expand(horScale, verScale, horScale).offset(horOutset, 2.5, horOutset));
            // x- z+
            boxes.add(box.expand(horScale, verScale, horScale).offset(-horOutset, 2.5, horOutset));

            // x+ z-
            boxes.add(box.expand(horScale, verScale, horScale).offset(horOutset, 2.5, -horOutset));
            // x- z-
            boxes.add(box.expand(horScale, verScale, horScale).offset(-horOutset, 2.5, -horOutset));
        }
        if (hasBurner()) {
            boxes.add(getBurnerBoundingBox(mainBox));
        }

        return boxes;
    }

    @Override
    public Map<Box, List<Entity>> getCollidingEntities(Stream<Box> boundingBoxes) {
        return EntityCollisions.getCollidingEntities(this, boundingBoxes, RIDER_PREDICATE);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setBasketType(compound.get("basket", BasketType.CODEC).orElse(BasketType.DEFAULT));
        setDesign(compound.get("design", BalloonDesign.CODEC).orElse(BalloonDesign.NONE));
        setAscending(compound.getBoolean("burnerActive", false));
        setBoostTicks(compound.getInt("boostTicks", 0));
        prevInflation = compound.getInt("inflationAmount", 0);
        setInflation(prevInflation);
        activeFuel = MathHelper.clamp(compound.getInt("fuel", 0), 0, maxFuel);
        fuelItems = new ArrayList<>(compound.get("fuelItems", FUEL_CODEC, getRegistryManager().getOps(NbtOps.INSTANCE)).stream().flatMap(List::stream).toList());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.put("design", BalloonDesign.CODEC, getDesign());
        compound.put("basket", BasketType.CODEC, getBasketType());
        compound.putBoolean("burnerActive", isAscending());
        compound.putInt("boostTicks", getBoostTicks());
        compound.putInt("inflationAmount", getInflation());
        compound.putInt("fuel", activeFuel);
        compound.put("fuelItems", FUEL_CODEC, getRegistryManager().getOps(NbtOps.INSTANCE), fuelItems);
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

    public enum BalloonDesign implements StringIdentifiable {
        NONE,
        LUNA,
        DAWN,
        EQUALITY,
        STORM,
        TALE;

        public static final BalloonDesign[] VALUES = values();
        public static final EnumCodec<BalloonDesign> CODEC = StringIdentifiable.createCodec(BalloonDesign::values);
        public static final PacketCodec<ByteBuf, BalloonDesign> PACKET_CODEC = PacketCodecUtils.ofEnum(BalloonDesign.class);

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return name;
        }

        public static AirBalloonEntity.BalloonDesign of(World world, ItemStack stack) {
            AirBalloonEntity.BalloonDesign design = BalloonDesignComponent.get(stack).design();
            if (design == NONE) {
                return VALUES[1 + world.getRandom().nextInt(VALUES.length - 1)];
            }
            return design;
        }

        public static BalloonDesign getType(int type) {
            return VALUES[Math.abs(type) % VALUES.length];
        }
    }

    public record BasketType(Identifier id, WoodType woodType, Block representative) {
        private static final Map<Identifier, BasketType> REGISTRY = new HashMap<>();
        public static final Codec<BasketType> CODEC = WoodType.CODEC.xmap(woodType -> REGISTRY.get(Identifier.of(woodType.name())), BasketType::woodType);
        public static final BasketType DEFAULT = of(WoodType.OAK, Blocks.OAK_PLANKS);

        public boolean isOf(WoodType woodType) {
            return this.woodType == woodType;
        }

        static BasketType of(String name) {
            return REGISTRY.getOrDefault(Identifier.tryParse(name), DEFAULT);
        }

        public static BasketType of(WoodType woodType, Block representative) {
            return REGISTRY.computeIfAbsent(Identifier.of(woodType.name()), id -> new BasketType(id, woodType, representative));
        }
    }
}









