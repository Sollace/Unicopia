package com.minelittlepony.unicopia.entity.mob;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.WeatherConditions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;

public class StormCloudEntity extends Entity implements MagicImmune {
    private static final TrackedData<Integer> CLEAR_TICKS = DataTracker.registerData(StormCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STORM_TICKS = DataTracker.registerData(StormCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> TARGET_SIZE = DataTracker.registerData(StormCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> DISSIPATING = DataTracker.registerData(StormCloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    static final float MAX_SIZE = 30;
    static final int CLEAR_TARGET_ALTITUDE = 90;
    static final int STORMY_TARGET_ALTITUDE = 20;

    private float prevSize;
    private float currentSize;

    public boolean cursed;

    private int phase;
    private int nextPhase;

    @Nullable
    private ServerBossBar bossBar;
    private final Set<ServerPlayerEntity> trackingPlayers = new HashSet<>();

    public StormCloudEntity(EntityType<StormCloudEntity> type, World world) {
        super(type, world);
        setSize(1 + random.nextInt(4));
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(STORM_TICKS, 0);
        dataTracker.startTracking(CLEAR_TICKS, 0);
        dataTracker.startTracking(TARGET_SIZE, 1F);
        dataTracker.startTracking(DISSIPATING, false);
    }

    public boolean isStormy() {
        return getStormTicks() != 0;
    }

    public int getStormTicks() {
        return dataTracker.get(STORM_TICKS);
    }

    public void setStormTicks(int stormTicks) {
        dataTracker.set(STORM_TICKS, stormTicks);
        setClearTicks(stormTicks);
    }

    public void setDissipating(boolean dissipating) {
        dataTracker.set(DISSIPATING, dissipating);
    }

    public boolean isDissipating() {
        return dataTracker.get(DISSIPATING);
    }

    private void setClearTicks(int clearTicks) {
        dataTracker.set(CLEAR_TICKS, clearTicks);
    }

    private int getClearTicks() {
        return dataTracker.get(CLEAR_TICKS);
    }

    public boolean tickClear() {
        int clearTicks = getClearTicks();
        if (clearTicks > 0) {
            setClearTicks(--clearTicks);
        }
        return clearTicks > 0;
    }

    public float getSize(float tickDelta) {
        return MathHelper.clamp(MathHelper.lerp(tickDelta, prevSize, currentSize), 1, MAX_SIZE);
    }

    public void setSize(float size) {
        dataTracker.set(TARGET_SIZE, size);
    }

    public int getSizeInBlocks() {
        return (int)(getWidth() * (getSize(1) / 15F));
    }

    @Override
    public void tick() {
        setFireTicks(1);

        prevSize = currentSize;
        float targetSize = dataTracker.get(TARGET_SIZE);
        if (currentSize != targetSize) {
            float sizeDifference = (dataTracker.get(TARGET_SIZE) - currentSize);
            currentSize = Math.abs(sizeDifference) < 0.01F ? targetSize : currentSize + (sizeDifference * 0.02F);
        }

        if (isStormy()) {
            int stormTicks = getStormTicks();
            if (stormTicks > 0) {
                setStormTicks(stormTicks - 1);
            }
        }

        if (isLogicalSideForUpdatingMovement()) {
            float groundY = findSurfaceBelow(getWorld(), getBlockPos()).getY();
            float targetY = isStormy() ? STORMY_TARGET_ALTITUDE : CLEAR_TARGET_ALTITUDE;
            float cloudY = (float)getY() - targetY;

            addVelocity(0, 0.0003F * (groundY - cloudY), 0);

            if (!cursed && !isStormy()) {
                Vec3d wind = WeatherConditions.get(getWorld()).getWindDirection();
                addVelocity(wind.x * 0.001F, 0, wind.z * 0.001F);
            }

            Vec3d velocity = getVelocity();
            setPosition(getPos().add(velocity.multiply(0.7F)));
            setVelocity(velocity.multiply(0.9F));

            float randomYaw = random.nextFloat() * 360;
            Vec3d randomVelocity = Vec3d.fromPolar(0, randomYaw).multiply(0.002);

            for (var cloud : getWorld().getEntitiesByClass(StormCloudEntity.class, getBoundingBox(), EntityPredicates.VALID_ENTITY)) {
                if (cloud != this) {
                    cloud.addVelocity(randomVelocity);
                }
            }
        }

        super.tick();

        float size = getSize(1);

        if (getWorld().isClient()) {
            if (isStormy()) {
                WeatherConditions.get(getWorld()).addStorm(this);

                float sizeInBlocks = getSizeInBlocks();
                int area = (int)MathHelper.square(sizeInBlocks);
                sizeInBlocks /= getWidth();
                for (int i = 0; i < area; i++) {
                    float x = (float)getParticleX(sizeInBlocks);
                    float z = (float)getParticleZ(sizeInBlocks);

                    getWorld().addParticle(UParticles.RAIN_DROPS, x, getY(), z, 0, -0.2F, 0);
                }
            }
        } else {
            if (!isStormy() && !cursed && size > 10) {
                if (random.nextInt(1 + (int)(4000 / size)) == 0) {
                    split(random.nextBetween(1, 3));
                    return;
                }
            }

            if (cursed) {
                float percent = Math.min(1, (size + 1) / MAX_SIZE);
                getBossBar().setPercent(percent);
            }

            if (currentSize == targetSize) {
                if (isDissipating()) {
                    if (size < 2) {
                        kill();
                    } else {
                        if (random.nextInt(4) == 0) {
                            split(2);
                        } else {
                            setSize(size - 1);
                        }
                    }
                } else {
                    if (size < MAX_SIZE) {
                        if (cursed) {
                            setSize(size + 1);
                            setStormTicks(-1);

                            spawnLightningStrike(getBlockPos(), false, false);
                            playSound(USounds.AMBIENT_WIND_GUST, 1, 1);
                        }
                    }
                }
            }

            if (size >= MAX_SIZE) {
                if (cursed) {
                    setStormTicks(-1);

                    if (nextPhase-- == 0) {
                        phase++;
                        nextPhase = random.nextBetween(17, 120);

                        if (phase == 1) {
                            playSound(USounds.ENTITY_SOMBRA_SNICKER, 10, 1);
                        }

                        if (++phase >= 7) {
                            pickRandomPoints(13, pos -> spawnLightningStrike(pos, true, false));

                            cursed = false;
                            SombraEntity sombra = new SombraEntity(UEntities.SOMBRA, getWorld(), getBossBar());
                            sombra.setPosition(getPos());
                            sombra.setHomePos(getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()));
                            sombra.setScaleFactor(8);
                            sombra.stormCloud.set(this);
                            getWorld().spawnEntity(sombra);

                            playSound(USounds.ENTITY_SOMBRA_LAUGH, 10, 1);
                        } else {
                            pickRandomPoints(3, pos -> spawnLightningStrike(pos, true, false));
                        }
                    }
                } else {
                    if (!isStormy() && !tickClear()) {
                        setStormTicks(random.nextBetween(30, 120));
                    }
                }
            }

            if (isStormy() && age % 170 == 0) {
                pickRandomPoints(3, pos -> spawnLightningStrike(pos, false, random.nextInt(30) == 0));
            }
        }
    }

    private void pickRandomPoints(int count, Consumer<BlockPos> action) {
        BlockPos.iterateRandomly(random, 3, getBlockPos(), getSizeInBlocks()).forEach(pos -> {
            action.accept(findSurfaceBelow(getWorld(), pos));
        });
    }

    public static BlockPos findSurfaceBelow(World world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        mutable.set(pos);
        while (mutable.getY() > world.getBottomY() && world.isAir(mutable)) {
            mutable.move(Direction.DOWN);
        }
        while (world.isInBuildLimit(mutable) && !world.isAir(mutable)) {
            mutable.move(Direction.UP);
        }
        mutable.move(Direction.DOWN);

        return mutable;
    }

    private void spawnLightningStrike(BlockPos pos, boolean cosmetic, boolean infect) {
        if (infect) {
            if (!CrystalShardsEntity.infestBlock((ServerWorld)getWorld(), pos)) {
                return;
            }
        }
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(getWorld());
        lightning.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        lightning.setCosmetic(cosmetic);
        getWorld().spawnEntity(lightning);

    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        super.damage(source, amount);
        if (!cursed) {
            if (random.nextInt(35) == 0 || (source.isOf(DamageTypes.PLAYER_ATTACK) && EquineContext.of(source.getAttacker()).collidesWithClouds())) {
                if (getSize(1) < 2) {
                    if (!getWorld().isClient() && getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                        Identifier identifier = getType().getLootTableId();
                        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder((ServerWorld)this.getWorld())
                                .add(LootContextParameters.THIS_ENTITY, this)
                                .add(LootContextParameters.ORIGIN, this.getPos())
                                .add(LootContextParameters.DAMAGE_SOURCE, source)
                                .addOptional(LootContextParameters.KILLER_ENTITY, source.getAttacker())
                                .addOptional(LootContextParameters.DIRECT_KILLER_ENTITY, source.getSource());
                        if (source.getAttacker() instanceof PlayerEntity player) {
                            builder = builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, player).luck(player.getLuck());
                        }
                        getWorld().getServer().getLootManager().getLootTable(identifier)
                            .generateLoot(builder.build(LootContextTypes.ENTITY), 0L, this::dropStack);
                    }
                    kill();
                    getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
                } else {
                    split(2 + random.nextInt(4));
                }
            }
        }
        return false;
    }

    @Override
    @Nullable
    public ItemEntity dropStack(ItemStack stack) {
        stack = stack.copy();
        while (!stack.isEmpty()) {
            ItemEntity drop = super.dropStack(stack.split(1));
            if (drop != null) {
                drop.addVelocity(random.nextTriangular(0, 0.3), 0, random.nextTriangular(0, 0.3));
            }
        }
        return null;
    }

    public void split(int splitCount) {
        Vec3d center = getPos();

        float totalAngle = (360F / splitCount) + ((float)random.nextGaussian() * 360F);
        float size = getSize(1) / splitCount;
        int stormTicks = getStormTicks() / splitCount;

        if (size < 1) {
            return;
        }
        discard();

        for (int i = 0; i < splitCount; i++) {
            StormCloudEntity lump = (StormCloudEntity)getType().create(getWorld());
            lump.setSize(size);
            lump.setStormTicks(stormTicks);
            lump.setPosition(center);
            lump.setDissipating(isDissipating());
            lump.setVelocity(Vec3d.fromPolar(0, totalAngle * i).normalize().multiply(1.8F));

            getWorld().spawnEntity(lump);
        }

    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case EntityStatuses.ADD_DEATH_PARTICLES:
                for (int i = 0; i < 20; ++i) {
                    double d = random.nextGaussian() * 0.02;
                    double e = random.nextGaussian() * 0.02;
                    double f = random.nextGaussian() * 0.02;
                    getWorld().addParticle(ParticleTypes.POOF, getParticleX(1), getRandomBodyY(), getParticleZ(1), d, e, f);
                }
                break;
            default:
                super.handleStatus(status);
        }

    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public final boolean canHit() {
        return true;
    }

    private ServerBossBar getBossBar() {
        if (this.bossBar == null) {
            this.bossBar = SombraEntity.createBossBar(Text.translatable("unicopia.entity.sombra").formatted(Formatting.OBFUSCATED));
            trackingPlayers.removeIf(Entity::isRemoved);
            trackingPlayers.forEach(this.bossBar::addPlayer);
        }
        return this.bossBar;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("stormTicks", getStormTicks());
        nbt.putInt("clearTicks", getClearTicks());
        nbt.putFloat("size", getSize(1));
        nbt.putBoolean("cursed", cursed);
        nbt.putInt("phase", phase);
        nbt.putInt("nextPhase", nextPhase);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        setStormTicks(nbt.getInt("stormTicks"));
        setClearTicks(nbt.getInt("clearTicks"));
        if (nbt.contains("size", NbtElement.FLOAT_TYPE)) {
            setSize(currentSize = nbt.getFloat("size"));
        }
        cursed = nbt.getBoolean("cursed");
        phase = nbt.getInt("phase");
        nextPhase = nbt.getInt("nextPhase");
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        trackingPlayers.removeIf(Entity::isRemoved);
        trackingPlayers.add(player);
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        trackingPlayers.removeIf(Entity::isRemoved);
        trackingPlayers.remove(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

}
