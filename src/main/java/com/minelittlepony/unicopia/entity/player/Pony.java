package com.minelittlepony.unicopia.entity.player;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.client.render.PlayerPoser.AnimationInstance;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.*;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.RageAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitDiscovery;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.MetamorphosisStatusEffect;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.network.*;
import com.minelittlepony.unicopia.network.datasync.EffectSync.UpdateCallback;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.common.util.animation.LinearInterpolator;
import com.google.common.collect.Streams;
import com.minelittlepony.common.util.animation.Interpolator;
import com.mojang.authlib.GameProfile;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class Pony extends Living<PlayerEntity> implements Copyable<Pony>, UpdateCallback {
    private static final TrackedData<String> RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> SUPPRESSED_RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

    static final TrackedData<Float> ENERGY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXHAUSTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXERTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> MANA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> XP = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> CHARGE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Integer> LEVEL = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Integer> CORRUPTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    static final int INITIAL_SUN_IMMUNITY = 20;

    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    private final AbilityDispatcher powers = new AbilityDispatcher(this);
    private final PlayerPhysics gravity = addTicker(new PlayerPhysics(this));
    private final PlayerCharmTracker charms = new PlayerCharmTracker(this);
    private final PlayerCamera camera = new PlayerCamera(this);
    private final TraitDiscovery discoveries = new TraitDiscovery(this);
    private final Acrobatics acrobatics = new Acrobatics(this);
    private final CorruptionHandler corruptionHandler = new CorruptionHandler(this);

    private final Map<String, Integer> advancementProgress = new HashMap<>();

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

    public Pony(PlayerEntity player) {
        super(player, EFFECT);
        this.levels = new PlayerLevelStore(this, LEVEL, true, USounds.Vanilla.ENTITY_PLAYER_LEVELUP);
        this.corruption = new PlayerLevelStore(this, CORRUPTION, false, USounds.ENTITY_PLAYER_CORRUPTION);
        this.mana = addTicker(new ManaContainer(this));

        addTicker(this::updateAnimations);
        addTicker(this::updateBatPonyAbilities);
        addTicker(this::updateCorruptionDecay);
        addTicker(new PlayerAttributes(this));
        addTicker(corruptionHandler);
    }

    @Override
    public void initDataTracker() {
        super.initDataTracker();
        acrobatics.initDataTracker();
        mana.initDataTracker();
        entity.getDataTracker().startTracking(LEVEL, 0);
        entity.getDataTracker().startTracking(CORRUPTION, 0);
        entity.getDataTracker().startTracking(RACE, Race.DEFAULT_ID);
        entity.getDataTracker().startTracking(SUPPRESSED_RACE, Race.DEFAULT_ID);
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

    public Map<String, Integer> getAdvancementProgress() {
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
        return Race.fromName(entity.getDataTracker().get(RACE), Race.HUMAN);
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
    public void setSpecies(Race race) {
        race = race.validate(entity);
        Race current = getSpecies();
        entity.getDataTracker().set(RACE, race.getId().toString());
        if (race != current) {
            clearSuppressedRace();
        }

        ticksInSun = 0;

        gravity.updateFlightState();
        entity.sendAbilitiesUpdate();
        recalculateCompositeRace();
    }

    public void setSuppressedRace(Race race) {
        entity.getDataTracker().set(SUPPRESSED_RACE, race.validate(entity).getId().toString());
    }

    public void clearSuppressedRace() {
        setSuppressedRace(Race.UNSET);
    }

    public Race getSuppressedRace() {
        return Race.fromName(entity.getDataTracker().get(SUPPRESSED_RACE), Race.UNSET);
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

    @Override
    public void setDirty() {
        dirty = true;
    }

    private void sendCapabilities() {
        if (!dirty) {
            return;
        }
        dirty = false;

        if (entity instanceof ServerPlayerEntity) {
            MsgOtherPlayerCapabilities packet = new MsgOtherPlayerCapabilities(this);
            Channel.SERVER_PLAYER_CAPABILITIES.sendToPlayer(packet, (ServerPlayerEntity)entity);
            Channel.SERVER_OTHER_PLAYER_CAPABILITIES.sendToSurroundingPlayers(packet, entity);
        }
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

    public void onSpawn() {
        if (entity.getWorld() instanceof ServerWorld sw && sw.getServer().getSaveProperties().getGameMode() != GameMode.ADVENTURE) {
            boolean mustAvoidSun = getObservedSpecies() == Race.BAT && MeteorlogicalUtil.isPositionExposedToSun(sw, getOrigin());
            boolean mustAvoidAir = getCompositeRace().includes(Race.SEAPONY) && !sw.getFluidState(getOrigin()).isIn(FluidTags.WATER);
            if (mustAvoidSun || mustAvoidAir) {
                SpawnLocator.selectSpawnPosition(sw, entity, mustAvoidAir, mustAvoidSun);
                if ((mustAvoidAir && !sw.getFluidState(getOrigin()).isIn(FluidTags.WATER))
                 || (mustAvoidSun && MeteorlogicalUtil.isPositionExposedToSun(sw, getOrigin()))) {
                    Race suppressedRace = getSuppressedRace();
                    if (suppressedRace != Race.UNSET) {
                        setSpecies(suppressedRace);
                    }
                }
            }
        }
        ticksSunImmunity = INITIAL_SUN_IMMUNITY;
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

                if (entity.getAir() == -20) {
                    entity.setAir(0);
                    entity.damage(entity.getDamageSources().dryOut(), 2);
                }
            }
        }

        return super.beforeUpdate();
    }

    private void recalculateCompositeRace() {
        Race intrinsicRace = getSpecies();
        Race suppressedRace = getSuppressedRace();
        compositeRace = MetamorphosisStatusEffect.getEffectiveRace(entity, getSpellSlot()
                .get(SpellPredicate.IS_MIMIC, true)
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
        if (getObservedSpecies() == Race.CHANGELING && getSpellSlot().get(SpellPredicate.IS_DISGUISE, false).isEmpty()) {
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
            boolean hasShades = TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE).anyMatch(s -> s.isIn(UTags.Items.SHADES));
            if (!this.hasShades && hasShades && getObservedSpecies() == Race.BAT) {
                UCriteria.WEAR_SHADES.trigger(entity);
            }
            this.hasShades = hasShades;

            if (!hasShades && ticksSunImmunity <= 0 && MeteorlogicalUtil.isLookingIntoSun(asWorld(), entity)) {
                if (!isClient()) {
                    entity.addStatusEffect(new StatusEffectInstance(UEffects.SUN_BLINDNESS, SunBlindnessStatusEffect.MAX_DURATION, 2, true, false));
                    UCriteria.LOOK_INTO_SUN.trigger(entity);
                } else if (isClientPlayer()) {
                    InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_EARS_RINGING, entity.getId());
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
                        InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_EARS_RINGING, entity.getId());
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

        sendCapabilities();
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

            if (approachFactor < (entity.isSneaking() ? 0.8F : 0.6F)) {
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

    public int getImplicitEnchantmentLevel(Enchantment enchantment, int initial) {

        if ((enchantment == Enchantments.AQUA_AFFINITY
                || enchantment == Enchantments.DEPTH_STRIDER
                || enchantment == Enchantments.LUCK_OF_THE_SEA
                || enchantment == Enchantments.LURE) && getCompositeRace().includes(Race.SEAPONY)) {
            return MathHelper.clamp(initial + 3, enchantment.getMinLevel(), enchantment.getMaxLevel());
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

        if (!cause.isIn(DamageTypeTags.BYPASSES_SHIELD)
                && !cause.isOf(DamageTypes.MAGIC)
                && !cause.isIn(DamageTypeTags.IS_FIRE)
                && !cause.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !cause.isOf(DamageTypes.THORNS)
                && !cause.isOf(DamageTypes.FREEZE)) {

            if (getCompositeRace().canUseEarth() && entity.isSneaking()) {
                amount /= (cause.isOf(DamageTypes.MOB_PROJECTILE) ? 3 : 2) * (entity.getHealth() < 5 ? 3 : 1);

                return Optional.of(amount);
            }
        }
        return Optional.empty();
    }

    public void onDropItem(ItemEntity itemDropped) {
        Equine.of(itemDropped).ifPresent(eq -> {
            eq.setSpecies(getSpecies());
            eq.getPhysics().setBaseGravityModifier(gravity.getPersistantGravityModifier());
        });
    }

    public Optional<Float> onImpact(float distance, float damageMultiplier, DamageSource cause) {
        float originalDistance = distance;

        boolean extraProtection = getSpellSlot().get(SpellType.SHIELD, false).isPresent();

        if (!entity.isCreative() && !entity.isSpectator()) {

            if (extraProtection) {
                distance /= (getLevel().getScaled(3) + 1);
                if (entity.isSneaking()) {
                    distance /= 2;
                }
            }

            if (getCompositeRace().canFly() || (getCompositeRace().canUseEarth() && entity.isSneaking())) {
                distance -= 5;
            }
            distance = Math.max(0, distance);
        }

        handleFall(distance, damageMultiplier, cause);
        if (distance != originalDistance) {
            return Optional.of(distance);
        }
        return Optional.empty();
    }

    public void onEat(ItemStack stack) {
        if (isClient()) {
            return;
        }

        if (getObservedSpecies() == Race.KIRIN
                && (stack.isIn(UTags.Items.COOLS_OFF_KIRINS) || PotionUtil.getPotion(stack) == Potions.WATER)) {
            getMagicalReserves().getCharge().multiply(0.5F);
            getSpellSlot().get(SpellType.RAGE, false).ifPresent(RageAbilitySpell::setExtenguishing);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void handleFall(float distance, float damageMultiplier, DamageSource cause) {
        super.handleFall(distance, damageMultiplier, cause);

        if (getCompositeRace().canUseEarth() && entity.isSneaking()) {
            double radius = distance / 10;
            if (radius > 0) {
                EarthPonyStompAbility.spawnEffectAround(entity, entity.getLandingPos(), radius, radius);
            }
        }
    }

    public void onKill(Entity killedEntity, DamageSource damage) {
        if (killedEntity != null && killedEntity.getType() == EntityType.PHANTOM && getPhysics().isFlying()) {
            UCriteria.KILL_PHANTOM_WHILE_FLYING.trigger(entity);
        }
    }

    @Override
    public boolean subtractEnergyCost(double foodSubtract) {

        if (getSpellSlot().get(SpellPredicate.IS_CORRUPTING, false).isPresent()) {
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
        if (asWorld().getGameRules().getBoolean(UGameRules.DO_NOCTURNAL_BAT_PONIES) && getSpecies().isNocturnal()) {
            return asWorld().isDay() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isEnemy(Affine other) {
        return getArmour().contains(UItems.ALICORN_AMULET) || super.isEnemy(other);
    }

    @Override
    public void toSyncronisedNbt(NbtCompound compound) {
        super.toSyncronisedNbt(compound);
        compound.putString("playerSpecies", Race.REGISTRY.getId(getSpecies()).toString());
        compound.putString("suppressedSpecies", Race.REGISTRY.getId(getSuppressedRace()).toString());
        compound.putFloat("magicExhaustion", magicExhaustion);
        compound.putInt("ticksInSun", ticksInSun);
        compound.putBoolean("hasShades", hasShades);
        compound.put("acrobatics", acrobatics.toNBT());
        compound.put("powers", powers.toNBT());
        compound.put("gravity", gravity.toNBT());
        compound.put("charms", charms.toNBT());
        compound.put("discoveries", discoveries.toNBT());
        compound.put("mana", mana.toNBT());
        compound.putInt("levels", levels.get());
        compound.putInt("corruption", corruption.get());
        compound.putInt("ticksInvulnerable", ticksInvulnerable);
        compound.putInt("ticksMetamorphising", ticksMetamorphising);

        NbtCompound progress = new NbtCompound();
        advancementProgress.forEach((key, count) -> {
            progress.putInt(key, count);
        });
        compound.put("advancementProgress", progress);
    }

    @Override
    public void fromSynchronizedNbt(NbtCompound compound) {
        super.fromSynchronizedNbt(compound);
        setSpecies(Race.fromName(compound.getString("playerSpecies"), Race.HUMAN));
        setSuppressedRace(Race.fromName(compound.getString("suppressedSpecies"), Race.UNSET));
        powers.fromNBT(compound.getCompound("powers"));
        gravity.fromNBT(compound.getCompound("gravity"));
        charms.fromNBT(compound.getCompound("charms"));
        discoveries.fromNBT(compound.getCompound("discoveries"));
        levels.set(compound.getInt("levels"));
        corruption.set(compound.getInt("corruption"));
        mana.fromNBT(compound.getCompound("mana"));
        acrobatics.fromNBT(compound.getCompound("acrobatics"));
        magicExhaustion = compound.getFloat("magicExhaustion");
        ticksInvulnerable = compound.getInt("ticksInvulnerable");
        ticksInSun = compound.getInt("ticksInSun");
        hasShades = compound.getBoolean("hasShades");
        ticksMetamorphising = compound.getInt("ticksMetamorphising");

        NbtCompound progress = compound.getCompound("advancementProgress");
        advancementProgress.clear();
        for (String key : progress.getKeys()) {
            advancementProgress.put(key, progress.getInt(key));
        }
    }

    @Override
    public void copyFrom(Pony oldPlayer, boolean alive) {

        boolean forcedSwap = (!alive
                && entity instanceof ServerPlayerEntity
                && entity.getWorld().getGameRules().getBoolean(UGameRules.SWAP_TRIBE_ON_DEATH)
                && oldPlayer.respawnRace.isUnset())
                || oldPlayer.getSpecies().isUnset();

        Race oldSuppressedRace = oldPlayer.getSuppressedRace();

        if (alive) {
            oldPlayer.getSpellSlot().stream(true).forEach(getSpellSlot()::put);
        } else {
            if (forcedSwap) {
                oldSuppressedRace = Race.UNSET;
                Channel.SERVER_SELECT_TRIBE.sendToPlayer(new MsgTribeSelect(Race.allPermitted(entity), "gui.unicopia.tribe_selection.respawn"), (ServerPlayerEntity)entity);
            } else {
                oldPlayer.getSpellSlot().stream(true).filter(SpellPredicate.IS_PLACED).forEach(getSpellSlot()::put);
            }

            // putting it here instead of adding another injection point into ServerPlayerEntity.copyFrom()
            if (!asWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                PlayerInventory inventory = oldPlayer.asEntity().getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (EnchantmentUtil.consumeEnchantment(UEnchantments.HEART_BOUND, 1, stack, entity.getWorld().random, EnchantmentUtil.getLuck(3, oldPlayer.asEntity()))) {
                        asEntity().getInventory().setStack(i, stack);
                    }
                }
            }
        }

        setSpecies(oldPlayer.respawnRace != Race.UNSET && !alive ? oldPlayer.respawnRace : oldPlayer.getSpecies());
        setSuppressedRace(oldSuppressedRace);
        getDiscoveries().copyFrom(oldPlayer.getDiscoveries(), alive);
        getPhysics().copyFrom(oldPlayer.getPhysics(), alive);
        if (!forcedSwap) {
            getArmour().copyFrom(oldPlayer.getArmour(), alive);
            getCharms().equipSpell(Hand.MAIN_HAND, oldPlayer.getCharms().getEquippedSpell(Hand.MAIN_HAND));
            getCharms().equipSpell(Hand.OFF_HAND, oldPlayer.getCharms().getEquippedSpell(Hand.OFF_HAND));
            corruption.set(oldPlayer.getCorruption().get());
            levels.set(oldPlayer.getLevel().get());
        }

        mana.copyFrom(oldPlayer.mana, !forcedSwap);

        advancementProgress.putAll(oldPlayer.getAdvancementProgress());
        setDirty();
        onSpawn();
    }

    @Override
    public void onSpellSet(@Nullable Spell spell) {
        if (spell != null) {
            if (spell.getAffinity() == Affinity.BAD && entity.getWorld().random.nextInt(20) == 0) {
                getCorruption().add(entity.getRandom().nextBetween(1, 10));
            }
            getCorruption().add((int)spell.getTraits().getCorruption() * 10);
            setDirty();
        }
    }

    public boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(asEntity());
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
