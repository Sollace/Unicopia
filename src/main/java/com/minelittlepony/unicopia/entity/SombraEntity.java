package com.minelittlepony.unicopia.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SombraEntity extends HostileEntity {

    private static final TrackedData<Optional<BlockPos>> HOME_POS = DataTracker.registerData(SombraEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.PROGRESS)
            .setDarkenSky(true)
            .setThickenFog(true);

    public SombraEntity(EntityType<SombraEntity> type, World world) {
        super(type, world);
        bossBar.setStyle(BossBar.Style.NOTCHED_10);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 2000)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 102);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WARDEN_DEATH;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(HOME_POS, Optional.empty());
    }

    @Override
    protected void initGoals() {
        goalSelector.add(5, new WanderAroundGoal(this, 1));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8F));
        goalSelector.add(7, new LookAroundGoal(this));
        goalSelector.add(8, new PounceAtTargetGoal(this, 0.3f));
        goalSelector.add(8, new AttackGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }


    @Override
    protected EntityNavigation createNavigation(World world) {
        MobNavigation nav = new MobNavigation(this, world);
        nav.setCanPathThroughDoors(true);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    public Optional<BlockPos> getHomePos() {
        return dataTracker.get(HOME_POS);
    }

    public void setHomePos(BlockPos pos) {
        dataTracker.set(HOME_POS, Optional.of(pos));
    }

    @Override
    public void tick() {

        Optional<BlockPos> homePos = getHomePos();

        if (homePos.isEmpty() && !isRemoved()) {
            remove(RemovalReason.DISCARDED);
            return;
        }

        if (this.getBlockPos().getSquaredDistance(homePos.get()) > 16) {
            teleportTo(Vec3d.ofCenter(homePos.get()));
            setTarget(null);
        }

        super.tick();

        addVelocity(0, 0.002F, 0);
        if (isSubmergedInWater()) {
            jump();
        }

        if (age % 50 == 0) {
            playSound(SoundEvents.ENTITY_POLAR_BEAR_AMBIENT, 3, 0.3F);
        }

        if (age % 125 == 0) {
            playSound(SoundEvents.AMBIENT_CAVE.value(), 1, 0.3F);
        }

        if (getWorld().isClient) {
            float range = 9;
            for (int i = 0; i < 3; i++) {
                var particle = new SphereParticleEffect(UParticles.SPHERE,
                        0x222222,
                        0.7F,
                        (float)getWorld().getRandom().nextTriangular(2.5F, 0.7F)
                );
                getWorld().addParticle(particle,
                        getWorld().getRandom().nextTriangular(getX(), range),
                        getWorld().getRandom().nextTriangular(getY(), range),
                        getWorld().getRandom().nextTriangular(getZ(), range),
                        getWorld().getRandom().nextGaussian() / 6F,
                        getWorld().getRandom().nextGaussian() / 6F,
                        getWorld().getRandom().nextGaussian() / 6F
                    );
            }

            for (int i = 0; i < 13; i++) {
                getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                        getWorld().getRandom().nextTriangular(getX(), 1),
                        getWorld().getRandom().nextTriangular(getY(), 1),
                        getWorld().getRandom().nextTriangular(getZ(), 1),
                        0,
                        0,
                        0
                    );
            }
        }

        getWorld().getOtherEntities(this, this.getBoundingBox().expand(5), EntityPredicates.VALID_LIVING_ENTITY).forEach(target -> {
            ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 1));
        });
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(getHealth() / getMaxHealth());
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 64 * getRenderDistanceMultiplier();
        return distance < d * d;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {

    }

    @Override
    protected void tickCramming() {

    }

    @Override
    public boolean handleFallDamage(float distance, float damageMultiplier, DamageSource cause) {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (AmuletSelectors.ALICORN_AMULET.test(player)) {
                if (!getWorld().isClient) {
                    player.sendMessage(Text.translatable("entity.unicopia.sombra.taunt"));
                }
            }
            ItemStack amulet = AmuletItem.getForEntity(player);
            if (amulet.isOf(UItems.ALICORN_AMULET)) {
                amulet.decrement(1);
            }
        }
        boolean damaged = super.damage(source, amount);

        if (source.getAttacker() instanceof PlayerEntity player) {
            teleportRandomly(16);
        }

        return damaged;
    }

    @Override
    protected void fall(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    protected boolean teleportRandomly(int maxDistance) {
        if (getWorld().isClient() || !isAlive()) {
            return false;
        }
        return teleportTo(getPos().add(VecHelper.supply(() -> random.nextTriangular(0, maxDistance))));
    }

    private boolean teleportTo(Vec3d destination) {
        Vec3d oldPos = getPos();
        if (canTeleportTo(destination) && teleport(destination.x, destination.y, destination.z, true)) {
            getWorld().emitGameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Emitter.of(this));
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean canTeleportTo(Vec3d destination) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(destination.x, destination.y, destination.z);
        while (mutable.getY() > getWorld().getBottomY() && !getWorld().getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }
        BlockState destinationState = getWorld().getBlockState(mutable);
        return destinationState.blocksMovement() && !destinationState.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    @Deprecated
    public float getBrightnessAtEyes() {
        return super.getBrightnessAtEyes() * 0.2F;
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getHomePos().map(NbtHelper::fromBlockPos).ifPresent(pos -> {
            nbt.put("homePos", pos);
        });
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("homePos", NbtElement.COMPOUND_TYPE)) {
            setHomePos(NbtHelper.toBlockPos(nbt.getCompound("homePos")));
        }
        if (hasCustomName()) {
            bossBar.setName(getDisplayName());
        }
    }
}
