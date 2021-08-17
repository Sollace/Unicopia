package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.Jumper;
import com.minelittlepony.unicopia.entity.Leaner;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
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
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerPhysics extends EntityPhysics<PlayerEntity> implements Tickable, Motion, NbtSerialisable {

    private int ticksInAir;

    private float thrustScale = 0;

    private FlightType lastFlightType = FlightType.NONE;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

    private boolean soundPlaying;

    private int wallHitCooldown;

    private Vec3d lastPos = Vec3d.ZERO;

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
        return isFlying() && (entity.isSneaking() || ((Jumper)entity).isJumping()) && !pony.sneakingChanged();
    }

    @Override
    public boolean isRainbooming() {
        return pony.getSpellSlot().get(SpellType.JOUSTING, true).isPresent();
    }

    @Override
    public float getWingAngle() {
        float spreadAmount = -0.5F;

        if (isFlying()) {
            if (getFlightType() == FlightType.INSECTOID) {
                spreadAmount += Math.sin(pony.getEntity().age * 4F) * 8;
            } else {
                spreadAmount += isGliding() ? 3 : thrustScale * 60;
            }
        } else {
            spreadAmount += MathHelper.clamp(-entity.getVelocity().y, 0, 2);
            spreadAmount += Math.sin(entity.age / 9F) / 9F;
        }

        if (entity.isSneaking()) {
            spreadAmount += 2;
        }

        spreadAmount = MathHelper.clamp(spreadAmount, -2, 5);

        return pony.getInterpolator().interpolate("wingSpreadAmount", spreadAmount, 10);
    }

    public FlightType getFlightType() {

        if (UItems.PEGASUS_AMULET.isApplicable(entity)) {
            return FlightType.ARTIFICIAL;
        }

        return pony.getSpellSlot().get(true)
            .filter(effect -> !effect.isDead() && effect instanceof FlightType.Provider)
            .map(effect -> ((FlightType.Provider)effect).getFlightType(pony))
            .orElse(pony.getSpecies().getFlightType());
    }

    private void cancelFlight() {
        entity.getAbilities().flying = false;
        isFlyingEither = false;
        isFlyingSurvival = false;
    }

    private void playSound(SoundEvent event, float volume, float pitch) {
        entity.world.playSoundFromEntity(null, entity, event, entity.getSoundCategory(), volume, pitch);
    }

    private double getHorizontalMotion(Entity e) {
        return e.getPos().subtract(lastPos).horizontalLengthSquared();
    }

    @Override
    public void tick() {
        super.tick();

        if (wallHitCooldown > 0) {
            wallHitCooldown--;
        }

        final MutableVector velocity = new MutableVector(entity.getVelocity());

        if (isGravityNegative() && !entity.isSneaking() && entity.isInSneakingPose()) {
            float currentHeight = entity.getDimensions(entity.getPose()).height;
            float sneakingHeight = entity.getDimensions(EntityPose.STANDING).height;

            entity.setPos(entity.getX(), entity.getY() + currentHeight - sneakingHeight, entity.getZ());
            entity.setPose(EntityPose.STANDING);
        }

        FlightType type = getFlightType();

        if (type != lastFlightType && (lastFlightType.isArtifical() || type.isArtifical())) {
            ParticleUtils.spawnParticles(ParticleTypes.CLOUD, entity, 10);

            entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), entity.world.getDimension().isUltrawarm() ? SoundEvents.BLOCK_BELL_USE : SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 0.1125F, 1.5F, true);
        }

        entity.getAbilities().allowFlying = type.canFlyCreative(entity);

        boolean creative = entity.getAbilities().creativeMode || entity.isSpectator();

        if (!creative) {
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

                cancelFlight();
            }
        }

        if (isGravityNegative()) {
            if (entity.isOnGround() || (!creative && entity.horizontalCollision)) {
                cancelFlight();
            }

            if (entity.isClimbing() && (entity.horizontalCollision || ((Jumper)entity).isJumping())) {
                velocity.y = -0.2F;
            }
        }

        lastFlightType = type;
        isFlyingSurvival = entity.getAbilities().flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.getAbilities().flying);

        if (type.canFly()) {
            if (isFlying()) {
                if (pony.getSpecies() == Race.BAT && entity.verticalCollision && pony.canHangAt(pony.getOrigin().up(2))) {
                    EntityAttributeInstance attr = entity.getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);

                    if (!attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
                        attr.addPersistentModifier(PlayerAttributes.BAT_HANGING);
                        entity.setVelocity(Vec3d.ZERO);
                        return;
                    }
                }

                ticksInAir++;
                tickFlight(type, velocity);
            } else {
                ticksInAir = 0;
                soundPlaying = false;

                if (!creative && type.isAvian()) {
                    checkAvianTakeoffConditions(velocity);
                }
            }
        } else {
            soundPlaying = false;
        }

        lastPos = new Vec3d(entity.getX(), 0, entity.getZ());

        if (!entity.isOnGround()) {
            float heavyness = 1 - EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) * 0.015F;
            velocity.x *= heavyness;
            velocity.z *= heavyness;
        }

        entity.setVelocity(velocity.toImmutable());

        if (isFlying() && !entity.isInSwimmingPose()) {
            float pitch = ((Leaner)entity).getLeaningPitch();
            if (pitch < 1) {
                ((Leaner)entity).setLeaningPitch(Math.max(0, pitch + 0.18F));
            }
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

            if (entity.world.random.nextInt(9000) == 0) {
                entity.dropItem(UItems.PEGASUS_FEATHER);
            }
        }

        moveFlying(velocity);

        if (entity.world.hasRain(entity.getBlockPos())) {
            applyTurbulance(velocity);
        }

        if (type.isAvian()) {
            if (entity.world.isClient && ticksInAir % 20 == 0 && entity.getVelocity().length() < 0.29) {
                entity.playSound(getFlightType().getWingFlapSound(), 0.5F, 1);
                thrustScale = 1;
            }
            velocity.y -= 0.02 * getGravitySignum();
            velocity.x *= 0.9896;
            velocity.z *= 0.9896;
        } else if (type == FlightType.INSECTOID) {
            if (entity.world.isClient && !soundPlaying) {
                soundPlaying = true;
                InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_CHANGELING_BUZZ);
            }
        }
    }

    private void tickArtificialFlight(MutableVector velocity) {
        if (ticksInAir % 10 == 0 && !entity.world.isClient) {
            ItemStack stack = entity.getEquippedStack(EquipmentSlot.CHEST);

            int damageInterval = 20;
            int minDamage = 1;

            float energyConsumed = 2 + (float)getHorizontalMotion(entity) / 10F;
            if (entity.world.hasRain(entity.getBlockPos())) {
                energyConsumed *= 3;
            }
            if (entity.world.getDimension().isUltrawarm()) {
                energyConsumed *= 4;
                damageInterval /= 2;
                minDamage *= 3;
            }

            AmuletItem.consumeEnergy(stack, energyConsumed);

            if (AmuletItem.getEnergy(stack) < 9) {
                playSound(SoundEvents.BLOCK_CHAIN_STEP, 0.13F, 0.5F);
            }

            if (entity.world.random.nextInt(damageInterval) == 0) {
                stack.damage(minDamage + entity.world.random.nextInt(50), entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
            }

            if (!getFlightType().canFly()) {
                playSound(SoundEvents.ITEM_SHIELD_BREAK, 1, 2);
                cancelFlight();
            }
        }
    }

    private void tickNaturalFlight(MutableVector velocity) {
        int level = pony.getLevel().get() + 1;

        if (ticksInAir > (level * 100)) {
            Bar mana = pony.getMagicalReserves().getMana();

            float cost = (float)-getHorizontalMotion(entity) * 20F / level;
            if (entity.isSneaking()) {
                cost /= 10;
            }

            mana.add(cost);

            if (mana.getPercentFill() < 0.2) {
                pony.getMagicalReserves().getExertion().add(2);
                pony.getMagicalReserves().getEnergy().add(2 + (int)(getHorizontalMotion(entity) * 5));

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
        double horMotion = getHorizontalMotion(entity);
        double motion = entity.getPos().subtract(lastPos).lengthSquared();

        boolean takeOffCondition = velocity.y > 0
                && (horMotion > 0.2 || (motion > 0.2 && velocity.y < -0.02 * getGravitySignum()));
        boolean fallingTakeOffCondition = !entity.isOnGround() && velocity.y < -1.6 * getGravitySignum();

        if (takeOffCondition || fallingTakeOffCondition) {
            entity.getAbilities().flying = true;
            isFlyingEither = true;
            isFlyingSurvival = true;

            if (!isGravityNegative()) {
                velocity.y += horMotion + 0.3;
            }
            applyThrust(velocity);

            velocity.x *= 0.2;
            velocity.z *= 0.2;
        }
    }

    private void handleWallCollission(MutableVector velocity) {
        if (wallHitCooldown > 0) {
            return;
        }

        BlockPos pos = new BlockPos(entity.getCameraPosVec(1).add(entity.getRotationVec(1).normalize().multiply(2)));

        BlockState state = entity.world.getBlockState(pos);

        if (!entity.world.isAir(pos) && Block.isFaceFullSquare(state.getCollisionShape(entity.world, pos), entity.getHorizontalFacing().getOpposite())) {
            double motion = Math.sqrt(getHorizontalMotion(entity));

            float distance = (float)(motion * 20 - 3);

            float bouncyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.PADDED, entity) * 6;

            if (distance > 0) {
                wallHitCooldown = 30;

                if (bouncyness > 0) {
                    playSound(USounds.ENTITY_PLAYER_REBOUND, 1, 1);
                    ProjectileUtil.ricochet(entity, Vec3d.of(pos), 0.4F + Math.min(2, bouncyness / 18F));
                    velocity.fromImmutable(entity.getVelocity());
                    distance /= bouncyness;
                } else {
                    playSound(distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL, 1, 1);
                }
                entity.damage(DamageSource.FLY_INTO_WALL, distance);
            }
        }

        entity.setVelocity(velocity.toImmutable());
        cancelFlight();
    }

    private void moveFlying(MutableVector velocity) {
        double motion = getHorizontalMotion(entity);

        float forward = 0.000015F * (1 + (pony.getLevel().get() / 10F)) * (float)Math.sqrt(motion);

        // vertical drop due to gravity
        forward += 0.005F;

        velocity.x += - forward * MathHelper.sin(entity.getYaw() * 0.017453292F);
        velocity.y -= (0.01F / Math.max(motion * 100, 1)) * getGravityModifier();
        velocity.z += forward * MathHelper.cos(entity.getYaw() * 0.017453292F);
    }

    private void applyThrust(MutableVector velocity) {
        if (pony.sneakingChanged() && entity.isSneaking()) {
            thrustScale = 1;
            entity.playSound(getFlightType().getWingFlapSound(), 0.5F, 1);
        } else {
            thrustScale *= 0.1889F;
        }

        float heavyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, entity) / 6F;
        float thrustStrength = 0.135F * thrustScale;
        if (heavyness > 0) {
            thrustStrength /= heavyness;
        }

        Vec3d direction = entity.getRotationVec(1).normalize().multiply(thrustStrength);

        velocity.x += direction.x;
        velocity.z += direction.z;
        velocity.y += (direction.y * 2.45 + Math.abs(direction.y) * 10) * getGravitySignum() - heavyness / 5F;

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

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        isFlyingSurvival = compound.getBoolean("isFlying");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        ticksInAir = compound.getInt("ticksInAir");

        entity.calculateDimensions();
    }
}
