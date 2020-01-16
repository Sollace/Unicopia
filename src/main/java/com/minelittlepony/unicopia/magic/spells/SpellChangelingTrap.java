package com.minelittlepony.unicopia.magic.spells;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.EntityCuccoon;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.IAttachedEffect;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.ITossedEffect;
import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class SpellChangelingTrap extends AbstractSpell implements ITossedEffect, IAttachedEffect {

    private BlockPos previousTrappedPosition;

    private int struggleCounter = 10;

    @Override
    public String getName() {
        return "changeling_trap";
    }

    @Override
    public int getTint() {
        return 0x88FF88;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public SoundEvent getThrowSound(ICaster<?> caster) {
        return USounds.SLIME_RETRACT;
    }

    @Override
    public ItemStack getCastAppearance(ICaster<?> caster) {
        return new ItemStack(Items.SLIME_BALL);
    }

    private boolean checkStruggleCondition(ICaster<?> caster) {
        return !caster.getOrigin().equals(previousTrappedPosition)
            || (!(caster.getOwner() instanceof PlayerEntity) && caster.getWorld().random.nextInt(20) == 0);
    }

    @Override
    public boolean updateOnPerson(ICaster<?> caster) {
        LivingEntity entity = caster.getOwner();

        if (entity.getVelocity().y > 0) {
            entity.playSound(SoundEvents.BLOCK_SLIME_BLOCK_HIT, 1, 1);
        }

        BlockPos origin = caster.getOrigin();

        if (previousTrappedPosition == null) {
            previousTrappedPosition = origin;

            setDirty(true);
        }

        if (caster.isLocal()) {
            if (checkStruggleCondition(caster)) {
                previousTrappedPosition = origin;
                struggleCounter--;
                WorldEvent.DESTROY_BLOCK.play(caster.getWorld(), origin, Blocks.SLIME_BLOCK.getDefaultState());

                setDirty(true);
            }

            Block block = caster.getWorld().getBlockState(origin).getBlock();

            if (UBlocks.slime_layer.canPlaceBlockAt(caster.getWorld(), origin)) {
                if (caster.getWorld().isAir(origin) || (block != UBlocks.slime_layer && block.isReplaceable(caster.getWorld(), origin))) {
                    caster.getWorld().setBlockState(origin, UBlocks.slime_layer.getDefaultState());
                }
            }
        }

        entity.motionX = 0;

        if (!entity.onGround && entity.motionY > 0) {
            entity.motionY = 0;
        }
        entity.motionZ = 0;

        entity.moveForward = 0;
        entity.moveStrafing = 0;
        entity.limbSwingAmount = 0;
        entity.limbSwing = 0;
        entity.prevLimbSwingAmount = 0;

        entity.x = Math.floor(entity.x) + 0.4;
        entity.z = Math.floor(entity.z) + 0.4;

        entity.hurtTime = 2;
        entity.horizontalCollision = true;
        entity.collided = true;

        if (entity instanceof PlayerEntity) {
            ((PlayerEntity)entity).abilities.flying = false;
        }

        StatusEffectInstance SLIME_REGEN = new StatusEffectInstance(StatusEffects.REGENERATION, 0);

        entity.addPotionEffect(SLIME_REGEN);

        if (caster.isLocal()) {
            if (struggleCounter <= 0) {
                setDead();
                setDirty(true);

                WorldEvent.DESTROY_BLOCK.play(caster.getWorld(), origin, Blocks.SLIME_BLOCK.getDefaultState());
            }
        }

        return !entity.hasVehicle() && struggleCounter > 0;
    }

    @Override
    public boolean update(ICaster<?> source) {
        return !source.getEntity().hasVehicle();
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(ParticleTypes.DRIPPING_LAVA, 1);
    }

    @Override
    public void renderOnPerson(ICaster<?> source) {
        render(source);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    public void enforce(ICaster<?> caster) {
        struggleCounter = 10;

        if (caster.isLocal() && caster.getWorld().rand.nextInt(3) == 0) {
            setDead();

            EntityCuccoon cuccoon = new EntityCuccoon(caster.getWorld());
            cuccoon.copyLocationAndAnglesFrom(caster.getEntity());

            caster.getWorld().spawnEntity(cuccoon);
        }

        setDirty(true);
    }

    protected void entrap(ICaster<?> e) {

        SpellChangelingTrap existing = e.getEffect(SpellChangelingTrap.class, true);

        if (existing == null) {
            e.setEffect(copy());
        } else {
            existing.enforce(e);
        }
    }

    protected boolean canAffect(Entity e) {
        return !(e instanceof IMagicals)
            && e instanceof LivingEntity
            && !e.hasVehicle();
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, BlockState state) {
        if (caster.isLocal()) {
            caster.findAllEntitiesInRange(5)
                .filter(this::canAffect)
                .map(e -> SpeciesList.instance().getCaster((LivingEntity)e))
                .forEach(this::entrap);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (previousTrappedPosition != null) {
            compound.put("previousTrappedPosition", NBTUtils.createPosTag(previousTrappedPosition));
        }
        compound.putInt("struggle", struggleCounter);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        previousTrappedPosition = null;
        struggleCounter = compound.getInt("struggle");

        if (compound.containsKey("previousTrappedPosition")) {
            previousTrappedPosition = NBTUtil.getPosFromTag(compound.getCompound("previousTrappedPosition"));
        }
    }
}
