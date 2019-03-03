package com.minelittlepony.unicopia.spell;

import com.minelittlepony.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class SpellLight extends GenericSpell {

    private BlockPos lastPos;
    private ICaster<?> source;

    public SpellLight() {
        super("light", 0xF7FACB, SpellAffinity.GOOD);
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

    protected void resetLight(BlockPos pos) {

    }
}
