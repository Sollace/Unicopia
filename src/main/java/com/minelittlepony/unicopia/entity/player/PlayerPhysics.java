package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.block.data.ModificationType;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.*;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;

public class PlayerPhysics extends EntityPhysics<PlayerEntity> implements Tickable, Motion, NbtSerialisable {
    private static final int MAX_WALL_HIT_CALLDOWN = 30;
    private static final int MAX_TICKS_TO_GLIDE = 20;
    private static final int IDLE_FLAP_INTERVAL = 20;
    private static final int GLIDING_SOUND_INTERVAL = 200;

    private int ticksInAir;
    private int ticksToGlide;

    private float thrustScale = 0;

    private boolean flapping;
    private boolean isCancelled;

    private int prevStrafe;
    private float strafe;

    private FlightType lastFlightType = FlightType.NONE;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

    private boolean soundPlaying;

    private int wallHitCooldown;

    private Vec3d lastPos = Vec3d.ZERO;
    private Vec3d lastVel = Vec3d.ZERO;

    private final PlayerDimensions dimensions;

    private final Pony pony;

    public PlayerPhysics(Pony pony) {
        super(pony.getMaster(), Creature.GRAVITY);
        this.pony = pony;
        dimensions = new PlayerDimensions(pony, this);
    }

    @Override
    public PlayerDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival && !entity.isFallFlying() && !entity.hasVehicle();
    }

    @Override
    public boolean isGliding() {
        return ticksToGlide <= 0 && isFlying() && !entity.isSneaking();
    }

    @Override
    public boolean isRainbooming() {
        return pony.getSpellSlot().get(SpellType.RAINBOOM, true).isPresent();
    }

    @Override
    public float getWingAngle() {

        if (pony.getAnimation() == Animation.SPREAD_WINGS) {
            return AnimationUtil.seeSitSaw(pony.getAnimationProgress(1), 1.5F);
        }

        float spreadAmount = -0.5F;

        if (isFlying()) {
            if (getFlightType() == FlightType.INSECTOID) {
                spreadAmount += Math.sin(pony.getEntity().age * 4F) * 8;
            } else {
                if (isGliding()) {
                    spreadAmount += 2.5F;
                } else {
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

        if (UItems.PEGASUS_AMULET.isApplicable(entity)) {
            return FlightType.ARTIFICIAL;
        }

        return pony.getSpellSlot().get(true)
            .filter(effect -> !effect.isDead() && effect instanceof FlightType.Provider)
            .map(effect -> ((FlightType.Provider)effect).getFlightType())
            .filter(FlightType::isPresent)
            .orElse(pony.getSpecies().getFlightType());
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

        pony.setDirty();
        entity.sendAbilitiesUpdate();
    }

    private double getHorizontalMotion() {
        return lastVel.horizontalLengthSquared();
    }

    @Override
    public void tick() {
        super.tick();

        if (wallHitCooldown > 0) {
            wallHitCooldown--;
        }
        if (ticksToGlide > 0) {
            ticksToGlide--;
        }

        lastVel = entity.getPos().subtract(lastPos);
        lastPos = entity.getPos();

        final MutableVector velocity = new MutableVector(entity.getVelocity());

        if (isGravityNegative() && !entity.isSneaking() && entity.isInSneakingPose()) {
            float currentHeight = entity.getDimensions(entity.getPose()).height;
            float sneakingHeight = entity.getDimensions(EntityPose.STANDING).height;

            entity.setPos(entity.getX(), entity.getY() + currentHeight - sneakingHeight, entity.getZ());
            entity.setPose(EntityPose.STANDING);
        }

        FlightType type = getFlightType();

        boolean typeChanged = type != lastFlightType && (lastFlightType.isArtifical() || type.isArtifical());

        if (typeChanged) {
            pony.spawnParticles(ParticleTypes.CLOUD, 10);

            entity.playSound(entity.world.getDimension().ultrawarm() ? USounds.ITEM_ICARUS_WINGS_CORRUPT : USounds.ITEM_ICARUS_WINGS_PURIFY, 0.1125F, 1.5F);
        }

        entity.getAbilities().allowFlying = type.canFlyCreative(entity);

        boolean creative = entity.getAbilities().creativeMode || entity.isSpectator();

        boolean startedFlyingCreative = !creative && isFlyingEither != entity.getAbilities().flying;

        if (!creative) {
            if (entity.isOnGround() || isCancelled) {
                cancelFlight(false);
            }

            if (entity.isOnGround()) {
                isCancelled = false;
            }

            entity.getAbilities().flying |= (type.canFly() || entity.getAbilities().allowFlying) && isFlyingEither;
            if (!type.canFly() && (type != lastFlightType)) {
                entity.getAbilities().flying = false;
            }

            if ((entity.isOnGround() && entity.isSneaking())
                    || entity.isTouchingWater()
                    || entity.horizontalCollision
                    || (entity.verticalCollision && (pony.getSpecies() != Race.BAT || velocity.y < 0))) {

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

        lastFlightType = type;
        isFlyingSurvival = entity.getAbilities().flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.getAbilities().flying);

        if (typeChanged || startedFlyingCreative) {
            entity.calculateDimensions();
        }

        if (type.canFly()) {
            if (isFlying()) {
                if (pony.getSpecies() == Race.BAT && entity.verticalCollision && pony.canHangAt(pony.getOrigin().up(2))) {
                    EntityAttributeInstance attr = entity.getAttributeInstance(UEntityAttributes.ENTITY_GRAVTY_MODIFIER);

                    if (!attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
                        attr.addPersistentModifier(PlayerAttributes.BAT_HANGING);
                        entity.setVelocity(Vec3d.ZERO);
                        return;
                    }
                }

                ticksInAir++;
                tickFlight(type, velocity);

                int strafing = (int)Math.signum(entity.sidewaysSpeed);
                if (strafing != prevStrafe) {
                    prevStrafe = strafing;
                    strafe = 1;
                    ticksToGlide = MAX_TICKS_TO_GLIDE;
                    if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                        entity.playSound(type.getWingFlapSound(), 0.25F, 1);
                    }
                } else {
                    strafe *= 0.28;
                }
            } else {
                prevStrafe = 0;
                strafe = 0;
                ticksInAir = 0;
                wallHitCooldown = MAX_WALL_HIT_CALLDOWN;
                soundPlaying = false;

                if (!creative && type.isAvian()) {
                    checkAvianTakeoffConditions(velocity);
                }
            }
        } else {
            soundPlaying = false;
        }

        if (!entity.isOnGround()) {
            float heavyness = 1 - EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) * 0.015F;
            velocity.x *= heavyness;
            velocity.z *= heavyness;
        }

        entity.setVelocity(velocity.toImmutable());

        if (isFlying() && !entity.isFallFlying() && !pony.isHanging() && pony.isClient()) {
            if (MineLPDelegate.getInstance().getPlayerPonyRace(entity).isDefault() && getHorizontalMotion() > 0.03) {
                float pitch = ((LivingEntityDuck)entity).getLeaningPitch();
                if (pitch < 1) {
                    if (pitch < 0.9F) {
                        pitch += 0.1F;
                    }
                    pitch += 0.09F;
                    ((LivingEntityDuck)entity).setLeaningPitch(Math.max(0, pitch));
                }
            }

            entity.limbAngle = 20 + (float)Math.cos(entity.age / 7F) - 0.5F;
            entity.limbDistance = thrustScale;
        }
    }

    private void tickFlight(FlightType type, MutableVector velocity) {
        if (type.isArtifical()) {
            tickArtificialFlight(velocity);
        } else {
            tickNaturalFlight(velocity);
        }

        entity.fallDistance = 0;

        if (type.isAvian()) {
            applyThrust(velocity);

            if (pony.getSpecies() != Race.BAT && entity.world.random.nextInt(9000) == 0) {
                entity.dropItem(UItems.PEGASUS_FEATHER);
                entity.playSound(USounds.ENTITY_PLAYER_PEGASUS_MOLT, 0.3F, 1);
                UCriteria.SHED_FEATHER.trigger(entity);
            }
        }

        moveFlying(velocity);

        if (entity.world.hasRain(entity.getBlockPos())) {
            applyTurbulance(velocity);
        }

        if (type.isAvian()) {
            if (entity.world.isClient && ticksInAir % IDLE_FLAP_INTERVAL == 0 && entity.getVelocity().length() < 0.29) {
                flapping = true;
                ticksToGlide = MAX_TICKS_TO_GLIDE;
            }

            if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                if (ticksInAir % GLIDING_SOUND_INTERVAL == 1 && pony.isClient()) {
                    InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_GLIDING, entity.getId());
                }
            }

            velocity.y -= 0.02 * getGravitySignum();
            velocity.x *= 0.9896;
            velocity.z *= 0.9896;
        } else if (type == FlightType.INSECTOID && !SpellPredicate.IS_DISGUISE.isOn(pony)) {
            if (entity.world.isClient && !soundPlaying) {
                soundPlaying = true;
                InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_CHANGELING_BUZZ, entity.getId());
            }
        }
    }

    private void tickArtificialFlight(MutableVector velocity) {
        if (ticksInAir % 10 == 0 && !entity.world.isClient) {
            ItemStack stack = entity.getEquippedStack(EquipmentSlot.CHEST);

            int damageInterval = 20;
            int minDamage = 1;

            float energyConsumed = 2 + (float)getHorizontalMotion() / 10F;
            if (entity.world.hasRain(entity.getBlockPos())) {
                energyConsumed *= 3;
            }
            if (entity.world.getDimension().ultrawarm()) {
                energyConsumed *= 4;
                damageInterval /= 2;
                minDamage *= 3;
            }

            AmuletItem.consumeEnergy(stack, energyConsumed);

            if (AmuletItem.getEnergy(stack) < 9) {
                entity.playSound(USounds.ITEM_ICARUS_WINGS_WARN, 0.13F, 0.5F);
            }

            if (entity.world.random.nextInt(damageInterval) == 0) {
                stack.damage(minDamage + entity.world.random.nextInt(50), entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
            }

            if (!getFlightType().canFly()) {
                entity.playSound(USounds.ITEM_ICARUS_WINGS_EXHAUSTED, 1, 2);
                cancelFlight(false);
            }
        }
    }

    private void tickNaturalFlight(MutableVector velocity) {
        float level = pony.getLevel().getScaled(5) + 1;

        if (ticksInAir > (level * 100)) {
            Bar mana = pony.getMagicalReserves().getMana();

            float cost = (float)-getHorizontalMotion() * 20F / level;
            if (entity.isSneaking()) {
                cost /= 10;
            }

            mana.add(cost);

            if (mana.getPercentFill() < 0.2) {
                pony.getMagicalReserves().getExertion().add(2);
                pony.getMagicalReserves().getExhaustion().add(2 + (int)(getHorizontalMotion() * 50));

                if (mana.getPercentFill() < 0.1 && ticksInAir % 10 == 0) {
                    float exhaustion = (0.3F * ticksInAir) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    entity.addExhaustion(exhaustion);
                }
            }
        }
    }

    private void checkAvianTakeoffConditions(MutableVector velocity) {
        double horMotion = getHorizontalMotion();
        double motion = entity.getPos().subtract(lastPos).lengthSquared();

        boolean takeOffCondition = velocity.y > 0
                && (horMotion > 0.2 || (motion > 0.2 && velocity.y < -0.02 * getGravitySignum()));
        boolean fallingTakeOffCondition = !entity.isOnGround() && velocity.y < -1.6 * getGravitySignum();

        if ((takeOffCondition || fallingTakeOffCondition) && !pony.isHanging() && !isCancelled) {
            startFlying(false);

            if (!isGravityNegative()) {
                velocity.y += horMotion + 0.3;
            }
            applyThrust(velocity);

            velocity.x *= 0.2;
            velocity.z *= 0.2;
        }
    }

    public void startFlying(boolean force) {
        if (force) {
            isCancelled = false;
        }
        entity.getAbilities().flying = true;
        isFlyingEither = true;
        isFlyingSurvival = true;
        entity.calculateDimensions();
    }

    private void handleWallCollission(MutableVector velocity) {
        if (wallHitCooldown > 0) {
            return;
        }

        BlockPos pos = new BlockPos(entity.getCameraPosVec(1).add(entity.getRotationVec(1).normalize().multiply(2)));

        BlockState state = entity.world.getBlockState(pos);

        if (!entity.world.isAir(pos) && Block.isFaceFullSquare(state.getCollisionShape(entity.world, pos), entity.getHorizontalFacing().getOpposite())) {
            double motion = Math.sqrt(getHorizontalMotion());

            float distance = (float)(motion * 20 - 3);

            float bouncyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.PADDED, entity) * 6;

            if (distance > 0) {
                wallHitCooldown = MAX_WALL_HIT_CALLDOWN;

                if (bouncyness > 0) {
                    entity.playSound(USounds.ENTITY_PLAYER_REBOUND, 1, 1);
                    ProjectileUtil.ricochet(entity, Vec3d.of(pos), 0.4F + Math.min(2, bouncyness / 18F));
                    velocity.fromImmutable(entity.getVelocity());
                    distance /= bouncyness;
                } else {
                    entity.playSound(distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL, 1, 1);
                }
                entity.damage(DamageSource.FLY_INTO_WALL, distance);
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
        velocity.y -= (0.01F / Math.max(motion * 100, 1)) * getGravityModifier();
        velocity.z += forward * MathHelper.cos(entity.getYaw() * 0.017453292F);
    }

    private void applyThrust(MutableVector velocity) {
        if (pony.sneakingChanged() && entity.isSneaking()) {
            flapping = true;
            ticksToGlide = MAX_TICKS_TO_GLIDE;
        }

        thrustScale *= 0.2889F;

        if (thrustScale <= 0.000001F & flapping) {
            flapping = false;
            if (!SpellPredicate.IS_DISGUISE.isOn(pony)) {
                entity.playSound(getFlightType().getWingFlapSound(), 0.5F, 1);
            }
            thrustScale = 1;
        }

        float heavyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) / 6F;
        float thrustStrength = 0.135F * thrustScale;
        if (heavyness > 0) {
            thrustStrength /= heavyness;
        }

        Vec3d direction = entity.getRotationVec(1).normalize().multiply(thrustStrength);

        if (entity.getVelocity().horizontalLength() > 0.1) {
            velocity.x += direction.x;
            velocity.z += direction.z;
            velocity.y += (direction.y * 2.45 + Math.abs(direction.y) * 10) * getGravitySignum() - heavyness / 5F;
        }

        if (entity.isSneaking()) {
            if (!isGravityNegative()) {
                velocity.y += 0.4 - 0.25;
            }
            if (pony.sneakingChanged()) {
                velocity.y += 0.75 * getGravitySignum();
            }
        } else {
            velocity.y -= 0.1 * getGravitySignum();
        }

        if (velocity.y < 0 && entity.getVelocity().horizontalLength() < 0.1) {
            velocity.y *= 0.01;
        }

    }

    private void applyTurbulance(MutableVector velocity) {
        float glance = 360 * entity.world.random.nextFloat();
        float forward = 0.015F * entity.world.random.nextFloat() *  entity.world.getRainGradient(1);

        if (entity.world.random.nextInt(30) == 0) {
            forward *= 10;
        }
        if (entity.world.random.nextInt(30) == 0) {
            forward *= 10;
        }
        if (entity.world.random.nextInt(40) == 0) {
            forward *= 100;
        }

        if (entity.world.isThundering() && entity.world.random.nextInt(60) == 0) {
            velocity.y += forward * 3 * getGravitySignum();
        }

        if (forward >= 1) {
            entity.world.playSound(null, entity.getBlockPos(), USounds.AMBIENT_WIND_GUST, SoundCategory.AMBIENT, 3, 1);
        }

        forward = Math.min(forward, 7);
        forward /= 1 + (EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) * 0.8F);

        velocity.x += - forward * MathHelper.sin((entity.getYaw() + glance) * 0.017453292F);
        velocity.z += forward * MathHelper.cos((entity.getYaw() + glance) * 0.017453292F);

        if (!entity.world.isClient && entity.world.isThundering() && entity.world.random.nextInt(9000) == 0) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.world);
            lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());

            entity.world.spawnEntity(lightning);
            UCriteria.LIGHTNING_STRUCK.trigger(entity);
        }
    }

    /**
     * Called when a player's species changes to update whether they can fly or not
     */
    public void updateFlightState() {
        FlightType type = getFlightType();
        entity.getAbilities().allowFlying = type.canFlyCreative(entity);
        entity.getAbilities().flying &= type.canFly() || entity.getAbilities().allowFlying;
        isFlyingSurvival = entity.getAbilities().flying;
    }

    public void dashForward(float speed) {
        if (pony.isClient()) {
            return;
        }

        Vec3d orientation = entity.getRotationVec(1).multiply(speed);
        entity.addVelocity(orientation.x, orientation.y, orientation.z);

        int damage = TraceHelper.findBlocks(entity, speed + 4, 1, state -> state.isIn(UTags.GLASS_PANES)).stream()
            .flatMap(pos -> BlockPos.streamOutwards(pos, 2, 2, 2))
            .filter(pos -> entity.world.getBlockState(pos).isOf(Blocks.GLASS_PANE))
            .reduce(0, (u, pos) -> {
                if (pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
                    entity.world.breakBlock(pos, true);
                } else {
                    ParticleUtils.spawnParticles(new MagicParticleEffect(0x00AAFF), entity.world, Vec3d.ofCenter(pos), 15);
                }
                return 1;
            }, Integer::sum);

        if (damage > 0) {
            pony.subtractEnergyCost(damage / 5F);
            entity.damage(DamageSource.FLY_INTO_WALL, Math.min(damage, entity.getHealth() - 1));
            UCriteria.BREAK_WINDOW.trigger(entity);
        }

        pony.updateVelocity();
        pony.playSound(USounds.ENTITY_PLAYER_PEGASUS_DASH, 1);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isCancelled", isCancelled);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        isFlyingSurvival = compound.getBoolean("isFlying");
        isCancelled = compound.getBoolean("isCancelled");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        ticksInAir = compound.getInt("ticksInAir");

        entity.calculateDimensions();
    }
}
