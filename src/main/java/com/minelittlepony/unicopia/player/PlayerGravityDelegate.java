package com.minelittlepony.unicopia.player;

import java.util.Random;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.unicopia.mixin.MixinEntity;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

class PlayerGravityDelegate implements IUpdatable, IGravity, InbtSerialisable, IFlyingPredicate, IPlayerHeightPredicate {

    private final IPlayer player;

    private static final float MAXIMUM_FLIGHT_EXPERIENCE = 1500;

    public int ticksNextLevel = 0;
    public float flightExperience = 0;

    public boolean isFlying = false;
    public boolean isRainbooming = false;

    private double lastTickPosX = 0;
    private double lastTickPosZ = 0;

    private float gravity = 0;

    public PlayerGravityDelegate(IPlayer player) {
        this.player = player;
    }

    @Override
    public boolean checkCanFly(IPlayer player) {
        if (player.getOwner().capabilities.isCreativeMode) {
            return true;
        }

        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IFlyingPredicate) {
                return ((IFlyingPredicate)effect).checkCanFly(player);
            }
        }

        return player.getPlayerSpecies().canFly();
    }

    protected boolean isRainboom(IPlayer player) {
        return Math.sqrt(getHorizontalMotion(player.getOwner())) > 0.4F;
    }

    @Override
    public float getTargetEyeHeight(IPlayer player) {
        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IPlayerHeightPredicate) {
                float val = ((IPlayerHeightPredicate)effect).getTargetEyeHeight(player);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (isFlying && isRainboom(player)) {
            return 0.5F;
        }

        return player.getOwner().getDefaultEyeHeight();
    }

    @Override
    public float getTargetBodyHeight(IPlayer player) {
        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IPlayerHeightPredicate) {
                float val = ((IPlayerHeightPredicate)effect).getTargetBodyHeight(player);
                if (val > 0) {
                    return val;
                }
            }
        }

        // Player height is reset at this point, so we can use it as our baseline.

        if (isFlying && isRainboom(player)) {
            return player.getOwner().height / 2;
        }

        return player.getOwner().height;
    }

    @Override
    public void setGraviationConstant(float constant) {
        gravity = constant;
    }

    @Override
    public float getGravitationConstant() {
        return gravity;
    }

    @Override
    public boolean isExperienceCritical() {
        return isRainbooming || flightExperience > MAXIMUM_FLIGHT_EXPERIENCE * 0.8;
    }

    @Override
    public void onUpdate() {

        EntityPlayer entity = player.getOwner();

        if (isExperienceCritical() && player.isRemote()) {
            Random rnd = player.getWorld().rand;

            for (int i = 0; i < 360 + getHorizontalMotion(entity); i += 10) {
                Vec3d pos = player.getOriginVector().add(
                        rnd.nextGaussian() * entity.width,
                        rnd.nextGaussian() * entity.height/2,
                        rnd.nextGaussian() * entity.width
                );
                Vec3d vel = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);

                Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, true, pos, vel);
            }
        }

        entity.capabilities.allowFlying = checkCanFly(player);

        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.isFlying |= entity.capabilities.allowFlying && isFlying && !entity.onGround && !entity.isWet();
        }

        isFlying = entity.capabilities.isFlying && !entity.capabilities.isCreativeMode;

        if (gravity != 0) {
            if (!entity.capabilities.isFlying) {
                entity.motionY += 0.08;
                entity.motionY -= gravity;
            }

            entity.onGround = !entity.world.isAirBlock(new BlockPos(entity.posX, entity.posY + entity.height + 0.5F, entity.posZ));

            if (entity.onGround) {
                entity.capabilities.isFlying = isFlying = false;
            }
        }

        float bodyHeight = getTargetBodyHeight(player);
        float eyeHeight = getTargetEyeHeight(player);

        if (gravity < 0) {
            eyeHeight = bodyHeight - eyeHeight;
        }

        if (entity.ticksExisted > 10) {
            bodyHeight = player.getInterpolator().interpolate("standingHeight", bodyHeight, 10);
            eyeHeight = player.getInterpolator().interpolate("eyeHeight", eyeHeight, 10);
        }

        MixinEntity.setSize(entity, entity.width, bodyHeight);
        entity.eyeHeight = eyeHeight;

        if (gravity < 0) {
            if (entity.isSneaking()) {
                entity.eyeHeight += 0.2F;
            }
        }

        if (!entity.capabilities.isCreativeMode && !entity.isElytraFlying()) {
            if (isFlying && !entity.isRiding()) {

                if (!isRainbooming && getHorizontalMotion(entity) > 0.2 && flightExperience < MAXIMUM_FLIGHT_EXPERIENCE) {
                    flightExperience++;
                }

                entity.fallDistance = 0;

                if (player.getPlayerSpecies() != Race.CHANGELING && entity.world.rand.nextInt(100) == 0) {
                    float exhaustion = (0.3F * ticksNextLevel) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    exhaustion *= (1 - flightExperience / MAXIMUM_FLIGHT_EXPERIENCE);

                    entity.addExhaustion(exhaustion);
                }

                if (ticksNextLevel++ >= MAXIMUM_FLIGHT_EXPERIENCE) {
                    ticksNextLevel = 0;

                    entity.addExperience(1);
                    addFlightExperience(entity, 1);
                    entity.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, 1, 1);
                }

                moveFlying(entity);

                if (isExperienceCritical()) {

                    if (player.getEnergy() <= 0.25F) {
                        player.addEnergy(2);
                    }

                    if (isRainbooming || (entity.isSneaking() && isRainboom(player))) {
                        float forward = 0.5F * flightExperience / MAXIMUM_FLIGHT_EXPERIENCE;

                        entity.motionX += - forward * MathHelper.sin(entity.rotationYaw * 0.017453292F);
                        entity.motionZ += forward * MathHelper.cos(entity.rotationYaw * 0.017453292F);
                        entity.motionY += 0.3;

                        if (!isRainbooming || entity.world.rand.nextInt(5) == 0) {
                            entity.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 1, 1);
                        }

                        if (flightExperience > 0) {
                            flightExperience -= 13;
                            isRainbooming = true;
                        } else {
                            isRainbooming = false;
                        }
                    }
                }

                if (ticksNextLevel > 0 && ticksNextLevel % 30 == 0) {
                    entity.playSound(getWingSound(), 0.5F, 1);
                }
            } else {
                if (ticksNextLevel != 0) {
                    entity.playSound(getWingSound(), 0.4F, 1);
                }

                ticksNextLevel = 0;

                if (isExperienceCritical()) {
                    addFlightExperience(entity, -0.39991342F);
                } else {
                    addFlightExperience(entity, -0.019991342F);
                }

                if (flightExperience < 0.02) {
                    isRainbooming = false;
                }
            }
        }

        lastTickPosX = entity.posX;
        lastTickPosZ = entity.posZ;
    }

    public SoundEvent getWingSound() {
        return player.getPlayerSpecies() == Race.CHANGELING ? USounds.INSECT : USounds.WING_FLAP;
    }

    protected void moveFlying(EntityPlayer player) {

        float forward = 0.000015F * flightExperience * (float)Math.sqrt(getHorizontalMotion(player));
        int factor = gravity < 0 ? -1 : 1;
        boolean sneak = !player.isSneaking();

        // vertical drop due to gravity
        if (sneak) {
            player.motionY -= (0.005F - getHorizontalMotion(player) / 100) * factor;
        } else {
            forward += 0.005F;
            player.motionY -= 0.0001F * factor;
        }

        player.motionX += - forward * MathHelper.sin(player.rotationYaw * 0.017453292F);
        player.motionZ += forward * MathHelper.cos(player.rotationYaw * 0.017453292F);

        if (player.world.isRainingAt(player.getPosition())) {
            float glance = 360 * player.world.rand.nextFloat();


            forward = 0.015F * player.world.rand.nextFloat() *  player.world.getRainStrength(1);

            if (player.world.rand.nextInt(30) == 0) {
                forward *= 10;
            }
            if (player.world.rand.nextInt(30) == 0) {
                forward *= 10;
            }
            if (player.world.rand.nextInt(40) == 0) {
                forward *= 100;
            }

            if (player.world.isThundering() && player.world.rand.nextInt(60) == 0) {
                player.motionY += forward * 3;
            }

            if (forward >= 1) {
                player.world.playSound(null, player.getPosition(), USounds.WIND_RUSH, SoundCategory.AMBIENT, 3, 1);
            }

            if (forward > 4) {
                forward = 4;
            }

            //player.knockBack(player, forward, 1, 1);
            player.motionX += - forward * MathHelper.sin((player.rotationYaw + glance) * 0.017453292F);
            player.motionZ += forward * MathHelper.cos((player.rotationYaw + glance) * 0.017453292F);
        }
    }

    public void landHard(EntityPlayer player, float distance, float damageMultiplier) {
        if (distance <= 0) {
            return;
        }

        PotionEffect potioneffect = player.getActivePotionEffect(MobEffects.JUMP_BOOST);
        float potion = potioneffect != null ? potioneffect.getAmplifier() + 1 : 0;
        int i = MathHelper.ceil((distance - 8.0F - potion) * damageMultiplier);

        if (i > 0) {
            int j = MathHelper.floor(player.posX);
            int k = MathHelper.floor(player.posY - 0.20000000298023224D);
            int l = MathHelper.floor(player.posZ);

            BlockPos pos = new BlockPos(j, k, l);

            IBlockState state = player.world.getBlockState(pos);
            Block block = state.getBlock();

            if (state.getMaterial() != Material.AIR) {

                player.playSound(getFallSound(i), 1, 1);
                player.attackEntityFrom(DamageSource.FALL, i);

                SoundType soundtype = block.getSoundType(state, player.getEntityWorld(), pos, player);

                player.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.5f, soundtype.getPitch() * 0.75f);
            }
        }
    }

    protected double getHorizontalMotion(Entity e) {
        double motionX = e.posX - lastTickPosX;
        double motionZ = e.posZ - lastTickPosZ;

        return (motionX * motionX)
               + (motionZ * motionZ);
    }

    protected SoundEvent getFallSound(int distance) {
        return distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }

    private void addFlightExperience(EntityPlayer entity, float factor) {
        float maximumGain = MAXIMUM_FLIGHT_EXPERIENCE - flightExperience;
        float gainSteps = 20;

        flightExperience = Math.max(0, flightExperience + factor * maximumGain / gainSteps);
    }

    public void updateFlightStat(EntityPlayer entity, boolean flying) {
        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.allowFlying = player.getPlayerSpecies().canFly();

            if (entity.capabilities.allowFlying) {
                entity.capabilities.isFlying |= flying;

                isFlying = entity.capabilities.isFlying;

                if (isFlying) {
                    ticksNextLevel = 0;
                }

            } else {
                entity.capabilities.isFlying = false;
                isFlying = false;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("flightDuration", ticksNextLevel);
        compound.setFloat("flightExperience", flightExperience);
        compound.setBoolean("isFlying", isFlying);
        compound.setBoolean("isRainbooming", isRainbooming);

        if (gravity != 0) {
            compound.setFloat("gravity", gravity);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ticksNextLevel = compound.getInteger("flightDuration");
        flightExperience = compound.getFloat("flightExperience");
        isFlying = compound.getBoolean("isFlying");
        isRainbooming = compound.getBoolean("isRainbooming");

        if (compound.hasKey("gravity")) {
            gravity = compound.getFloat("gravity");
        } else {
            gravity = 0;
        }
    }

    @Override
    public boolean isFlying() {
        return isFlying;
    }

    @Override
    public float getFlightExperience() {
        return flightExperience / MAXIMUM_FLIGHT_EXPERIENCE;
    }

    @Override
    public float getFlightDuration() {
        return ticksNextLevel / MAXIMUM_FLIGHT_EXPERIENCE;
    }
}
