package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.WorldEvent;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean updateOnPerson(IPlayer caster) {
        EntityPlayer entity = caster.getOwner();

        if (entity.motionY > 0) {
            entity.playSound(SoundEvents.BLOCK_SLIME_HIT, 1, 1);
        }

        BlockPos origin = caster.getOrigin();

        if (previousTrappedPosition == null) {
            previousTrappedPosition = origin;

            setDirty(true);
        }

        if (!caster.getWorld().isRemote) {
            if (!origin.equals(previousTrappedPosition)) {
                previousTrappedPosition = origin;
                struggleCounter--;
                WorldEvent.DESTROY_BLOCK.play(caster.getWorld(), origin, Blocks.SLIME_BLOCK.getDefaultState());

                setDirty(true);
            }
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
        entity.capabilities.isFlying = false;

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
    public void renderOnPerson(IPlayer source) {
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

    protected void entrap(IPlayer e) {

        SpellChangelingTrap existing = e.getEffect(SpellChangelingTrap.class, true);

        if (existing == null) {
            e.setEffect(copy());
        } else {
            existing.enforce();
        }
    }

    @Override
    public void onImpact(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            VecHelper.findAllEntitiesInRange(null, world, pos, 5)
                .filter(e -> e instanceof EntityPlayer)
                .map(e -> PlayerSpeciesList.instance().getPlayer((EntityPlayer)e))
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
