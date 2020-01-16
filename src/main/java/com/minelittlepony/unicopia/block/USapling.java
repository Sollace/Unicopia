package com.minelittlepony.unicopia.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.util.LenientState;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class USapling extends BlockSapling implements ITreeGen {

    private ITreeGen treeGen;

    public USapling(String domain, String name) {
        setRegistryName(domain, name);
        setTranslationKey(name);
        setSoundType(SoundType.PLANT);

        setDefaultState(stateFactory.getDefaultState().with(STAGE, 0));
    }

    public USapling setTreeGen(ITreeGen gen) {
        treeGen = gen;

        return this;
    }

    @Override
    public void generateTree(World world, BlockPos pos, BlockState state, Random rand) {

        boolean massive = canGrowMassive();

        BlockPos i = massive ? findTwoByTwoSpace(world, pos, state) : pos;

        if (i == null) {
            massive = false;
            i = BlockPos.ORIGIN;
        }

        // remove the sapling and any contributing siblings before growing the tree
        setSaplingsState(world, Blocks.AIR.getDefaultState(), massive, i);

        if (!getTreeGen(world, state, massive).generate(world, rand, i)) {
            // place them back if tree growth failed
            setSaplingsState(world, state, massive, i);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    protected void setSaplingsState(World world, BlockState state, boolean massive, BlockPos pos) {
        if (massive) {
            world.setBlockState(pos             , state, 4);
            world.setBlockState(pos.add(1, 0, 0), state, 4);
            world.setBlockState(pos.add(0, 0, 1), state, 4);
            world.setBlockState(pos.add(1, 0, 1), state, 4);
        } else {
            world.setBlockState(pos, state, 4);
        }
    }

    @Override
    public WorldGenAbstractTree getTreeGen(World world, BlockState state, boolean massive) {
        return treeGen.getTreeGen(world, state, massive);
    }

    @Override
    public boolean canGrowMassive() {
        return treeGen.canGrowMassive();
    }

    /**
     * Looks for a suitable 2x2 space that a tree can grow in.
     * Returns null if no such spaces were found.
     */
    @Nullable
    public BlockPos findTwoByTwoSpace(World world, BlockPos pos, BlockState state) {
        BlockPos xNegP = pos.add(-1, 0,  0);
        BlockPos xPosP = pos.add( 1, 0,  0);

        BlockPos zNegP = pos.add( 0, 0, -1);
        BlockPos zPosP = pos.add( 0, 0,  1);

        boolean xNeg = isMatch(state, world.getBlockState(xNegP));
        boolean xPos = isMatch(state, world.getBlockState(xPosP));

        boolean zNeg = isMatch(state, world.getBlockState(zNegP));
        boolean zPos = isMatch(state, world.getBlockState(zPosP));

        if (xNeg && zNeg) {
            BlockPos corner = pos.add(-1, 0, -1);

            if (isMatch(state, world.getBlockState(corner))) {
                return corner;
            }
        }

        if (xNeg && zPos && isMatch(state, world.getBlockState(pos.add(-1, 0, 1)))) {
            return xNegP;
        }

        if (xPos && zNeg && isMatch(state, world.getBlockState(pos.add(1, 0, -1)))) {
            return pos;
        }

        if (xPos && zPos && isMatch(state, world.getBlockState(pos.add(-1, 0, 1)))) {
            return zNegP;
        }

        return null;
    }

    protected boolean isMatch(BlockState state, BlockState other) {
        return other.getBlock() == this;
    }

    @Deprecated
    @Override
    public boolean isTypeAt(World world, BlockPos pos, BlockPlanks.EnumType type) {
        return world.getBlockState(pos).getBlock() == this;
    }

    @Override
    public int damageDropped(BlockState state) {
        return 0;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().with(STAGE, (meta & 8) >> 3);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        int i = 0;
        i |= state.getValue(STAGE) << 3;
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new LenientState(this, STAGE);
    }
}
