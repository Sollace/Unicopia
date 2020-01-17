package com.minelittlepony.unicopia.magic.spells;

import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IHeldEffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class SpellLight extends GenericSpell implements IHeldEffect {

    private BlockPos lastPos;
    private ICaster<?> source;

    public SpellLight() {
        super("light", 0xF7FACB, Affinity.GOOD);
    }

    @Override
    public void onPlaced(ICaster<?> caster) {
        this.source = caster;
    }

    @Override
    public void setDead() {
        super.setDead();

        if (lastPos != null) {
            source.getWorld().checkLight(lastPos);
        }
    }

    @Override
    public void updateInHand(IPlayer caster, Affinity affinity) {
        if (caster.getSpecies().canCast()) {
            update(caster);
        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        this.source = source;

        BlockPos pos = source.getOrigin().down();

        World world = source.getWorld();

        if (lastPos != null && !lastPos.equals(pos)) {
            world.checkLight(lastPos);
        }

        lastPos = pos;


        int light = world.getLightFor(EnumSkyBlock.BLOCK, pos);

        if (light < 8) {
            world.setLightFor(EnumSkyBlock.BLOCK, pos, 8);
            world.notifyLightSet(pos);
            world.checkLight(pos.up());
        }

        return true;
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (compound.hasKey("lastX")) {
            lastPos = new BlockPos(compound.getInteger("lastX"), compound.getInteger("lastY"), compound.getInteger("lastZ"));
        } else {
            lastPos = null;
        }
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (lastPos != null) {
            compound.setInteger("lastX", lastPos.getX());
            compound.setInteger("lastY", lastPos.getY());
            compound.setInteger("lastZ", lastPos.getZ());
        }
    }
}
