package com.minelittlepony.unicopia.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class CuccoonEntity extends LivingEntity implements IMagicals, InAnimate {

    private static final TrackedData<Integer> STRUGGLE_COUNT = DataTracker.registerData(CuccoonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final List<ItemStack> armour = Lists.newArrayList();

    private boolean captiveLastSneakState;

    public CuccoonEntity(EntityType<CuccoonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(STRUGGLE_COUNT, 0);
    }

    public int getStruggleCount() {
        return getDataTracker().get(STRUGGLE_COUNT) % 6;
    }

    public void setStruggleCount(int count) {
        getDataTracker().set(STRUGGLE_COUNT, count % 6);
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
    public boolean damage(DamageSource source, float amount) {

        if (EquinePredicates.BUGGY.test(source.getSource())) {
            amount = 0;
        }

        return super.damage(source, amount);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return super.canAddPassenger(entity)
                && !entity.isSneaking()
                && !hasPassengers()
                && entity instanceof LivingEntity
                && !EquinePredicates.BUGGY.test(entity);
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public double getMountedHeightOffset() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (hasPassengers()) {
            Entity passenger = getPrimaryPassenger();

            boolean sneaking = passenger.isSneaking();

            if (sneaking && !attemptDismount(passenger)) {
                passenger.setSneaking(false);
            }

            captiveLastSneakState = sneaking;

            if (passenger instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)passenger;

                if (!living.hasStatusEffect(StatusEffects.REGENERATION) && living.getHealth() < living.getHealthMaximum()) {
                    living.addPotionEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 2));
                }

                if (!living.hasStatusEffect(StatusEffects.SLOWNESS) && living.getHealth() < living.getHealthMaximum()) {
                    living.addPotionEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000, 4));
                }
            }
        }

        if (world.isClient) {
            EntityDimensions dims = getDimensions(getPose());

            double x = this.x + dims.width * random.nextFloat() - dims.width/2;
            double y = this.y + dims.height * random.nextFloat();
            double z = this.z + dims.width * random.nextFloat() - dims.width/2;

            world.addParticle(ParticleTypes.DRIPPING_LAVA, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {

        if (hand == Hand.MAIN_HAND && EquinePredicates.BUGGY.test(player)) {

            if (hasPassengers()) {
                Entity passenger = getPrimaryPassenger();

                if (player.canConsume(false) || player.getHealth() < player.getHealthMaximum()) {
                    DamageSource d = MagicalDamageSource.causePlayerDamage("feed", player);

                    Pony.of(player).spawnParticles(UParticles.CHANGELING_MAGIC, 7);

                    if (passenger instanceof LivingEntity) {
                        if (player.hasStatusEffect(StatusEffects.NAUSEA)) {
                            ((LivingEntity)passenger).addPotionEffect(player.removePotionEffect(StatusEffects.NAUSEA));
                        } else if (random.nextInt(2300) == 0) {
                            ((LivingEntity)passenger).addPotionEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 1));
                        }
                    }

                    if (passenger instanceof PlayerEntity) {
                        if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                            player.addPotionEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 13000, 1));
                        }
                    }

                    passenger.damage(d, 5);

                    if (player.canConsume(false)) {
                        player.getHungerManager().add(5, 0);
                    } else {
                        player.heal(5);
                    }

                    return ActionResult.SUCCESS;
                }
            }
        }

        return super.interactAt(player, vec, hand);
    }

    public float getBreatheAmount(float stutter) {
        return MathHelper.sin((age + stutter) / 40) / 2
                + hurtTime / 10F;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public boolean attemptDismount(Entity captive) {
        if (captive.isSneaking() != captiveLastSneakState) {
            setStruggleCount(getStruggleCount() + 1);

            spawnSlimeParticles();

            captive.playSound(USounds.SLIME_RETRACT, 1, 1);
            this.hurtTime += 15;

            if (getStruggleCount() == 0) {
                remove();

                return true;
            }
        }

        return false;
    }

    private void spawnSlimeParticles() {
        EntityDimensions dims = getDimensions(getPose());

        for (int k = 0; k < 20; k++) {
            double d2 = random.nextGaussian() * 0.02;
            double d0 = random.nextGaussian() * 0.02;
            double d1 = random.nextGaussian() * 0.02;

            world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.getDefaultState()),
                    x + random.nextFloat() * dims.width * 2 - dims.width,
                    y + random.nextFloat() * dims.height,
                    z + random.nextFloat() * dims.width * 2 - dims.width,
                    d2, d0, d1);
        }
    }

    @Override
    protected void updatePostDeath() {
        if (++deathTime == 20) {
            if (!world.isClient && lastAttackedTicks > 0 && canDropLootAndXp() && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                int i = getCurrentExperience(attackingPlayer);

                while (i > 0) {
                    int j = ExperienceOrbEntity.roundToOrbSize(i);

                    i -= j;

                    world.spawnEntity(new ExperienceOrbEntity(world, x, y, z, j));
                }

                removeAllPassengers();
            }

            remove();
            spawnSlimeParticles();
        }
    }

    @Override
    @Nullable
    public Box getHardCollisionBox(Entity entity) {
        return entity.collides() ? entity.getBoundingBox() : null;
    }

    @Nullable
    public Box getCollisionBoundingBox() {
        return getBoundingBox().contract(0.2);
    }

    @Override
    protected void pushAway(Entity entity) {
        if (canAddPassenger(entity)) {
            entity.playSound(USounds.SLIME_ADVANCE, 1, 1);
            entity.startRiding(this, true);
        } else {
            super.pushAway(entity);
        }
    }

    @Override
    public boolean canInteract(Race race) {
        return race == Race.CHANGELING;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return armour;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setEquippedStack(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return Arm.LEFT;
    }
}
