package com.minelittlepony.unicopia.entity.player;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.compat.ad_astra.OxygenUtils;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.input.Heuristic;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.ChargeableItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgPlayerFlightControlsInput;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.util.*;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;

public class PlayerPhysics extends EntityPhysics<PlayerEntity> implements Tickable, Motion, NbtSerialisable {
    private static final int MAX_WALL_HIT_CALLDOWN = 30;
    private static final int MAX_TICKS_TO_GLIDE = 20;
    private static final int MAX_TICKS_TO_WEATHER_EFFECTS = 100;
    private static final int IDLE_FLAP_INTERVAL = 20;
    private static final int GLIDING_SOUND_INTERVAL = 200;

    private int ticksInAir;
    private int ticksToGlide;
    private int ticksDiving;

    private float thrustScale = 0;
    private float prevThrustScale;

    private boolean flapping;
    private boolean isCancelled;

    private int prevStrafe;
    private float strafe;

    private float descentRate;

    private FlightType lastFlightType = FlightType.NONE;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

    private boolean soundPlaying;

    private int wallHitCooldown;

    private Vec3d lastPos = Vec3d.ZERO;
    private Vec3d lastVel = Vec3d.ZERO;

    private final PlayerDimensions dimensions;

    private final Pony pony;

    private Lerp updraft = new Lerp(0);
    private Lerp windStrength = new Lerp(0);

    public PlayerPhysics(Pony pony) {
        super(pony.asEntity(), Creature.GRAVITY);
        this.pony = pony;
        dimensions = new PlayerDimensions(pony, this);
    }

    @Override
    public PlayerDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public Vec3d getClientVelocity() {
        return lastVel;
    }

    public final float getPersistantGravityModifier() {
        return super.getGravityModifier();
    }

    public float getFlapCooldown(float tickDelta) {
        float lerpedThrust = MathHelper.lerp(tickDelta, prevThrustScale, thrustScale);
        return lerpedThrust <= 0.000001F ? 0 : lerpedThrust;
    }

    @Override
    public float getGravityModifier() {
        float modifier = getPersistantGravityModifier();
        if (pony.getAcrobatics().isHanging() && pony.getObservedSpecies() == Race.BAT) {
            modifier *= -0.05F;
        }
        return modifier;
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival
                && !entity.isFallFlying()
                && !entity.hasVehicle()
                && !entity.getAbilities().creativeMode
                && !entity.isSpectator();
    }

    @Override
    public boolean isGliding() {
        return ticksToGlide <= 0 && isFlying() && !pony.getJumpingHeuristic().getState();
    }

    @Override
    public boolean isDiving() {
        return ticksDiving > 0;
    }

    @Override
    public boolean isRainbooming() {
        return SpellType.RAINBOOM.isOn(pony);
    }

    @Override
    public float getWingAngle() {

        if (pony.getAnimation().isOf(Animation.SPREAD_WINGS)) {
            return AnimationUtil.seeSitSaw(pony.getAnimationProgress(1), 1.5F);
        }

        float spreadAmount = -0.5F;

        if (isFlying()) {
            if (lastFlightType == FlightType.INSECTOID) {
                spreadAmount += Math.sin(pony.asEntity().age * 4F) * 8;
            } else {
                if (isGliding()) {
                    spreadAmount += MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine() ? -0.8F : 2.5F;
                } else {
                    if (MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine()) {
                        spreadAmount -= 1.8F;
                    }
                    spreadAmount += strafe * 10;
                    spreadAmount += thrustScale * 24;
                }
            }
        } else {
            spreadAmount += MathHelper.clamp(-lastVel.y, -0.3F, 2);
            spreadAmount += Math.sin(entity.age / 9F) / 9F;

            if (entity.isSneaking()) {
                spreadAmount += 2;
            }
        }

        spreadAmount = MathHelper.clamp(spreadAmount, -2, 6);

        return pony.getInterpolator().interpolate("wingSpreadAmount", spreadAmount, 10);
    }

    public FlightType getFlightType() {
        return lastFlightType;
    }

    private FlightType recalculateFlightType() {
        DimensionType dimension = entity.getWorld().getDimension();

        if ((RegistryUtils.isIn(entity.getWorld(), dimension, RegistryKeys.DIMENSION_TYPE, UTags.HAS_NO_ATMOSPHERE)
                || Unicopia.getConfig().dimensionsWithoutAtmosphere.get().contains(RegistryUtils.getId(entity.getWorld(), dimension, RegistryKeys.DIMENSION_TYPE).toString()))
                && !OxygenUtils.API.hasOxygen(entity)) {
            return FlightType.NONE;
        }

        if (UItems.PEGASUS_AMULET.isApplicable(entity)) {
            return FlightType.ARTIFICIAL;
        }

        return pony.getSpellSlot().get(true)
            .filter(effect -> !effect.isDead() && effect instanceof FlightType.Provider)
            .map(effect -> ((FlightType.Provider)effect).getFlightType())
            .filter(FlightType::isPresent)
            .orElse(pony.getCompositeRace().flightType());
    }

    public void cancelFlight(boolean force) {
        if (force) {
            isCancelled = true;
        }
        boolean wasFlying = isFlyingEither;
        entity.getAbilities().flying = false;
        isFlyingEither = false;
        isFlyingSurvival = false;
        strafe = 0;
        thrustScale = 0;

        if (wasFlying) {
            entity.calculateDimensions();
        }
    }

    public double getHorizontalMotion() {
        return getClientVelocity().horizontalLengthSquared();
    }

    @Override
    public void tick() {
        super.tick();

        if (pony.isClientPlayer() && isFlying() && (pony.getJumpingHeuristic().hasChanged(Heuristic.ONCE) || pony.sneakingChanged())) {
            Channel.FLIGHT_CONTROLS_INPUT.sendToServer(new MsgPlayerFlightControlsInput(pony));
        }

        prevThrustScale = thrustScale;

        if (wallHitCooldown > 0) {
            wallHitCooldown--;
        }
        if (ticksToGlide > 0) {
            ticksToGlide--;
        }

        lastVel = entity.getPos().subtract(lastPos);
        lastPos = entity.getPos();

        final MutableVector velocity = new MutableVector(entity.getVelocity());

        FlightType type = recalculateFlightType();

        boolean typeChanged = type != lastFlightType;

        if (typeChanged && (lastFlightType.isArtifical() || type.isArtifical())) {
            pony.spawnParticles(ParticleTypes.CLOUD, 10);

            playSound(entity.getWorld().getDimension().ultrawarm() ? USounds.ITEM_ICARUS_WINGS_CORRUPT : USounds.ITEM_ICARUS_WINGS_PURIFY, 0.1125F, 1.5F);
        }

        lastFlightType = type;

        entity.getAbilities().allowFlying = lastFlightType.canFlyCreative(entity);

        boolean creative = entity.isCreative() || entity.isSpectator();
        boolean startedFlyingCreative = !creative && isFlyingEither != entity.getAbilities().flying;

        if (!creative) {
            if (entity.isOnGround() || isCancelled) {
                cancelFlight(false);
            }

            if (entity.isOnGround()) {
                isCancelled = false;
            }

            entity.getAbilities().flying |= (lastFlightType.canFly() || entity.getAbilities().allowFlying) && isFlyingEither;
            if (!lastFlightType.canFly() && typeChanged) {
                entity.getAbilities().flying = false;
            }

            if ((entity.isOnGround() && entity.isSneaking())
                    || entity.isTouchingWater()
                    || entity.horizontalCollision
                    || (entity.verticalCollision && (pony.getObservedSpecies() != Race.BAT || velocity.y < 0))) {

                if (entity.getAbilities().flying && entity.horizontalCollision) {
                    handleWallCollission(velocity);
                    return;
                }

                cancelFlight(false);
            }
        }

        if (isGravityNegative()) {
            if (entity.isOnGround() || (!creative && entity.horizontalCollision)) {
                cancelFlight(false);
            }

            if (entity.isClimbing() && (entity.horizontalCollision || ((LivingEntityDuck)entity).isJumping())) {
                velocity.y = -0.2F;
            }
        }

        isFlyingSurvival = entity.getAbilities().flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.getAbilities().flying);

        if (typeChanged || startedFlyingCreative) {
            entity.calculateDimensions();
        }

        if (lastFlightType.canFly()) {
            if (isFlying()) {
                ticksInAir++;
                tickFlight(lastFlightType, velocity);

                int strafing = (int)Math.signum(entity.sidewaysSpeed);
                if (strafing != prevStrafe) {
                    prevStrafe = strafing;
                    strafe = 1;
                    ticksToGlide = MAX_TICKS_TO_GLIDE;
                    if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                        if (lastFlightType != FlightType.INSECTOID) {
                            playSound(lastFlightType.getWingFlapSound(), 0.25F, entity.getSoundPitch() * lastFlightType.getWingFlapSoundPitch());
                        }
                        entity.getWorld().emitGameEvent(entity, GameEvent.ELYTRA_GLIDE, entity.getPos());
                    }
                } else {
                    strafe *= 0.28;
                }

                if (((LivingEntityDuck)entity).isJumping()) {
                    velocity.y -= 0.2F;
                    velocity.y /= 2F;
                }

                double horizontalSpeed = this.getHorizontalMotion();
                double verticalSpeed = velocity.y;

                if (Abilities.RAINBOOM.canUse(pony.getCompositeRace()) && horizontalSpeed != 0 && verticalSpeed < -0.3F && (verticalSpeed / horizontalSpeed) < -0.3F) {
                    ticksDiving++;
                } else {
                    ticksDiving = 0;
                }

                if (ticksDiving > 0 && ticksDiving % 25 == 0) {
                    pony.getMagicalReserves().getCharge().addPercent(12.5F);
                }
            } else {
                prevStrafe = 0;
                strafe = 0;
                ticksInAir = 0;
                wallHitCooldown = MAX_WALL_HIT_CALLDOWN;
                soundPlaying = false;
                descentRate = 0;
                ticksDiving = 0;
                updraft.update(0, 100);
                windStrength.update(0, 100);

                if (Abilities.RAINBOOM.canUse(pony.getCompositeRace()) && entity.isOnGround()) {
                    pony.getMagicalReserves().getCharge().set(0);
                }

                if (!creative) {
                    checkAvianTakeoffConditions(velocity);
                }
            }
        } else {
            descentRate = 0;
            soundPlaying = false;
            updraft.update(0, 100);
            windStrength.update(0, 100);
        }

        if (!entity.isOnGround()) {
            float heavyness = 1 - EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) * 0.015F;
            velocity.x /= heavyness;
            velocity.z /= heavyness;
        }

        entity.setVelocity(velocity.toImmutable());

        if (isFlying() && !entity.isFallFlying() && !pony.getAcrobatics().isHanging() && pony.isClient()) {
            if (!MineLPDelegate.getInstance().getPlayerPonyRace(entity).isEquine() && getHorizontalMotion() > 0.03) {
                float pitch = ((LivingEntityDuck)entity).getLeaningPitch();
                if (pitch < 1) {
                    if (pitch < 0.9F) {
                        pitch += 0.1F;
                    }
                    pitch += 0.09F;
                    ((LivingEntityDuck)entity).setLeaningPitch(Math.max(0, pitch));
                }
            }

            LimbAnimationUtil.resetToZero(entity.limbAnimator);
        }
    }

    private void tickFlight(FlightType type, MutableVector velocity) {
        if (type.isArtifical()) {
            tickArtificialFlight(velocity);
        } else {
            tickNaturalFlight(velocity);
        }

        entity.fallDistance = 0;

        applyThrust(velocity);

        if (type.isAvian()) {
            if (pony.getObservedSpecies() != Race.BAT && entity.getWorld().random.nextInt(9000) == 0) {
                entity.dropItem(pony.getObservedSpecies() == Race.HIPPOGRIFF ? UItems.GRYPHON_FEATHER : UItems.PEGASUS_FEATHER);
                playSound(USounds.ENTITY_PLAYER_PEGASUS_MOLT, 0.3F, 1);
                UCriteria.SHED_FEATHER.trigger(entity);
            }
        }

        moveFlying(velocity);

        if (entity.getWorld().isClient && ticksInAir % IDLE_FLAP_INTERVAL == 0 && entity.getVelocity().length() < 0.29) {
            flapping = true;
            ticksToGlide = MAX_TICKS_TO_GLIDE;
        }

        if (type.isAvian()) {
            if (!SpellPredicate.IS_DISGUISE.isOn(pony) && pony.isClient()) {
                if (ticksInAir % GLIDING_SOUND_INTERVAL == 5) {
                    InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_GLIDING, entity.getId());
                }
            }
        } else if (type == FlightType.INSECTOID && !SpellPredicate.IS_DISGUISE.isOn(pony)) {
            if (entity.getWorld().isClient && !soundPlaying) {
                soundPlaying = true;
                InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_CHANGELING_BUZZ, entity.getId());
            }
        }

        velocity.y -= 0.02;
        velocity.x *= 0.9896;
        velocity.z *= 0.9896;
    }

    private void tickArtificialFlight(MutableVector velocity) {
        if (ticksInAir % 10 == 0 && !entity.getWorld().isClient) {
            ItemStack stack = AmuletItem.getForEntity(entity);
            if (ChargeableItem.getEnergy(stack) < 9) {
                playSound(USounds.ITEM_ICARUS_WINGS_WARN, 0.13F, 0.5F);
            }

            int damageInterval = 20;
            int minDamage = 1;

            float energyConsumed = 2 + (float)getHorizontalMotion() / 10F;
            if (entity.getWorld().hasRain(entity.getBlockPos())) {
                energyConsumed *= 3;
            }
            if (entity.getWorld().getDimension().ultrawarm()) {
                energyConsumed *= 4;
                damageInterval /= 2;
                minDamage *= 3;
            }

            ChargeableItem.consumeEnergy(stack, energyConsumed);

            if (entity.getWorld().random.nextInt(damageInterval) == 0) {
                stack.damage(minDamage + entity.getWorld().random.nextInt(50), entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
            }

            if (!lastFlightType.canFly()) {
                playSound(USounds.ITEM_ICARUS_WINGS_EXHAUSTED, 1, 2);
                cancelFlight(false);
            }
        }
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        entity.getWorld().playSoundFromEntity(entity, entity, sound, SoundCategory.PLAYERS, volume, pitch);
    }

    private void tickNaturalFlight(MutableVector velocity) {
        float level = pony.getLevel().getScaled(5) + 1;

        if (ticksInAir > (level * 100)) {
            Bar mana = pony.getMagicalReserves().getMana();

            float cost = (float)-getHorizontalMotion() / 2F;
            if (((LivingEntityDuck)entity).isJumping()) {
                cost /= 10;
            }

            mana.add(MathHelper.clamp(cost, -100, 0));

            if (mana.getPercentFill() < 0.2) {
                pony.getMagicalReserves().getExertion().addPercent(2);
                pony.getMagicalReserves().getExhaustion().add(2 + (int)(getHorizontalMotion() * 50));

                if (mana.getPercentFill() < 0.1 && ticksInAir % 10 == 0) {
                    float exhaustion = (0.3F * ticksInAir) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    entity.addExhaustion(exhaustion);
                }

                if (pony.getMagicalReserves().getExhaustion().get() > 99 && ticksInAir % 25 == 0) {
                    entity.damage(pony.damageOf(UDamageTypes.EXHAUSTION), 2);

                    if (entity.getWorld().random.nextInt(110) == 1 && !pony.isClient()) {
                        pony.getLevel().add(1);
                        if (Abilities.RAINBOOM.canUse(pony.getCompositeRace())) {
                            pony.getMagicalReserves().getCharge().addPercent(4);
                        }
                        pony.getMagicalReserves().getExertion().set(0);
                        pony.getMagicalReserves().getExhaustion().set(0);
                        mana.set(mana.getMax() * 100);
                        UCriteria.SECOND_WIND.trigger(entity);
                    }
                }
            }
        }
    }

    private void checkAvianTakeoffConditions(MutableVector velocity) {
        double horMotion = getHorizontalMotion();
        double motion = entity.getPos().subtract(lastPos).lengthSquared();

        boolean takeOffCondition =
                   (horMotion > 0.05 || motion > 0.05)
                && pony.getJumpingHeuristic().hasChanged(Heuristic.TWICE);
        boolean fallingTakeOffCondition = !entity.isOnGround() && velocity.y < -1.6 && entity.fallDistance > 1;

        if ((takeOffCondition || fallingTakeOffCondition) && !pony.getAcrobatics().isHanging() && !isCancelled) {
            initiateTakeoff(velocity);
        }
    }

    private void initiateTakeoff(MutableVector velocity) {
        startFlying(false);

        velocity.y += getHorizontalMotion() + 0.3;
        applyThrust(velocity);

        velocity.x *= 0.2;
        velocity.z *= 0.2;
    }

    public void startFlying(boolean force) {
        if (force) {
            isCancelled = false;
        }
        entity.getAbilities().flying = true;
        isFlyingEither = true;
        isFlyingSurvival = true;
        thrustScale = 0;
        descentRate = 0;
        entity.calculateDimensions();

        if (entity.isOnGround() || !force) {
            BlockState steppingState = pony.asEntity().getSteppingBlockState();
            if (steppingState.isIn(UTags.KICKS_UP_DUST)) {
                pony.addParticle(new BlockStateParticleEffect(UParticles.DUST_CLOUD, steppingState), pony.getOrigin().toCenterPos(), Vec3d.ZERO);
            } else {
                Supplier<Vec3d> pos = VecHelper.sphere(pony.asWorld().getRandom(), 0.5D);
                Supplier<Vec3d> vel = VecHelper.sphere(pony.asWorld().getRandom(), 0.015D);
                pony.spawnParticles(ParticleTypes.CLOUD, pos, vel, 5);
            }
        }
    }

    private void handleWallCollission(MutableVector velocity) {
        if (wallHitCooldown > 0) {
            return;
        }

        BlockPos pos = BlockPos.ofFloored(entity.getCameraPosVec(1).add(entity.getRotationVec(1).normalize().multiply(2)));

        BlockState state = entity.getWorld().getBlockState(pos);

        if (!entity.getWorld().isAir(pos) && Block.isFaceFullSquare(state.getCollisionShape(entity.getWorld(), pos), entity.getHorizontalFacing().getOpposite())) {
            double motion = Math.sqrt(getHorizontalMotion());

            float distance = (float)(motion * 20 - 3);

            float bouncyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.PADDED, entity) * 6;

            if (distance > 0) {
                wallHitCooldown = MAX_WALL_HIT_CALLDOWN;

                if (bouncyness > 0) {
                    playSound(USounds.ENTITY_PLAYER_REBOUND, 1, entity.getSoundPitch());
                    ProjectileUtil.ricochet(entity, Vec3d.of(pos), 0.4F + Math.min(2, bouncyness / 18F));
                    velocity.fromImmutable(entity.getVelocity());
                    distance /= bouncyness;
                } else {
                    LivingEntity.FallSounds fallSounds = entity.getFallSounds();
                    playSound(distance > 4 ? fallSounds.big() : fallSounds.small(), 1, entity.getSoundPitch());
                }
                entity.damage(entity.getDamageSources().flyIntoWall(), distance);
            }
        }

        entity.setVelocity(velocity.toImmutable());
        cancelFlight(false);
    }

    private void moveFlying(MutableVector velocity) {
        double motion = getHorizontalMotion();

        float forward = 0.000015F * (1 + (pony.getLevel().getScaled(10) / 10F)) * (float)Math.sqrt(motion);

        // vertical drop due to gravity
        forward += 0.005F;

        velocity.x += - forward * MathHelper.sin(entity.getYaw() * 0.017453292F);
        velocity.z += forward * MathHelper.cos(entity.getYaw() * 0.017453292F);

        if (entity.getWorld().hasRain(entity.getBlockPos())) {
            applyTurbulance(velocity);
        } else {
            float targetUpdraft = (float)WeatherConditions.getUpdraft(new BlockPos.Mutable().set(entity.getBlockPos()), entity.getWorld()) / 3F;
            targetUpdraft *= 1 + motion;
            if (isGravityNegative()) {
                targetUpdraft *= -1;
            }
            this.updraft.update(targetUpdraft, targetUpdraft > this.updraft.getTarget() ? 30_000 : 3000);
            double updraft = this.updraft.getValue();
            velocity.y += updraft;
            descentRate -= updraft;
        }

        descentRate += 0.001F;
        descentRate = Math.min(1.5F, descentRate);
        if (descentRate < 0) {
            descentRate *= 0.8F;
        }

        velocity.y -= descentRate;
    }

    private void applyThrust(MutableVector velocity) {
        boolean manualFlap = pony.getJumpingHeuristic().hasChanged(Heuristic.ONCE) && pony.getJumpingHeuristic().getState();
        if (manualFlap) {
            flapping = true;
            ticksToGlide = MAX_TICKS_TO_GLIDE;
        }

        thrustScale *= 0.2889F;

        boolean hovering = entity.getVelocity().horizontalLength() < 0.1;

        if (lastFlightType == FlightType.INSECTOID) {
            descentRate = pony.getJumpingHeuristic().getState() ? -0.5F : 0;
        } else {
            if (thrustScale <= 0.000001F & flapping) {
                flapping = false;
                if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                    playSound(lastFlightType.getWingFlapSound(), 0.25F, entity.getSoundPitch() * lastFlightType.getWingFlapSoundPitch());
                    entity.getWorld().emitGameEvent(entity, GameEvent.ELYTRA_GLIDE, entity.getPos());
                }
                thrustScale = 1;
                if (manualFlap) {
                    descentRate -= 0.5;
                } else {
                    descentRate = Math.max(0, descentRate / 2);
                }
            }
        }

        float heavyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity);
        float thrustStrength = 0.235F * thrustScale;

        if (heavyness > 0) {
            thrustStrength /= 1 + heavyness;
        }

        Vec3d direction = entity.getRotationVec(1).normalize().multiply(thrustStrength);

        if (hovering) {
            if (entity.isSneaking()) {
                velocity.y -= 0.2F;
            }
        } else {
            velocity.x += direction.x * 1.3F;
            velocity.z += direction.z * 1.3F;
            velocity.y += ((direction.y * 2.45 + Math.abs(direction.y) * 10));// - heavyness / 5F
        }

        if (velocity.y < 0 && hovering) {
            velocity.y *= 0.01;
        }
    }

    private void applyTurbulance(MutableVector velocity) {
        int globalEffectStrength = MathHelper.clamp(entity.getWorld().getGameRules().getInt(UGameRules.WEATHER_EFFECTS_STRENGTH), 0, 100);
        float effectStrength = Math.min(1, (float)ticksInAir / MAX_TICKS_TO_WEATHER_EFFECTS) * (globalEffectStrength / 100F);
        Vec3d gust = WeatherConditions.getGustStrength(entity.getWorld(), entity.getBlockPos())
                .multiply(globalEffectStrength / 100D)
                .multiply(1 / (1 + Math.floor(pony.getLevel().get() / 10F)));





        if (effectStrength * gust.getX() >= 1) {
            SoundEmitter.playSoundAt(entity, USounds.AMBIENT_WIND_GUST, SoundCategory.AMBIENT, 3, 1);
        }

        float weight = 1 + (EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) * 0.8F) + (pony.getCompositeRace().canUseEarth() ? 1 : 0);

        Vec3d airflow = WeatherConditions.getAirflow(entity.getBlockPos(), entity.getWorld())
                .multiply(0.04F * effectStrength)
                .add(Vec3d.fromPolar(
                    (entity.getPitch() + (float)gust.getY()) * MathHelper.RADIANS_PER_DEGREE,
                    (entity.getYaw() + (float)gust.getZ()) * MathHelper.RADIANS_PER_DEGREE
                ).multiply(effectStrength * (float)gust.getX() / weight));

        windStrength.update((float)airflow.length(), airflow.length() > windStrength.getValue() ? 1000 : 500);
        velocity.add(airflow.normalize(), windStrength.getValue());

        if (!entity.getWorld().isClient && effectStrength > 0.9F && entity.getWorld().isThundering() && entity.getWorld().random.nextInt(9000) == 0) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
            lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());

            entity.getWorld().spawnEntity(lightning);
            UCriteria.LIGHTNING_STRUCK.trigger(entity);
        }
    }

    /**
     * Called when a player's species changes to update whether they can fly or not
     */
    public void updateFlightState() {
        FlightType type = recalculateFlightType();
        entity.getAbilities().allowFlying = type.canFlyCreative(entity);
        entity.getAbilities().flying &= type.canFly() || entity.getAbilities().allowFlying;
        isFlyingSurvival = entity.getAbilities().flying;
        lastFlightType = type;
    }

    public void dashForward(float speed) {
        if (pony.isClient()) {
            return;
        }

        Vec3d orientation = entity.getRotationVec(1).multiply(speed);
        entity.addVelocity(orientation.x, orientation.y, orientation.z);

        boolean isEarthPonySmash = pony.getObservedSpecies().canUseEarth() && !isFlying();
        int damage = TraceHelper.findBlocks(entity, speed + 4, 1, state -> (isEarthPonySmash && !state.isAir()) || state.isIn(ConventionalBlockTags.GLASS_PANES)).stream()
            .flatMap(pos -> BlockPos.streamOutwards(pos, 2, 2, 2))
            .filter(pos -> (isEarthPonySmash && !entity.getWorld().isAir(pos)) || entity.getWorld().getBlockState(pos).isIn(ConventionalBlockTags.GLASS_PANES))
            .reduce(0, (u, pos) -> {
                if (pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
                    if (isEarthPonySmash) {
                        BlockDestructionManager.of(entity.getWorld()).damageBlock(pos, (int)entity.getWorld().getRandom().nextTriangular(5, 3));
                        if (BlockDestructionManager.of(entity.getWorld()).getBlockDestruction(pos) >= 9) {
                            entity.getWorld().breakBlock(pos, true);
                        }
                    } else {
                        entity.getWorld().breakBlock(pos, true);
                    }
                } else {
                    ParticleUtils.spawnParticles(new MagicParticleEffect(0x00AAFF), entity.getWorld(), Vec3d.ofCenter(pos), 15);
                }
                return 1;
            }, Integer::sum);

        if (damage > 0) {
            pony.subtractEnergyCost(damage / 5F);
            entity.damage(entity.getDamageSources().flyIntoWall(), Math.min(damage, entity.getHealth() - 1));
            if (!isEarthPonySmash) {
                UCriteria.BREAK_WINDOW.trigger(entity);
            }
        }

        if (isEarthPonySmash) {
            DamageSource damageSource = pony.damageOf(UDamageTypes.STEAMROLLER);
            pony.findAllEntitiesInRange(speed + 4, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).forEach(e -> e.damage(damageSource, 50));
        }

        pony.updateVelocity();

        if (isFlying()) {
            playSound(USounds.ENTITY_PLAYER_PEGASUS_DASH, 1, 1);
        } else {
            playSound(USounds.ENTITY_PLAYER_EARTHPONY_DASH, 2, 0.3F);
        }
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isCancelled", isCancelled);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
        compound.putFloat("descentRate", descentRate);
        compound.putFloat("updraft", updraft.getValue());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        isFlyingSurvival = compound.getBoolean("isFlying");
        isCancelled = compound.getBoolean("isCancelled");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        ticksInAir = compound.getInt("ticksInAir");
        descentRate = compound.getFloat("descentRate");
        updraft.update(compound.getFloat("updraft"), 0);

        entity.calculateDimensions();
    }
}
