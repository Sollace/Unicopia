package com.minelittlepony.unicopia.entity.player;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.client.render.PlayerPoser.AnimationInstance;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.*;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.SpellSlots.UpdateCallback;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.RageAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitDiscovery;
import com.minelittlepony.unicopia.advancement.TriggerCountTracker;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.effect.MetamorphosisStatusEffect;
import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.minelittlepony.unicopia.network.*;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.common.util.animation.LinearInterpolator;
import com.google.common.collect.Streams;
import com.minelittlepony.common.util.animation.Interpolator;
import com.mojang.authlib.GameProfile;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class Pony extends Living<PlayerEntity> implements Copyable<Pony>, UpdateCallback {
    static final int INITIAL_SUN_IMMUNITY = 20;

    private final AbilityDispatcher powers = new AbilityDispatcher(this);
    private final PlayerPhysics gravity = addTicker(new PlayerPhysics(this, tracker));
    private final PlayerCharmTracker charms = new PlayerCharmTracker(this);
    private final PlayerCamera camera = new PlayerCameraImpl(this);
    private final TraitDiscovery discoveries = new TraitDiscovery(this);
    private final Acrobatics acrobatics = new Acrobatics(this, tracker);
    private final CorruptionHandler corruptionHandler = new CorruptionHandler(this);

    private TriggerCountTracker advancementProgress = new TriggerCountTracker(Map.of());

    private final ManaContainer mana;
    private final PlayerLevelStore levels;
    private final PlayerLevelStore corruption;

    private final Interpolator interpolator = new LinearInterpolator();

    private Race.Composite compositeRace = Race.UNSET.composite();
    private Race respawnRace = Race.UNSET;

    private boolean dirty;

    private float magicExhaustion = 0;

    private int ticksInvulnerable;
    private int ticksMetamorphising;

    private int ticksInSun;
    private boolean hasShades;
    private int ticksSunImmunity = INITIAL_SUN_IMMUNITY;

    private AnimationInstance animation = new AnimationInstance(Animation.NONE, Animation.Recipient.ANYONE);
    private int animationMaxDuration;
    private int animationDuration;

    private DataTracker.Entry<Race> race;
    private DataTracker.Entry<Race> suppressedRace;

    public Pony(PlayerEntity player) {
        super(player);
        trackers.addPacketEmitter((sender, initial) -> {
            if (initial || dirty) {
                dirty = false;
                sender.accept(Channel.SERVER_PLAYER_CAPABILITIES.toPacket(new MsgPlayerCapabilities(this)));
            }
        });

        race = this.tracker.startTracking(TrackableDataType.RACE, Race.UNSET);
        suppressedRace = this.tracker.startTracking(TrackableDataType.RACE, Race.UNSET);
        this.levels = new PlayerLevelStore(this, tracker, true, USounds.Vanilla.ENTITY_PLAYER_LEVELUP);
        this.corruption = new PlayerLevelStore(this, tracker, false, USounds.ENTITY_PLAYER_CORRUPTION);
        this.mana = addTicker(new ManaContainer(this, tracker));

        addTicker(this::updateAnimations);
        addTicker(this::updateBatPonyAbilities);
        addTicker(this::updateCorruptionDecay);
        addTicker(new PlayerAttributes(this));
        addTicker(corruptionHandler);
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(UEntityAttributes.EXTRA_MINING_SPEED);
        builder.add(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
    }

    @Deprecated
    public void setAnimation(Animation animation) {
        setAnimation(new AnimationInstance(animation, Animation.Recipient.ANYONE));
    }

    @Deprecated
    public void setAnimation(Animation animation, int duration) {
        setAnimation(new AnimationInstance(animation, Animation.Recipient.ANYONE));
    }

    public void setAnimation(Animation animation, Animation.Recipient recipient) {
        if (getAnimation().isOf(animation) && animationDuration > 0) {
            return;
        }
        setAnimation(new AnimationInstance(animation, recipient), animation.getDuration());
    }

    public void setAnimation(Animation animation, Animation.Recipient recipient, int duration) {
        if (getAnimation().isOf(animation) && animationDuration > 0) {
            return;
        }
        setAnimation(new AnimationInstance(animation, recipient), duration);
    }

    public void setAnimation(AnimationInstance animation) {
        setAnimation(animation, animation.animation().getDuration());
    }

    public void setAnimation(AnimationInstance animation, int duration) {
        if (!animation.equals(this.animation) || duration != animationDuration) {
            this.animation = animation;
            this.animationDuration = animation.isOf(Animation.NONE) ? 0 : Math.max(0, duration);
            this.animationMaxDuration = animationDuration;

            if (!isClient()) {
                Channel.SERVER_PLAYER_ANIMATION_CHANGE.sendToAllPlayers(new MsgPlayerAnimationChange(this, animation, animationDuration), asWorld());
            }

            animation.animation().getSound().ifPresent(sound -> {
                playSound(sound, sound == USounds.ENTITY_PLAYER_WOLOLO ? 0.1F : 0.9F, 1);
            });
            setDirty();
        }
    }

    public AnimationInstance getAnimation() {
        return animation;
    }

    public float getAnimationProgress(float delta) {
        if (animation.isOf(Animation.NONE)) {
            return 0;
        }
        return 1 - (((float)animationDuration) / animationMaxDuration);
    }

    public TriggerCountTracker getAdvancementProgress() {
        return advancementProgress;
    }

    public void setRespawnRace(Race race) {
        respawnRace = race;
    }

    /**
     * Gets this player's inherent species.
     */
    @Override
    public Race getSpecies() {
        return race.get();
    }

    /**
     * Gets the species this player appears to be.
     * This includes illusions and shape-shifting but excludes items that grant abilities without changing their race.
     */
    public Race getObservedSpecies() {
        return getCompositeRace().physical();
    }

    /**
     * Gets the composite race that represents what this player is capable of.
     * Physical is the race they appear to have, whilst pseudo is the race who's abilities they have been granted by magical means.
     */
    @Override
    public Race.Composite getCompositeRace() {
        return compositeRace;
    }

    @Override
    public boolean collidesWithClouds() {
        return getCompositeRace().canInteractWithClouds() || entity.isCreative();
    }

    @Override
    public void setSpecies(Race race) {
        race = race.validate(entity);
        Race current = getSpecies();
        this.race.set(race);
        if (race != current) {
            clearSuppressedRace();
        }

        ticksInSun = 0;

        gravity.updateFlightState();
        entity.sendAbilitiesUpdate();
        recalculateCompositeRace();
    }

    public void setSuppressedRace(Race race) {
        suppressedRace.set(race.validate(entity));
    }

    public void clearSuppressedRace() {
        setSuppressedRace(Race.UNSET);
    }

    public Race getSuppressedRace() {
        return suppressedRace.get();
    }

    public TraitDiscovery getDiscoveries() {
        return discoveries;
    }

    public MagicReserves getMagicalReserves() {
        return mana;
    }

    public PlayerCharmTracker getCharms() {
        return charms;
    }

    public Acrobatics getAcrobatics() {
        return acrobatics;
    }

    @Override
    public LevelStore getLevel() {
        return levels;
    }

    @Override
    public LevelStore getCorruption() {
        return corruption;
    }

    public CorruptionHandler getCorruptionhandler() {
        return corruptionHandler;
    }

    public boolean canUseSuperMove() {
        return entity.isCreative() || getMagicalReserves().getCharge().get() >= getMagicalReserves().getCharge().getMax();
    }

    public boolean consumeSuperMove() {
        if (canUseSuperMove()) {
            Bar charge = getMagicalReserves().getCharge();
            charge.set(charge.get() - charge.getMax());
            return true;
        }
        return false;
    }

    public boolean isSunImmune() {
        return ticksSunImmunity > 0;
    }

    public void setInvulnerabilityTicks(int ticks) {
        this.ticksInvulnerable = Math.max(0, ticks);
    }

    public int getTicksMetamorphising() {
        return ticksMetamorphising;
    }

    public void setTicksmetamorphising(int ticks) {
        ticksMetamorphising = ticks;
    }

    @Override
    public Affinity getAffinity() {
        return getSpecies().getAffinity();
    }

    @Deprecated
    public void setDirty() {
        dirty = true;
    }

    public AbilityDispatcher getAbilities() {
        return powers;
    }

    @Override
    public PlayerPhysics getPhysics() {
        return gravity;
    }

    public float getBlockBreakingSpeed() {
        return (float)entity.getAttributeInstance(UEntityAttributes.EXTRA_MINING_SPEED).getValue();
    }

    public Motion getMotion() {
        return gravity;
    }

    public PlayerCamera getCamera() {
        return camera;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public final LivingEntity getMaster() {
        return asEntity();
    }

    @Override
    public Optional<UUID> getMasterId() {
        return Optional.of(asEntity().getUuid());
    }

    public void forceRespawnOnRaceChange() {
        if (isSpawnInvalid(getOrigin())) {
            BlockPos respawnPos = entity.getWorldSpawnPos((ServerWorld)asWorld(), getOrigin());
            if (!isSpawnInvalid(respawnPos)) {
                Vec3d pos = respawnPos.toBottomCenterPos();
                entity.updatePosition(pos.x, pos.y, pos.z);
            }
        }
        onSpawn();
    }

    public void onSpawn() {
        if (isSpawnInvalid(getOrigin())) {
            Race suppressedRace = getSuppressedRace();
            if (suppressedRace != Race.UNSET) {
                setSpecies(suppressedRace);
            }
        }
        ticksSunImmunity = INITIAL_SUN_IMMUNITY;
    }

    public boolean isSpawnInvalid(BlockPos pos) {
        return (entity.getWorld() instanceof ServerWorld sw && sw.getDimension().hasSkyLight() && sw.getServer().getSaveProperties().getGameMode() != GameMode.ADVENTURE)
            && ((getCompositeRace().includes(Race.BAT) && MeteorlogicalUtil.isPositionExposedToSun(asWorld(), pos))
            || (getCompositeRace().includes(Race.SEAPONY) && !asWorld().getFluidState(pos).isIn(FluidTags.WATER)));
    }

    @Override
    public boolean beforeUpdate() {
        if (compositeRace.includes(Race.UNSET) || entity.age % 2 == 0) {
            recalculateCompositeRace();
        }

        if (ticksInvulnerable > 0) {
            entity.setInvulnerable(--ticksInvulnerable > 0);
        }

        if (isClient()) {
            if (entity.hasVehicle() && entity.isSneaking()) {

                @Nullable
                Entity vehicle = entity.getVehicle();

                if (vehicle instanceof Trap) {
                    if (((Trap)vehicle).attemptDismount(entity)) {
                        setCarrier((UUID)null);
                        entity.stopRiding();
                        entity.refreshPositionAfterTeleport(vehicle.getPos());
                        Living.transmitPassengers(vehicle);
                    } else {
                        entity.setSneaking(false);
                    }
                } else {
                    setCarrier((UUID)null);
                    entity.stopRiding();
                    if (vehicle != null) {
                        entity.refreshPositionAfterTeleport(vehicle.getPos());
                    }
                    Living.transmitPassengers(vehicle);
                }
            }
        }

        magicExhaustion = ManaConsumptionUtil.burnFood(entity, magicExhaustion);

        powers.tick();
        acrobatics.tick();

        SeaponyGraceStatusEffect.update(entity);

        if (getObservedSpecies() == Race.KIRIN) {
            var charge = getMagicalReserves().getCharge();

            if (entity.isTouchingWater() || entity.isFrozen()) {
                charge.multiply(0.5F);
            }

            if (charge.getPercentFill() >= 1) {
                var energy = getMagicalReserves().getEnergy();
                if (energy.getPercentFill() < 0.002F) {
                    energy.addPercent(1.03F);
                    if (entity.age % 25 == 0) {
                        playSound(USounds.ENTITY_PLAYER_HEARTBEAT, 0.17F + (float)entity.getWorld().random.nextGaussian() * 0.03F, 0.5F);
                        spawnParticles(ParticleTypes.LAVA, 2);
                        energy.addPercent(1.07F);
                    }
                }
            }

            if (entity.getAttackCooldownProgress(0) == 0 && (entity.getAttacking() != null || entity.getWorld().random.nextInt(50) == 0)) {
                if (charge.getPercentFill() < 1) {
                    charge.addPercent(3);
                }

                if (!EquinePredicates.RAGING.test(entity) && charge.getPercentFill() >= 1 && entity.getWorld().random.nextInt(1000) == 0) {
                    SpellType.RAGE.withTraits().apply(this, CastingMethod.INNATE);
                }
            }
        }

        if (getCompositeRace().includes(Race.SEAPONY)) {
            if (entity.isSubmergedInWater()) {
                if (entity.getVelocity().lengthSquared() > 0.02) {
                    spawnParticles(ParticleTypes.BUBBLE, 4);
                }
            } else {
                if (entity.getAir() == entity.getMaxAir()) {
                    entity.setAir(entity.getAir() - 1);
                }

                if (entity.age % 60 == 0) {
                    entity.playSound(SoundEvents.ENTITY_TURTLE_AMBIENT_LAND, 1, 1);
                }

                if (entity.getAir() == -20 && !asWorld().isClient) {
                    entity.setAir(0);
                    entity.damage((ServerWorld)asWorld(), entity.getDamageSources().dryOut(), 2);
                }
            }
        }

        return super.beforeUpdate();
    }

    private void recalculateCompositeRace() {
        Race intrinsicRace = getSpecies();
        Race suppressedRace = getSuppressedRace();
        compositeRace = MetamorphosisStatusEffect.getEffectiveRace(entity, getSpellSlot()
                .get(SpellPredicate.IS_MIMIC)
                .map(AbstractDisguiseSpell::getDisguise)
                .map(EntityAppearance::getAppearance)
                .flatMap(Pony::of)
                .map(Pony::getSpecies)
                .orElse(intrinsicRace)).composite(
              AmuletSelectors.UNICORN_AMULET.test(entity) ? Race.UNICORN
            : AmuletSelectors.ALICORN_AMULET.test(entity) ? Race.ALICORN
            : null,
            AmuletSelectors.PEARL_NECKLACE.test(entity) ? suppressedRace.or(Race.SEAPONY) : null
        );
        UCriteria.PLAYER_CHANGE_RACE.trigger(entity);
    }

    @Override
    public Optional<BlockPos> chooseClimbingPos() {
        if (getObservedSpecies() == Race.CHANGELING && getSpellSlot().get(SpellPredicate.IS_DISGUISE).isEmpty()) {
            if (acrobatics.isFaceClimbable(entity.getWorld(), entity.getBlockPos(), entity.getHorizontalFacing()) || acrobatics.canHangAt(entity.getBlockPos())) {
                return Optional.of(entity.getBlockPos());
            }
        }
        return super.chooseClimbingPos();
    }

    private void updateAnimations() {

        if (acrobatics.distanceClimbed > 0
                && ((animation.isOf(Animation.CLIMB) && entity.isSneaking()) || animation.isOf(Animation.HANG))
                && entity.getClimbingPos().isPresent()
                && entity.getVelocity().length() < 0.08F) {
            if (animation.renderBothArms()) {
                animationDuration = 2;
            }
            return;
        }

        if (animationDuration <= 0 || --animationDuration <= 0) {

            if (animation.renderBothArms() && acrobatics.distanceClimbed > 0) {
                return;
            }

            if (!getAnimation().isOf(Animation.NONE)) {
                setAnimation(AnimationInstance.NONE);
            }
        }
    }

    private void updateBatPonyAbilities() {
        if (ticksSunImmunity > 0) {
            ticksSunImmunity--;
        }

        if (getObservedSpecies() == Race.BAT && !entity.hasPortalCooldown()) {
            boolean hasShades = TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE).anyMatch(s -> s.stack().isIn(UTags.Items.SHADES));
            if (!this.hasShades && hasShades && getObservedSpecies() == Race.BAT) {
                UCriteria.WEAR_SHADES.trigger(entity);
            }
            this.hasShades = hasShades;

            if (!hasShades && ticksSunImmunity <= 0 && MeteorlogicalUtil.isLookingIntoSun(asWorld(), entity)) {
                if (!isClient()) {
                    entity.addStatusEffect(new StatusEffectInstance(UEffects.SUN_BLINDNESS, SunBlindnessStatusEffect.MAX_DURATION, 2, true, false));
                    UCriteria.LOOK_INTO_SUN.trigger(entity);
                } else if (isClientPlayer()) {
                    InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_EARS_RINGING, entity.getId());
                }
            }

            if (SunBlindnessStatusEffect.hasSunExposure(entity)) {
                if (ticksInSun < 200) {
                    ticksInSun++;
                }

                if (ticksInSun == 1) {
                    if (!isClient()) {
                        entity.addStatusEffect(new StatusEffectInstance(UEffects.SUN_BLINDNESS, SunBlindnessStatusEffect.MAX_DURATION, 1, true, false));
                    } else if (isClientPlayer()) {
                        InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_EARS_RINGING, entity.getId());
                    }
                }
            } else if (ticksInSun > 0) {
                ticksInSun--;
            }
        }
    }

    private void updateCorruptionDecay() {

    }

    @Override
    public void tick() {
        super.tick();

        Race currentRace = getSpecies();
        if (!currentRace.isUnset()) {
            Race newRace = currentRace.validate(entity);

            if (newRace != currentRace) {
                setSpecies(newRace);
            }
        }
    }

    @Override
    public boolean canBeSeenBy(Entity entity) {
        if (entity instanceof HostileEntity hostile
                && getSpecies() == Race.BAT
                && hostile.getTarget() != this.entity
                && hostile.getAttacker() != this.entity
                && entity.distanceTo(this.entity) > entity.getWidth()) {
            if (entity.isSneaking() && entity.distanceTo(this.entity) > 4) {
                return false;
            }

            float vel = (float)getPhysics().getHorizontalMotion();
            float velocityScale = MathHelper.clamp(vel * 15, 0, 1);
            int light = asWorld().getLightLevel(getPhysics().getHeadPosition());
            float lightScale = light / 15F;
            float approachFactor = ((velocityScale + lightScale) / 2F);

            if (approachFactor < (entity.isSneaking() ? 0.8F : 0.3F)) {
                return false;
            }
        }
        return super.canBeSeenBy(entity);
    }

    @Override
    public Optional<Vec3d> adjustMovementSpeedInWater(Vec3d speed) {
        if (getObservedSpecies() == Race.KIRIN) {
            return Optional.of(speed.multiply(0.5, 1, 0.5));
        }
        if (getCompositeRace().includes(Race.SEAPONY)) {
            float factor = entity.isSwimming() ? 1.132F : 1.0232F;
            float max = 0.6F;
            return Optional.of(new Vec3d(
                    MathHelper.clamp(speed.x * factor, -max, max),
                    speed.y * (speed.y > 0 ? 1.2 : 1.101),
                    MathHelper.clamp(speed.z * factor, -max, max)
            ));
        }
        return Optional.empty();
    }

    public Optional<Living<?>> getEntityInArms() {
        return Living.getOrEmpty(entity.getFirstPassenger()).filter(Living::isBeingCarried);
    }

    @Override
    public boolean onUpdatePassengerPosition(Entity passender, Entity.PositionUpdater positionUpdater) {
        Entity passenger = entity.getFirstPassenger();
        if (Living.getOrEmpty(passenger).filter(Living::isBeingCarried).isPresent()) {

            Vec3d carryPosition = new Vec3d(0, 0, entity.getWidth());

            float leanAmount = ((LivingEntityDuck)entity).getLeaningPitch();
            carryPosition = carryPosition.rotateX(-leanAmount * MathHelper.PI / 4F)
                    .add(new Vec3d(0, -0.5F, 0).multiply(leanAmount));

            carryPosition = carryPosition.rotateY(-entity.getBodyYaw() * MathHelper.RADIANS_PER_DEGREE);

            carryPosition = entity.getPos().add(carryPosition);
            positionUpdater.accept(passenger, carryPosition.x, carryPosition.y, carryPosition.z);
            return true;
        }
        return false;
    }

    public int getImplicitEnchantmentLevel(RegistryEntry<Enchantment> enchantment, int initial) {

        if ((enchantment == Enchantments.AQUA_AFFINITY
                || enchantment == Enchantments.DEPTH_STRIDER
                || enchantment == Enchantments.LUCK_OF_THE_SEA
                || enchantment == Enchantments.LURE) && getCompositeRace().includes(Race.SEAPONY)) {
            return MathHelper.clamp(initial + 3, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
        }

        return initial;
    }

    public Optional<Float> modifyDamage(DamageSource cause, float amount) {

        if (getObservedSpecies() == Race.KIRIN) {
            var charge = getMagicalReserves().getCharge();
            charge.addPercent(MathHelper.clamp(amount / 10F, 5, 15));
            float anger = charge.getPercentFill();
            getMagicalReserves().getEnergy().addPercent(50 * anger);
            playSound(USounds.ENTITY_PLAYER_KIRIN_RAGE, 0.2F, 1.25F);
            spawnParticles(ParticleTypes.LAVA, 2);

            if (anger > 0 && entity.getWorld().random.nextFloat() < anger / 2F) {
                if (consumeSuperMove()) {
                    SpellType.RAGE.withTraits().apply(this, CastingMethod.INNATE);
                }
            }
        }

        if (EffectUtils.hasExtraDefenses(entity)
                && !cause.isIn(DamageTypeTags.BYPASSES_SHIELD)
                && !cause.isOf(DamageTypes.MAGIC)
                && !cause.isIn(DamageTypeTags.IS_FIRE)
                && !cause.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !cause.isOf(DamageTypes.THORNS)
                && !cause.isOf(DamageTypes.FREEZE)) {

            amount /= (cause.isOf(DamageTypes.MOB_PROJECTILE) ? 3 : 2) * (entity.getHealth() < 5 ? 3 : 1);

            return Optional.of(amount);
        }
        return Optional.empty();
    }

    public void onDropItem(ItemEntity itemDropped) {
        Equine.of(itemDropped).ifPresent(eq -> {
            eq.setSpecies(getSpecies());
            eq.getPhysics().setBaseGravityModifier(gravity.getPersistantGravityModifier());
        });
    }

    @Override
    public float onImpact(float distance, float damageMultiplier, DamageSource cause) {
        distance = super.onImpact(distance, damageMultiplier, cause);

        if (EffectUtils.hasExtraDefenses(entity)) {
            double radius = distance / 10;
            if (radius > 0) {
                EarthPonyStompAbility.spawnEffectAround(this, entity, entity.getSteppingPos(), radius, radius);
            }
        }

        return distance;
    }

    @Override
    protected float getEffectiveFallDistance(float distance) {
        boolean extraProtection = getSpellSlot().get(SpellType.SHIELD).isPresent();

        if (!entity.isCreative() && !entity.isSpectator()) {

            if (extraProtection) {
                distance /= (getLevel().getScaled(3) + 1);
                if (entity.isSneaking()) {
                    distance /= 2;
                }
            }

            if (getCompositeRace().canFly() || EffectUtils.hasExtraDefenses(entity)) {
                distance -= 5;
            }
        }

        return Math.max(0, distance);
    }

    public FoodComponent onEat(ItemStack stack, FoodComponent food) {
        if (isClient()) {
            return food;
        }

        if (getObservedSpecies() == Race.KIRIN
                && (stack.isIn(UTags.Items.COOLS_OFF_KIRINS) || stack.get(DataComponentTypes.POTION_CONTENTS) == PotionContentsComponent.DEFAULT)) {
            getMagicalReserves().getCharge().multiply(0.5F);
            getSpellSlot().get(SpellType.RAGE).ifPresent(RageAbilitySpell::setExtenguishing);
        }

        PonyDiets.getInstance().getEffects(stack, this).ailment().effects().afflict(asEntity(), stack);

        return food;
    }

    public void onKill(Entity killedEntity, DamageSource damage) {
        if (killedEntity != null && killedEntity.getType() == EntityType.PHANTOM && getPhysics().isFlying()) {
            UCriteria.KILL_PHANTOM_WHILE_FLYING.trigger(entity);
        }
    }

    @Override
    public boolean subtractEnergyCost(double foodSubtract) {

        if (getSpellSlot().get(SpellPredicate.IS_CORRUPTING).isPresent()) {
            int corruptionTaken = (int)(foodSubtract * (AmuletSelectors.ALICORN_AMULET.test(entity) ? 0.9F : 0.5F));
            foodSubtract -= corruptionTaken;
            getCorruption().add(corruptionTaken);
        }

        List<Pony> partyMembers = FriendshipBraceletItem.getPartyMembers(this, 10).toList();

        if (!partyMembers.isEmpty()) {
            foodSubtract /= (partyMembers.size() + 1);
            for (Pony member : partyMembers) {
                member.directTakeEnergy(foodSubtract);
            }
        }

        directTakeEnergy(foodSubtract);

        return entity.isCreative() || (entity.getHealth() > 1 && mana.getMana().getPercentFill() > 0.1F);
    }

    protected void directTakeEnergy(double foodSubtract) {
        if (!entity.isCreative() && !entity.getWorld().isClient) {
            magicExhaustion += ManaConsumptionUtil.consumeMana(mana.getMana(), foodSubtract);
        }
    }

    @Override
    protected Stream<ItemStack> getInventoryStacks() {
        return Streams.concat(
                super.getInventoryStacks(),
                entity.getInventory().main.stream()
        );
    }

    @Override
    protected void giveBackItem(ItemStack stack) {
        if (!entity.giveItemStack(stack)) {
            entity.dropItem(stack, false);
        }
    }

    public Optional<Text> trySleep(BlockPos pos) {

        if (AmuletSelectors.ALICORN_AMULET.test(entity)) {
            return Optional.of(Text.translatable("block.unicopia.bed.not_tired"));
        }

        return findAllSpellsInRange(10)
                .filter(p -> p instanceof Pony && ((Pony)p).isEnemy(this))
                .findFirst()
                .map(p -> Text.translatable("block.unicopia.bed.not_safe"));
    }

    public ActionResult canSleepNow() {
        if (!asWorld().isClient && ((ServerWorld)asWorld()).getGameRules().getBoolean(UGameRules.DO_NOCTURNAL_BAT_PONIES) && getSpecies().isNocturnal()) {
            return asWorld().isDay() || asWorld().getAmbientDarkness() >= 4 ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isEnemy(Affine other) {
        return getArmour().contains(UItems.ALICORN_AMULET) || super.isEnemy(other);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("mana", mana.toNBT(lookup));
        compound.putInt("levels", levels.get());
        compound.putInt("corruption", corruption.get());
        compound.put("advancementTriggerCounts", NbtSerialisable.encode(TriggerCountTracker.CODEC, advancementProgress, lookup));
        super.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        levels.set(compound.getInt("levels"));
        corruption.set(compound.getInt("corruption"));
        mana.fromNBT(compound.getCompound("mana"), lookup);
        advancementProgress = NbtSerialisable.decode(TriggerCountTracker.CODEC, compound.get("advancementTriggerCounts"), lookup).orElseGet(() -> new TriggerCountTracker(Map.of()));
        super.fromNBT(compound, lookup);
    }

    @Override
    public void toSyncronisedNbt(NbtCompound compound, WrapperLookup lookup) {
        super.toSyncronisedNbt(compound, lookup);
        compound.putString("playerSpecies", Race.REGISTRY.getId(getSpecies()).toString());
        compound.putString("suppressedSpecies", Race.REGISTRY.getId(getSuppressedRace()).toString());
        compound.putFloat("magicExhaustion", magicExhaustion);
        compound.putInt("ticksInSun", ticksInSun);
        compound.putBoolean("hasShades", hasShades);
        compound.put("acrobatics", acrobatics.toNBT(lookup));
        compound.put("powers", powers.toNBT(lookup));
        compound.put("gravity", gravity.toNBT(lookup));
        compound.put("charms", charms.toNBT(lookup));
        compound.put("discoveries", discoveries.toNBT(lookup));
        compound.putInt("ticksInvulnerable", ticksInvulnerable);
        compound.putInt("ticksMetamorphising", ticksMetamorphising);
    }

    @Override
    public void fromSynchronizedNbt(NbtCompound compound, WrapperLookup lookup) {
        super.fromSynchronizedNbt(compound, lookup);
        setSpecies(Race.fromName(compound.getString("playerSpecies"), Race.HUMAN));
        setSuppressedRace(Race.fromName(compound.getString("suppressedSpecies"), Race.UNSET));
        powers.fromNBT(compound.getCompound("powers"), lookup);
        gravity.fromNBT(compound.getCompound("gravity"), lookup);
        charms.fromNBT(compound.getCompound("charms"), lookup);
        discoveries.fromNBT(compound.getCompound("discoveries"), lookup);
        acrobatics.fromNBT(compound.getCompound("acrobatics"), lookup);
        magicExhaustion = compound.getFloat("magicExhaustion");
        ticksInvulnerable = compound.getInt("ticksInvulnerable");
        ticksInSun = compound.getInt("ticksInSun");
        hasShades = compound.getBoolean("hasShades");
        ticksMetamorphising = compound.getInt("ticksMetamorphising");
    }

    @Override
    public void copyFrom(Pony oldPlayer, boolean alive) {
        boolean forcedSwap = (!alive
                && entity instanceof ServerPlayerEntity
                && ((ServerWorld)entity.getWorld()).getGameRules().getBoolean(UGameRules.SWAP_TRIBE_ON_DEATH)
                && oldPlayer.respawnRace.isUnset())
                || oldPlayer.getSpecies().isUnset();

        Race oldSuppressedRace = oldPlayer.getSuppressedRace();
        Race newRace = oldPlayer.respawnRace != Race.UNSET && !alive ? oldPlayer.respawnRace : oldPlayer.getSpecies();

        if (forcedSwap || !newRace.canCast()) {
            getSpellSlot().clear();
        } else {
            getSpellSlot().copyFrom(oldPlayer.getSpellSlot(), alive);
        }

        if (forcedSwap) {
            oldSuppressedRace = Race.UNSET;
            Channel.SERVER_SELECT_TRIBE.sendToPlayer(new MsgTribeSelect(Race.allPermitted(entity), "gui.unicopia.tribe_selection.respawn"), (ServerPlayerEntity)entity);
        }

        if (!alive) {
            // putting it here instead of adding another injection point into ServerPlayerEntity.copyFrom()
            if (!((ServerWorld)asWorld()).getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                PlayerInventory inventory = oldPlayer.asEntity().getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (EnchantmentUtil.consumeEnchantment(entryFor(UEnchantments.HEART_BOUND), 1, stack, entity.getWorld().random, EnchantmentUtil.getLuck(3, oldPlayer.asEntity()))) {
                        asEntity().getInventory().setStack(i, stack);
                    }
                }
            }
        }

        setSpecies(newRace);
        setSuppressedRace(oldSuppressedRace);
        getDiscoveries().copyFrom(oldPlayer.getDiscoveries(), alive);
        getPhysics().copyFrom(oldPlayer.getPhysics(), alive);
        if (!forcedSwap) {
            getArmour().copyFrom(oldPlayer.getArmour(), alive);
            getCharms().copyFrom(oldPlayer.getCharms(), alive);
            corruption.set(oldPlayer.getCorruption().get());
            levels.set(oldPlayer.getLevel().get());
        }

        mana.copyFrom(oldPlayer.mana, !forcedSwap);
        advancementProgress.copyFrom(oldPlayer.advancementProgress, alive);
        setDirty();
        onSpawn();
    }

    @Override
    public void onSpellAdded(Spell spell) {
        if (spell.getAffinity() == Affinity.BAD && entity.getWorld().random.nextInt(20) == 0) {
            getCorruption().add(entity.getRandom().nextBetween(1, 10));
        }
        getCorruption().add(((int)spell.getTypeAndTraits().traits().getCorruption() * 10) + spell.getTypeAndTraits().type().getAffinity().getCorruption());
    }

    public boolean isClientPlayer() {
        return InteractionManager.getInstance().isClientPlayer(asEntity());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Pony of(@Nullable PlayerEntity player) {
        return player == null ? null : ((Container<Pony>)player).get();
    }

    public static Stream<Pony> stream(Stream<Entity> entities) {
        return entities.flatMap(entity -> of(entity).stream());
    }

    public static Optional<Pony> of(Entity entity) {
        return Equine.<Entity, Pony>of(entity, a -> a instanceof Pony);
    }

    public static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    public static boolean equal(Entity one, Entity two) {
        return Objects.equals(one, two) || (one instanceof PlayerEntity && two instanceof PlayerEntity && equal((PlayerEntity)one, (PlayerEntity)two));
    }

    public static boolean equal(PlayerEntity one, PlayerEntity two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
