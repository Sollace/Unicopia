package com.minelittlepony.unicopia.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityCuccoon extends EntityLivingBase implements IMagicals, IInAnimate {

    private static final DataParameter<Integer> STRUGGLE_COUNT = EntityDataManager.createKey(EntityCuccoon.class, DataSerializers.VARINT);

    private final List<ItemStack> armour = Lists.newArrayList();

    private boolean captiveLastSneakState;

    public EntityCuccoon(World world) {
        super(world);
        setSize(0.6f, 0.6f);

        width = 1.5F;
        height = 1.6F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(STRUGGLE_COUNT, 0);
    }

    public int getStruggleCount() {
        return getDataManager().get(STRUGGLE_COUNT) % 6;
    }

    public void setStruggleCount(int count) {
        getDataManager().set(STRUGGLE_COUNT, count % 6);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getFallSound(int heightIn) {
        return SoundEvents.ENTITY_SLIME_SQUISH;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

        if (Predicates.BUGGY.test(source.getTrueSource())) {
            amount = 0;
        }

        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean canBeRidden(Entity entity) {
        return super.canBeRidden(entity)
                && !entity.isSneaking()
                && !isBeingRidden()
                && entity instanceof EntityLivingBase
                && !Predicates.BUGGY.test(entity);
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public double getMountedYOffset() {
        return 0;
    }

    @Override
    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (isBeingRidden()) {
            Entity passenger = getPassengers().get(0);

            boolean sneaking = passenger.isSneaking();

            if (sneaking && !attemptDismount(passenger)) {
                passenger.setSneaking(false);
            }

            captiveLastSneakState = sneaking;

            if (passenger instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase)passenger;

                if (!living.isPotionActive(MobEffects.REGENERATION) && living.getHealth() < living.getMaxHealth()) {
                    living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, 2));
                }

                if (!living.isPotionActive(MobEffects.SLOWNESS) && living.getHealth() < living.getMaxHealth()) {
                    living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2000, 4));
                }
            }
        }

        if (world.isRemote) {
            double x = posX + width * world.rand.nextFloat() - width/2;
            double y = posY + height * world.rand.nextFloat();
            double z = posZ + width * world.rand.nextFloat() - width/2;

            world.spawnParticle(EnumParticleTypes.DRIP_LAVA, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {

        if (hand == EnumHand.MAIN_HAND && Predicates.BUGGY.test(player)) {

            if (isBeingRidden()) {
                Entity passenger = getPassengers().get(0);

                if (player.canEat(false) || player.getHealth() < player.getMaxHealth()) {
                    DamageSource d = MagicalDamageSource.causePlayerDamage("feed", player);


                    IPower.spawnParticles(UParticles.CHANGELING_MAGIC, this, 7);

                    if (passenger instanceof EntityLivingBase) {
                        if (player.isPotionActive(MobEffects.NAUSEA)) {
                            ((EntityLivingBase)passenger).addPotionEffect(player.removeActivePotionEffect(MobEffects.NAUSEA));
                        } else if (world.rand.nextInt(2300) == 0) {
                            ((EntityLivingBase)passenger).addPotionEffect(new PotionEffect(MobEffects.WITHER, 20, 1));
                        }
                    }

                    if (passenger instanceof EntityPlayer) {
                        if (!player.isPotionActive(MobEffects.HEALTH_BOOST)) {
                            player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 13000, 1));
                        }
                    }

                    passenger.attackEntityFrom(d, 5);

                    if (player.canEat(false)) {
                        player.getFoodStats().addStats(5, 0);
                    } else {
                        player.heal(5);
                    }

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return super.applyPlayerInteraction(player, vec, hand);
    }

    public float getBreatheAmount(float stutter) {
        return MathHelper.sin((ticksExisted + stutter) / 40) / 2
                + hurtTime / 10F;
    }

    @Override
    public boolean attackable() {
        return false;
    }

    public boolean attemptDismount(Entity captive) {
        if (captive.isSneaking() != captiveLastSneakState) {
            setStruggleCount(getStruggleCount() + 1);

            for (int k = 0; k < 20; k++) {
                double d2 = rand.nextGaussian() * 0.02;
                double d0 = rand.nextGaussian() * 0.02;
                double d1 = rand.nextGaussian() * 0.02;

                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        posX + rand.nextFloat() * width * 2 - width,
                        posY + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2 - width,
                        d2, d0, d1, Block.getStateId(Blocks.SLIME_BLOCK.getDefaultState()));
            }

            captive.playSound(USounds.SLIME_RETRACT, 1, 1);
            this.hurtTime += 15;

            if (getStruggleCount() == 0) {
                setDead();

                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDeathUpdate() {
        if (++deathTime == 20) {
            if (!world.isRemote && (isPlayer() || recentlyHit > 0 && canDropLoot() && world.getGameRules().getBoolean("doMobLoot"))) {
                int i = ForgeEventFactory.getExperienceDrop(this, attackingPlayer, getExperiencePoints(attackingPlayer));

                while (i > 0) {
                    int j = EntityXPOrb.getXPSplit(i);

                    i -= j;

                    world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, j));
                }

                this.dismountRidingEntity();
            }

            setDead();

            for (int k = 0; k < 20; k++) {
                double d2 = rand.nextGaussian() * 0.02;
                double d0 = rand.nextGaussian() * 0.02;
                double d1 = rand.nextGaussian() * 0.02;

                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        posX + rand.nextFloat() * width * 2 - width,
                        posY + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2 - width,
                        d2, d0, d1, Block.getStateId(Blocks.SLIME_BLOCK.getDefaultState()));
            }
        }
    }

    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity.canBeCollidedWith() ? entity.getEntityBoundingBox() : null;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox().shrink(0.2);
    }

    @Override
    protected void collideWithEntity(Entity entity) {
        if (canBeRidden(entity)) {
            entity.playSound(USounds.SLIME_ADVANCE, 1, 1);
            entity.startRiding(this, true);
        } else {
            super.collideWithEntity(entity);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
    }

    @Override
    public boolean canInteract(Race race) {
        return race == Race.CHANGELING;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return armour;
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.LEFT;
    }
}
