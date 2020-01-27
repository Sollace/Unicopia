package com.minelittlepony.unicopia.magic.spells;

import java.util.Optional;

import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CasterUtils;
import com.minelittlepony.unicopia.magic.IAttachedEffect;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.ITossedEffect;
import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ChangelingTrapSpell extends AbstractSpell implements ITossedEffect, IAttachedEffect {

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

            BlockState state = caster.getWorld().getBlockState(origin);
            BlockState slimeState = UBlocks.slime_layer.getDefaultState();

            if (slimeState.canPlaceAt(caster.getWorld(), origin)) {
                if (caster.getWorld().isAir(origin) || (state.getBlock() != UBlocks.slime_layer && state.canReplace(new AutomaticItemPlacementContext(caster.getWorld(), origin, Direction.DOWN, new ItemStack(UBlocks.slime_layer), Direction.UP)))) {
                    caster.getWorld().setBlockState(origin, slimeState);
                }
            }
        }

        double yMotion = entity.getVelocity().y;

        if (!entity.onGround && yMotion > 0) {
            yMotion = 0;
        }
        entity.setVelocity(0, yMotion, 0);

        entity.forwardSpeed = 0;
        entity.sidewaysSpeed = 0;
        entity.limbAngle = 0;
        entity.limbDistance = 0;
        entity.lastLimbDistance = 0;

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

        if (caster.isLocal() && caster.getWorld().random.nextInt(3) == 0) {
            setDead();

            CuccoonEntity cuccoon = UEntities.CUCCOON.create(caster.getWorld());
            cuccoon.copyPositionAndRotation(caster.getEntity());

            caster.getWorld().spawnEntity(cuccoon);
        }

        setDirty(true);
    }

    protected void entrap(ICaster<?> e) {

        ChangelingTrapSpell existing = e.getEffect(ChangelingTrapSpell.class, true);

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
                .map(e -> CasterUtils.toCaster(e))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::entrap);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (previousTrappedPosition != null) {
            compound.put("previousTrappedPosition", TagHelper.serializeBlockPos(previousTrappedPosition));
        }
        compound.putInt("struggle", struggleCounter);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        previousTrappedPosition = null;
        struggleCounter = compound.getInt("struggle");

        if (compound.containsKey("previousTrappedPosition")) {
            previousTrappedPosition = TagHelper.deserializeBlockPos(compound.getCompound("previousTrappedPosition"));
        }
    }
}
