package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.init.UBlocks;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
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
            || (!(caster.getOwner() instanceof EntityPlayer) && caster.getWorld().rand.nextInt(20) == 0);
    }

    @Override
    public boolean updateOnPerson(ICaster<?> caster) {
        EntityLivingBase entity = caster.getOwner();

        if (entity.motionY > 0) {
            entity.playSound(SoundEvents.BLOCK_SLIME_HIT, 1, 1);
        }

        BlockPos origin = caster.getOrigin();

        if (previousTrappedPosition == null) {
            previousTrappedPosition = origin;

            setDirty(true);
        }

        if (!caster.getWorld().isRemote && checkStruggleCondition(caster)) {
            previousTrappedPosition = origin;
            struggleCounter--;
            WorldEvent.DESTROY_BLOCK.play(caster.getWorld(), origin, Blocks.SLIME_BLOCK.getDefaultState());

            setDirty(true);
        }

        if (caster.getWorld().isAirBlock(origin) || caster.getWorld().getBlockState(origin).getBlock().isReplaceable(caster.getWorld(), origin)) {
            caster.getWorld().setBlockState(origin, UBlocks.slime_layer.getDefaultState());
        }

        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;

        entity.moveForward = 0;
        entity.moveStrafing = 0;
        entity.limbSwingAmount = 0;
        entity.limbSwing = 0;
        entity.prevLimbSwingAmount = 0;

        entity.posX = Math.floor(entity.posX) + 0.4;
        entity.posZ = Math.floor(entity.posZ) + 0.4;

        entity.hurtTime = 2;
        entity.collidedHorizontally = true;
        entity.collided = true;

        if (entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).capabilities.isFlying = false;
        }

        PotionEffect SLIME_REGEN = new PotionEffect(MobEffects.REGENERATION, 0);

        entity.addPotionEffect(SLIME_REGEN);

        if (caster.getWorld().isRemote) {
            if (struggleCounter <= 0) {
                setDead();
                setDirty(true);

                WorldEvent.DESTROY_BLOCK.play(caster.getWorld(), origin, Blocks.SLIME_BLOCK.getDefaultState());
            }
        }

        return struggleCounter > 0;
    }

    @Override
    public boolean update(ICaster<?> source) {
        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(EnumParticleTypes.DRIP_LAVA.getParticleID(), 1);
    }

    @Override
    public void renderOnPerson(ICaster<?> source) {
        render(source);
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    public void enforce() {
        struggleCounter = 10;
        setDirty(true);
    }

    protected void entrap(ICaster<?> e) {

        SpellChangelingTrap existing = e.getEffect(SpellChangelingTrap.class, true);

        if (existing == null) {
            e.setEffect(copy());
        } else {
            existing.enforce();
        }
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state) {
        if (caster.isLocal()) {
            caster.findAllEntitiesInRange(5)
                .filter(e -> !(e instanceof IMagicals) && e instanceof EntityLivingBase)
                .map(e -> PlayerSpeciesList.instance().getCaster((EntityLivingBase)e))
                .forEach(this::entrap);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (previousTrappedPosition != null) {
            compound.setTag("previousTrappedPosition", NBTUtil.createPosTag(previousTrappedPosition));
        }
        compound.setInteger("struggle", struggleCounter);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        struggleCounter = compound.getInteger("struggle");

        if (compound.hasKey("previousTrappedPosition")) {
            previousTrappedPosition = NBTUtil.getPosFromTag(compound.getCompoundTag("previousTrappedPosition"));
        } else {
            previousTrappedPosition = null;
        }
    }
}
