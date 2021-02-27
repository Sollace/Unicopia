package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.JoustingSpell;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerPhysics extends EntityPhysics<Pony> implements Tickable, Motion, NbtSerialisable {

    private int ticksInAir;

    private float thrustScale = 0;

    private FlightType lastFlightType = FlightType.NONE;

    public boolean isFlyingEither = false;
    public boolean isFlyingSurvival = false;

    private int wallHitCooldown;

    private Vec3d lastPos = Vec3d.ZERO;

    private final PlayerDimensions dimensions;

    public PlayerPhysics(Pony pony) {
        super(pony, Creature.GRAVITY);
        dimensions = new PlayerDimensions(pony, this);
    }

    @Override
    public PlayerDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public boolean isFlying() {
        return isFlyingSurvival && !pony.getMaster().isFallFlying() && !pony.getMaster().hasVehicle();
    }

    @Override
    public boolean isGliding() {
        return isFlying() && (pony.getMaster().isSneaking() || ((Jumper)pony.getMaster()).isJumping()) && !pony.sneakingChanged();
    }

    @Override
    public boolean isRainbooming() {
        return pony.getSpellOrEmpty(JoustingSpell.class).isPresent();
    }

    @Override
    public float getWingAngle() {
        float spreadAmount = -0.5F;

        PlayerEntity entity = pony.getMaster();

        if (isFlying()) {
            //spreadAmount += Math.sin(pony.getEntity().age / 4F) * 8;
            spreadAmount += isGliding() ? 3 : thrustScale * 60;
        }

        if (entity.isSneaking()) {
            spreadAmount += 2;
        }

        spreadAmount += MathHelper.clamp(-entity.getVelocity().y, 0, 2);
        spreadAmount += Math.sin(entity.age / 9F) / 9F;
        spreadAmount = MathHelper.clamp(spreadAmount, -2, 5);

        return pony.getInterpolator().interpolate("wingSpreadAmount", spreadAmount, 10);
    }

    @Override
    public void tick() {
        super.tick();

        if (wallHitCooldown > 0) {
            wallHitCooldown--;
        }
        PlayerEntity entity = pony.getMaster();

        final MutableVector velocity = new MutableVector(entity.getVelocity());

        if (isGravityNegative() && !entity.isSneaking() && entity.isInSneakingPose()) {
            float currentHeight = entity.getDimensions(entity.getPose()).height;
            float sneakingHeight = entity.getDimensions(EntityPose.STANDING).height;

            entity.setPos(entity.getX(), entity.getY() + currentHeight - sneakingHeight, entity.getZ());
            entity.setPose(EntityPose.STANDING);
        }

        boolean creative = entity.abilities.creativeMode || pony.getMaster().isSpectator();

        FlightType type = getFlightType();

        if (type != lastFlightType && (lastFlightType.isArtifical() || type.isArtifical())) {
            ParticleUtils.spawnParticles(ParticleTypes.CLOUD, entity, 10);

            entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), entity.world.getDimension().isUltrawarm() ? SoundEvents.BLOCK_BELL_USE : SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 0.1125F, 1.5F, true);
        }

        entity.abilities.allowFlying = type.canFlyCreative(entity);

        if (!creative) {
            entity.abilities.flying |= (type.canFly() || entity.abilities.allowFlying) && isFlyingEither;
            if (!type.canFly() && (type != lastFlightType)) {
                entity.abilities.flying = false;
            }

            if ((entity.isOnGround() && entity.isSneaking())
                    || entity.isTouchingWater()
                    || entity.horizontalCollision
                    || (entity.verticalCollision && (pony.getSpecies() != Race.BAT || velocity.y < 0))) {

                if (entity.abilities.flying && entity.horizontalCollision) {
                    handleWallCollission(entity, velocity);

                    entity.setVelocity(velocity.toImmutable());
                    entity.abilities.flying = false;
                    return;
                }

                entity.abilities.flying = false;
                isFlyingSurvival = entity.abilities.flying && !creative;
                isFlyingEither = isFlyingSurvival || (creative && entity.abilities.flying);
            }
        }

        lastFlightType = type;

        isFlyingSurvival = entity.abilities.flying && !creative;
        isFlyingEither = isFlyingSurvival || (creative && entity.abilities.flying);

        if (isGravityNegative()) {
            if (entity.isOnGround() || (!creative && entity.horizontalCollision)) {
                entity.abilities.flying = false;
                isFlyingEither = false;
                isFlyingSurvival = false;
            }

            if (entity.isClimbing() && (entity.horizontalCollision || ((Jumper)entity).isJumping())) {
                velocity.y = -0.2F;
            }
        }

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

                if (type.isArtifical()) {
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
                            entity.world.playSoundFromEntity(null, entity, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 0.13F, 0.5F);
                        }

                        if (entity.world.random.nextInt(damageInterval) == 0) {
                            stack.damage(minDamage + entity.world.random.nextInt(50), entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
                        }

                        if (!getFlightType().canFly()) {
                            entity.world.playSoundFromEntity(null, entity, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 2);
                            entity.abilities.flying = false;
                            isFlyingEither = false;
                            isFlyingSurvival = false;
                        }
                    }
                } else {
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

                entity.fallDistance = 0;
                if (type.isAvian()) {
                    applyThrust(entity, velocity);
                }
                moveFlying(entity, velocity);
                if (entity.world.hasRain(entity.getBlockPos())) {
                    applyTurbulance(entity, velocity);
                }

                if (type.isAvian()) {
                    if (entity.world.isClient && ticksInAir % 20 == 0 && entity.getVelocity().length() < 0.29) {
                        entity.playSound(getFlightType().getWingFlapSound(), 0.5F, 1);
                        thrustScale = 1;
                    }
                    velocity.y -= 0.02 * getGravitySignum();
                    velocity.x *= 0.9896;
                    velocity.z *= 0.9896;
                }
            } else {
                ticksInAir = 0;

                if (!creative && type.isAvian()) {

                    double horMotion = getHorizontalMotion(entity);
                    double motion = entity.getPos().subtract(lastPos).lengthSquared();

                    boolean takeOffCondition = velocity.y > 0
                            && (horMotion > 0.2 || (motion > 0.2 && velocity.y < -0.02 * getGravitySignum()));
                    boolean fallingTakeOffCondition = !entity.isOnGround() && velocity.y < -1.6 * getGravitySignum();

                    if (takeOffCondition || fallingTakeOffCondition) {
                        entity.abilities.flying = true;
                        isFlyingEither = true;
                        isFlyingSurvival = true;

                        if (!isGravityNegative()) {
                            velocity.y += horMotion + 0.3;
                        }
                        applyThrust(entity, velocity);

                        velocity.x *= 0.2;
                        velocity.z *= 0.2;
                    }
                }
            }
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

    protected void handleWallCollission(PlayerEntity player, MutableVector velocity) {

        if (wallHitCooldown > 0) {
            return;
        }

        BlockPos pos = new BlockPos(player.getCameraPosVec(1).add(player.getRotationVec(1).normalize().multiply(2)));

        BlockState state = player.world.getBlockState(pos);

        if (!player.world.isAir(pos) && Block.isFaceFullSquare(state.getCollisionShape(player.world, pos), player.getHorizontalFacing().getOpposite())) {
            double motion = Math.sqrt(getHorizontalMotion(player));

            float distance = (float)(motion * 20 - 3);

            float bouncyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.PADDED, player) * 6;

            if (distance > 0) {
                wallHitCooldown = 30;

                if (bouncyness > 0) {
                    player.world.playSoundFromEntity(null, player, USounds.ENTITY_PLAYER_REBOUND, SoundCategory.PLAYERS, 1, 1);
                    ProjectileUtil.ricochet(player, Vec3d.of(pos), 0.4F + Math.min(2, bouncyness / 18F));
                    velocity.fromImmutable(player.getVelocity());
                    distance /= bouncyness;
                } else {
                    player.world.playSoundFromEntity(null, player, distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.PLAYERS, 1, 1);
                    //player.playSound(distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL, 1, 1);
                }
                player.damage(DamageSource.FLY_INTO_WALL, distance);
            }
        }
    }

    protected void moveFlying(PlayerEntity player, MutableVector velocity) {
        double motion = getHorizontalMotion(player);

        float forward = 0.000015F * (1 + (pony.getLevel().get() / 10F)) * (float)Math.sqrt(motion);

        // vertical drop due to gravity
        forward += 0.005F;

        velocity.x += - forward * MathHelper.sin(player.yaw * 0.017453292F);
        velocity.y -= (0.01F / Math.max(motion * 100, 1)) * getGravityModifier();
        velocity.z += forward * MathHelper.cos(player.yaw * 0.017453292F);
    }

    protected void applyThrust(PlayerEntity player, MutableVector velocity) {
        if (pony.sneakingChanged() && player.isSneaking()) {
            thrustScale = 1;
            player.playSound(getFlightType().getWingFlapSound(), 0.5F, 1);
        } else {
            thrustScale *= 0.1889F;
        }

        float heavyness = EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, player) / 6F;
        float thrustStrength = 0.135F * thrustScale;
        if (heavyness > 0) {
            thrustStrength /= heavyness;
        }

        Vec3d direction = player.getRotationVec(1).normalize().multiply(thrustStrength);

        velocity.x += direction.x;
        velocity.z += direction.z;
        velocity.y += (direction.y * 2.45 + Math.abs(direction.y) * 10) * getGravitySignum() - heavyness / 5F;

        if (player.isSneaking()) {
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

    protected void applyTurbulance(PlayerEntity player, MutableVector velocity) {
        float glance = 360 * player.world.random.nextFloat();
        float forward = 0.015F * player.world.random.nextFloat() *  player.world.getRainGradient(1);

        if (player.world.random.nextInt(30) == 0) {
            forward *= 10;
        }
        if (player.world.random.nextInt(30) == 0) {
            forward *= 10;
        }
        if (player.world.random.nextInt(40) == 0) {
            forward *= 100;
        }

        if (player.world.isThundering() && player.world.random.nextInt(60) == 0) {
            velocity.y += forward * 3 * getGravitySignum();
        }

        if (forward >= 1) {
            player.world.playSound(null, player.getBlockPos(), USounds.AMBIENT_WIND_GUST, SoundCategory.AMBIENT, 3, 1);
        }

        forward = Math.min(forward, 7);
        forward /= 1 + (EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, player) * 0.8F);

        velocity.x += - forward * MathHelper.sin((player.yaw + glance) * 0.017453292F);
        velocity.z += forward * MathHelper.cos((player.yaw + glance) * 0.017453292F);

        if (!player.world.isClient && player.world.isThundering() && player.world.random.nextInt(9000) == 0) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(player.world);
            lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());

            player.world.spawnEntity(lightning);
        }
    }

    protected double getHorizontalMotion(Entity e) {
        return Entity.squaredHorizontalLength(e.getPos().subtract(lastPos));
    }

    private FlightType getFlightType() {

        if (UItems.PEGASUS_AMULET.isApplicable(pony.getMaster())) {
            return FlightType.ARTIFICIAL;
        }

        if (pony.hasSpell()) {
            Spell effect = pony.getSpell(true);
            if (!effect.isDead() && effect instanceof FlightType.Provider) {
                return ((FlightType.Provider)effect).getFlightType(pony);
            }
        }

        return pony.getSpecies().getFlightType();
    }

    public void updateFlightStat(boolean flying) {
        PlayerEntity entity = pony.getMaster();

        FlightType type = getFlightType();

        entity.abilities.allowFlying = type.canFlyCreative(entity);

        if (type.canFly() || entity.abilities.allowFlying) {
            entity.abilities.flying |= flying;
            isFlyingSurvival = entity.abilities.flying;
        } else {
            entity.abilities.flying = false;
            isFlyingSurvival = false;
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putBoolean("isFlying", isFlyingSurvival);
        compound.putBoolean("isFlyingEither", isFlyingEither);
        compound.putInt("ticksInAir", ticksInAir);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        isFlyingSurvival = compound.getBoolean("isFlying");
        isFlyingEither = compound.getBoolean("isFlyingEither");
        ticksInAir = compound.getInt("ticksInAir");

        pony.getMaster().calculateDimensions();
    }
}
