package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.init.UBlocks;
import com.minelittlepony.unicopia.init.UMaterials;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGrowingCuccoon extends Block {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
    public static final PropertyEnum<Shape> SHAPE = PropertyEnum.create("shape", Shape.class);

    public static final AxisAlignedBB[] SHAFTS = new AxisAlignedBB[] {
            new AxisAlignedBB(7/16F, 0, 7/16F, 9/16F, 1, 7/16F),
            new AxisAlignedBB(6/16F, 0, 6/16F, 10/16F, 1, 10/16F),
            new AxisAlignedBB(5/16F, 0, 5/16F, 11/16F, 1, 11/16F),
            new AxisAlignedBB(4/16F, 0, 4/16F, 12/16F, 1, 12/16F)
    };
    public static final AxisAlignedBB[] BULBS = new AxisAlignedBB[] {
            new AxisAlignedBB(6/16F, 1/16F, 6/16F, 10/16F, 8/16F, 10/16F),
            new AxisAlignedBB(4/16F, 0, 4/16F, 12/16F, 9/16F, 12/16F),
            new AxisAlignedBB(3/16F, 0, 3/16F, 13/16F, 10/16F, 13/16F),
            new AxisAlignedBB(2/16F, 0, 2/16F, 14/16F, 12/16F, 14/16F),
    };

    public BlockGrowingCuccoon(String domain, String name) {
        super(UMaterials.hive);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setResistance(0);
        setSoundType(SoundType.SLIME);
        setCreativeTab(CreativeTabs.MATERIALS);
        setDefaultSlipperiness(0.5F);
        setHarvestLevel("shovel", 2);
        setLightLevel(0.6F);
        setLightOpacity(0);

        useNeighborBrightness = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(AGE, 0)
                .withProperty(SHAPE, Shape.BULB));
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    //Push player out of block
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!checkSupport(world, pos)) {
            world.destroyBlock(pos, true);
            return;
        }

        int age = state.getValue(AGE);

        BlockPos below = pos.down();

        if (world.isBlockLoaded(below)) {
            boolean spaceBelow = world.isAirBlock(below);

            Shape shape = state.getValue(SHAPE);

            if (shape == Shape.STRING && spaceBelow) {
                world.setBlockState(pos, state.withProperty(SHAPE, Shape.BULB).withProperty(AGE, age / 2));
            } else if (shape == Shape.BULB && !spaceBelow) {
                world.setBlockState(pos, state.withProperty(SHAPE, Shape.STRING).withProperty(AGE, age / 2));
            } else if (age >= 7) {
                if (rand.nextInt(12) == 0 && spaceBelow) {
                    world.setBlockState(below, state.withProperty(AGE, age / 2));
                    world.setBlockState(pos, getDefaultState().withProperty(AGE, age / 2).withProperty(SHAPE, Shape.STRING));
                    world.playSound(null, pos, USounds.SLIME_ADVANCE, SoundCategory.BLOCKS, 1, 1);
                }
            } else {
                if (age < getMaximumAge(world, pos, state, spaceBelow)) {
                    if (rand.nextInt(5 * (age + 1)) == 0) {
                        world.setBlockState(pos, state.cycleProperty(AGE));
                    }
                }
            }
        }

        world.scheduleUpdate(pos, this, tickRate(world));
    }

    protected int getMaximumAge(World world, BlockPos pos, IBlockState state, boolean spaceBelow) {
        if (state.getValue(SHAPE) == Shape.STRING) {
            IBlockState higher = world.getBlockState(pos.up());

            if (higher.getBlock() != this) {
                return 7;
            }

            return ((BlockGrowingCuccoon)higher.getBlock()).getMaximumAge(world, pos.up(), higher, false) - 1;
        }

        if (!spaceBelow) {
            return 0;
        }

        return 7;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.SLIME_BALL;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (state.getValue(AGE) == 7) {
            super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && checkSupport(world, pos);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        if (world instanceof World && !checkSupport(world, pos)) {
            ((World)world).destroyBlock(pos, true);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        world.notifyNeighborsOfStateChange(pos, this, true);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, 10);
    }

    public boolean checkSupport(IBlockAccess world, BlockPos pos) {

        if (PosHelper.some(pos, p -> !world.isAirBlock(p), EnumFacing.HORIZONTALS)) {
            return false;
        }

        pos = pos.up();

        IBlockState above = world.getBlockState(pos);

        if (above.getBlock() == this || above.getBlock() == UBlocks.hive) {
            return true;
        }

        switch (above.getBlockFaceShape(world, pos, EnumFacing.DOWN)) {
            case SOLID:
            case CENTER:
            case CENTER_BIG:
            case CENTER_SMALL: return true;
            default: return false;
        }
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(world, pos);
        }

        int age = state.getValue(AGE) / 2;

        Vec3d offset = state.getOffset(world, pos);

        addCollisionBoxToList(pos, entityBox, collidingBoxes, SHAFTS[age % SHAFTS.length].offset(offset));

        if (state.getValue(SHAPE) == Shape.BULB) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BULBS[age % BULBS.length].offset(offset));
        }
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);

        if (state.getValue(SHAPE) == Shape.BULB) {
            return BULBS[state.getValue(AGE) / 2].offset(state.getOffset(source, pos));
        }

        return SHAFTS[state.getValue(AGE) / 2].offset(state.getOffset(source, pos));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(AGE, meta % 8)
                .withProperty(SHAPE, Shape.VALUES[(meta >> 3) % Shape.VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE) | (state.getValue(SHAPE).ordinal() << 3);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, SHAPE);
    }

    static enum Shape implements IStringSerializable {
        BULB,
        STRING;

        static final Shape[] VALUES = values();

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

    }
}
