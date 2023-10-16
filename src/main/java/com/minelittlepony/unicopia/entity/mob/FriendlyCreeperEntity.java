package com.minelittlepony.unicopia.entity.mob;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ai.FleeExplosionGoal;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.BlockView;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;

public class FriendlyCreeperEntity extends TameableEntity implements SkinOverlayOwner, Angerable {
    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(FriendlyCreeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CHARGED = DataTracker.registerData(FriendlyCreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(FriendlyCreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(FriendlyCreeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);

    private int lastFuseTime;
    private int currentFuseTime;
    private short fuseTime = 30;
    private byte explosionRadius = 3;
    private int headsDropped;
    private short hugTime;
    private short lastHugTime;

    protected FriendlyCreeperEntity(EntityType<? extends FriendlyCreeperEntity> type, World world) {
        super(type, world);
        setTamed(false);
        setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1);
        setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(FUSE_SPEED, -1);
        dataTracker.startTracking(CHARGED, false);
        dataTracker.startTracking(IGNITED, false);
        dataTracker.startTracking(ANGER_TIME, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(1, new EscapeGoal(1.5));
        goalSelector.add(2, new SitGoal(this));
        goalSelector.add(3, new IgniteGoal());
        goalSelector.add(4, new FleeEntityGoal<>(this, OcelotEntity.class, 6, 1, 1.2));
        goalSelector.add(4, new FleeEntityGoal<>(this, CatEntity.class, 6, 1, 1.2));
        goalSelector.add(5, new MeleeAttackGoal(this, 1, false));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(7, new LookAroundGoal(this));
        targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        targetSelector.add(2, new AttackWithOwnerGoal(this));
        targetSelector.add(3, new RevengeGoal(this).setGroupRevenge(WolfEntity.class));
        targetSelector.add(7, new ActiveTargetGoal<>(this, AbstractSkeletonEntity.class, false));
        targetSelector.add(8, new UniversalAngerGoal<>(this, true));
    }

    public static DefaultAttributeContainer.Builder createCreeperAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected Text getDefaultName() {
        return EntityType.CREEPER.getName();
    }

    @Override
    public int getSafeFallDistance() {
        return 3 + (getTarget() == null ? 0 : ((int)(getHealth() - 1)));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
        currentFuseTime += (int)(fallDistance * 1.5f);
        if (currentFuseTime > fuseTime - 5) {
            currentFuseTime = fuseTime - 5;
        }
        return bl;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (dataTracker.get(CHARGED).booleanValue()) {
            nbt.putBoolean("powered", true);
        }
        nbt.putShort("Fuse", fuseTime);
        nbt.putShort("Hugged", hugTime);
        nbt.putByte("ExplosionRadius", explosionRadius);
        nbt.putBoolean("ignited", isIgnited());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(CHARGED, nbt.getBoolean("powered"));
        if (nbt.contains("Fuse", NbtElement.NUMBER_TYPE)) {
            fuseTime = nbt.getShort("Fuse");
        }
        if (nbt.contains("Hugged", NbtElement.NUMBER_TYPE)) {
            hugTime = nbt.getShort("Hugged");
        }
        if (nbt.contains("ExplosionRadius", NbtElement.NUMBER_TYPE)) {
            explosionRadius = nbt.getByte("ExplosionRadius");
        }
        if (nbt.getBoolean("ignited")) {
            ignite();
        }
    }

    @Override
    public void tick() {
        setSitting(isInSittingPose());

        if (isAlive()) {
            lastFuseTime = currentFuseTime;
            if (isIgnited()) {
                setFuseSpeed(1);
            }
            int fuseSpeed = getFuseSpeed();
            if (fuseSpeed > 0 && currentFuseTime == 0) {
                playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                emitGameEvent(GameEvent.PRIME_FUSE);
            }
            currentFuseTime = Math.max(0, currentFuseTime + fuseSpeed);
            if (currentFuseTime >= fuseTime) {
                currentFuseTime = fuseTime;
                explode();
            }

            lastHugTime = hugTime;

            if (!isTamed()) {
                if (isConverting()) {
                    if (++hugTime >= 100) {
                        if (!getWorld().isClient) {
                            setOwner(getCreature().getCarrierId().map(getWorld()::getPlayerByUuid).orElse(null));
                            getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                        }
                    }

                    if (hugTime % 5 == 0) {
                        playHurtSound(getDamageSources().generic());
                    }
                } else {
                    hugTime = 0;
                    if (!getWorld().isClient) {
                        getWorld().spawnEntity(convertTo(EntityType.CREEPER, true));
                        discard();
                    }
                }
            } else {
                if (random.nextInt(30) == 0) {
                    spawnHeart();
                }
                hugTime = 0;
            }
        }

        super.tick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (!getWorld().isClient) {
            tickAngerLogic((ServerWorld)getWorld(), true);
        }
    }

    private void spawnHeart() {
        getWorld().addParticle(ParticleTypes.HEART, random.nextTriangular(getX(), 0.5), getY() + getHeight(), random.nextTriangular(getZ(), 0.5), 0, 0, 0);
    }

    private Creature getCreature() {
        return Equine.<FriendlyCreeperEntity, Creature>of(this, c -> c instanceof Creature).get();
    }

    public boolean isConverting() {
        return !isTamed() && getCreature().isBeingCarried();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof GoatEntity) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (source.getAttacker() instanceof CreeperEntity c && c.shouldDropHead()) {
            c.onHeadDropped();
            dropItem(Items.CREEPER_HEAD);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        return true;
    }

    @Override
    public boolean shouldRenderOverlay() {
        return dataTracker.get(CHARGED);
    }

    public float getClientFuseTime(float timeDelta) {
        return MathHelper.lerp(timeDelta, (float)lastFuseTime, (float)currentFuseTime) / (fuseTime - 2)
             + MathHelper.lerp(timeDelta, (float)lastHugTime, (float)hugTime) / 98F;
    }

    public int getFuseSpeed() {
        return dataTracker.get(FUSE_SPEED);
    }

    public void setFuseSpeed(int fuseSpeed) {
        dataTracker.set(FUSE_SPEED, fuseSpeed);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        super.onStruckByLightning(world, lightning);
        dataTracker.set(CHARGED, true);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        Consumer<PlayerEntity> statusCallback = p -> p.sendToolBreakStatus(hand);

        if (stack.isEmpty() && isOwner(player)) {
            setSitting(!isSitting());
            setInSittingPose(isSitting());
            return ActionResult.success(getWorld().isClient);
        }

        if (stack.isIn(ItemTags.CREEPER_IGNITERS)) {
            SoundEvent soundEvent = stack.isOf(Items.FIRE_CHARGE) ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ITEM_FLINTANDSTEEL_USE;
            getWorld().playSound(player, getX(), getY(), getZ(), soundEvent, getSoundCategory(), 1, random.nextFloat() * 0.4f + 0.8f);

            if (!getWorld().isClient) {
                ignite();
                if (!stack.isDamageable()) {
                    stack.decrement(1);
                } else {
                    stack.damage(1, player, statusCallback);
                }
            }

            return ActionResult.success(getWorld().isClient);
        }

        if (stack.isOf(Items.GUNPOWDER) && getHealth() < getMaxHealth()) {
            getWorld().playSound(player, getX(), getY(), getZ(), SoundEvents.ENTITY_CAT_EAT, getSoundCategory(), 1, random.nextFloat() * 0.4f + 0.8f);

            currentFuseTime = fuseTime - 1;
            if (!getWorld().isClient) {
                heal(3);
                getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                if (!stack.isDamageable()) {
                    stack.decrement(1);
                } else {
                    stack.damage(1, player, statusCallback);
                }
            }

            return ActionResult.success(getWorld().isClient);
        }

        return super.interactMob(player, hand);
    }

    private void explode() {
        if (!getWorld().isClient) {
            dead = true;
            getWorld().createExplosion(this, getX(), getY(), getZ(), getExplosionRadius(), World.ExplosionSourceType.MOB);
            discard();
            spawnEffectsCloud();
        }
    }

    @Override
    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        return false;
    }

    private float getExplosionRadius() {
        return explosionRadius * (shouldRenderOverlay() ? 2 : 1);
    }

    private void spawnEffectsCloud() {
        Collection<StatusEffectInstance> effects = getStatusEffects();
        if (!effects.isEmpty()) {
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(getWorld(), getX(), getY(), getZ());
            cloud.setRadius(2.5f);
            cloud.setRadiusOnUse(-0.5f);
            cloud.setWaitTime(10);
            cloud.setDuration(cloud.getDuration() / 2);
            cloud.setRadiusGrowth(-cloud.getRadius() / cloud.getDuration());
            effects.forEach(effect -> cloud.addEffect(new StatusEffectInstance(effect)));
            getWorld().spawnEntity(cloud);
        }
    }

    public boolean isIgnited() {
        return dataTracker.get(IGNITED);
    }

    public void ignite() {
        dataTracker.set(IGNITED, true);
        FleeExplosionGoal.notifySurroundings(this, getExplosionRadius());
    }

    public boolean shouldDropHead() {
        return shouldRenderOverlay() && headsDropped < 1;
    }

    public void onHeadDropped() {
        headsDropped++;
    }

    @Override
    public EntityView method_48926() {
        return getWorld();
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity partner) {
        FriendlyCreeperEntity child = (FriendlyCreeperEntity)getType().create(world);
        UUID uUID = getOwnerUuid();
        if (uUID != null) {
            child.setOwnerUuid(uUID);
            child.setTamed(true);
        }
        return child;
    }

    @Nullable
    private UUID angerTarget;

    @Override
    public int getAngerTime() {
        return dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int time) {
        dataTracker.set(ANGER_TIME, time);
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return angerTarget;
    }

    @Override
    public void setAngryAt(UUID target) {
        angerTarget = target;
    }

    @Override
    public void chooseRandomAngerTime() {
        setAngerTime(ANGER_TIME_RANGE.get(random));
    }

    class IgniteGoal extends Goal {
        @Nullable
        private LivingEntity target;

        public IgniteGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = getTarget();
            return getFuseSpeed() > 0 || livingEntity != null && squaredDistanceTo(livingEntity) < 9.0;
        }

        @Override
        public void start() {
            getNavigation().stop();
            target = getTarget();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (target == null) {
                setFuseSpeed(-1);
                return;
            }
            if (squaredDistanceTo(target) > 49.0) {
                setFuseSpeed(-1);
                return;
            }
            if (!getVisibilityCache().canSee(target)) {
                setFuseSpeed(-1);
                return;
            }
            setFuseSpeed(1);
        }
    }

    class EscapeGoal extends EscapeDangerGoal {

        public EscapeGoal(double speed) {
            super(FriendlyCreeperEntity.this, speed);
        }

        @Override
        protected boolean isInDanger() {
            return mob.shouldEscapePowderSnow() || mob.isOnFire();
        }
    }
}
