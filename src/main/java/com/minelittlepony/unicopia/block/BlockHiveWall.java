package com.minelittlepony.unicopia.block;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UMaterials;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockHiveWall extends BlockFalling {

    public static final PropertyEnum<State> STATE = PropertyEnum.create("state", State.class);
    public static final PropertyEnum<Axis> AXIS = PropertyEnum.create("axis", Axis.class);

    private static final IShape shape = new Sphere(false, 1.5);

    public BlockHiveWall(String domain, String name) {
        super(UMaterials.hive);

        setTranslationKey(name);
        setRegistryName(domain, name);

        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setDefaultState(blockState.getBaseState().withProperty(STATE, State.GROWING).withProperty(AXIS, Axis.Y));
        setHardness(2);
        setSoundType(SoundType.SLIME);
        setHarvestLevel("pickaxe", 1);
        setResistance(2);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

        if (rand.nextInt(300) == 0) {
            world.playSound(null, pos, USounds.INSECT, SoundCategory.BLOCKS, 1, 1);
        }

        State type = getState(state);

        Axis axis = getAxis(state);

        int matchedNeighbours = countNeighbours(world, pos);

        if (type == State.GROWING) {
            if (testForAxis(world, pos, axis)) {
                world.setBlockState(pos, state.withProperty(STATE, State.STABLE));
            } else {
                Axis newAxis = axis;

                for (Axis i : Axis.VALUES) {
                    if (testForAxis(world, pos, i)) {
                        newAxis = i;
                        break;
                    }
                }

                if (newAxis != axis) {
                    world.setBlockState(pos, state.withProperty(AXIS, newAxis).withProperty(STATE, State.STABLE));
                } else if (rand.nextInt(10) == 0) {
                    EnumFacing facing = axis.randomFacing(rand);

                    BlockPos other = pos.offset(facing);

                    if (canSpreadInto(world, other, axis)) {
                        world.playSound(null, pos, USounds.SLIME_RETRACT, SoundCategory.BLOCKS, 1, 1);
                        world.setBlockState(other, state);
                        world.setBlockState(pos, state.withProperty(STATE, State.STABLE));
                    }
                }
            }
        } else if (type == State.DYING) {
            if (matchedNeighbours > 1 && matchedNeighbours < 27) {
                world.setBlockState(pos, state.withProperty(STATE, State.STABLE));
            } else {
                die(world, pos, rand);
            }
        } else {
            if (!testForAxis(world, pos, axis)) {
                world.setBlockState(pos, state.withProperty(STATE, State.GROWING));
            } else if (matchedNeighbours >= 27) {
                world.setBlockState(pos, state.withProperty(STATE, State.DYING));
            } else {
                return;
            }
        }

        world.scheduleUpdate(pos, this, tickRate(world));
    }

    public State getState(IBlockState state) {
        return state.getValue(STATE);
    }

    public Axis getAxis(IBlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(STATE) != State.STABLE) {
            world.scheduleUpdate(pos, this, 10);
        }
    }

    protected boolean testForAxis(World world, BlockPos pos, Axis axis) {
        return !PosHelper.some(pos, world::isAirBlock, axis.getFacings());
    }

    protected void die(World world, BlockPos pos, Random rand) {
        world.destroyBlock(pos, false);

        PosHelper.all(pos, p -> {
            IBlockState s = world.getBlockState(p);

            if (s.getBlock() == this) {
                notifyDying(world, p, s, rand);
            }
        }, EnumFacing.VALUES);
    }

    protected void notifyDying(World world, BlockPos pos, IBlockState state, Random rand) {
        State oldState = state.getValue(STATE);
        State newState = oldState.downGrade();

        if (newState != oldState) {
            world.setBlockState(pos, state.withProperty(STATE, newState));
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        this.setResistance(10);
        if (entity instanceof EntityPlayer) {
            IPlayer player = PlayerSpeciesList.instance().getPlayer((EntityPlayer)entity);

            IBlockState state = world.getBlockState(pos);

            if (player.getPlayerSpecies() != Race.CHANGELING) {
                if (!world.isRemote) {
                    if (((world.isAirBlock(pos.down()) || canFallThrough(world.getBlockState(pos.down()))) && pos.getY() >= 0)) {
                        EntityFallingBlock faller = new EntityFallingBlock(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
                        onStartFalling(faller);
                        world.spawnEntity(faller);
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (playerIn.getHeldItem(hand).isEmpty()) {
            IPlayer player = PlayerSpeciesList.instance().getPlayer(playerIn);

            if (player.getPlayerSpecies() == Race.CHANGELING) {
                retreat(world, pos);

                PosHelper.adjacentNeighbours(pos).forEach(p -> {
                    if (world.getBlockState(p).getBlock() == this) {
                        retreat(world, p);
                    }
                });

                return true;
            }
        }

        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(AXIS, Axis.fromVanilla(facing.getAxis()));
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(16) == 0) {
            Vec3d vel = shape.computePoint(rand);
            Vec3d vec = vel.add(pos.getX(), pos.getY(), pos.getZ());

            world.spawnParticle(EnumParticleTypes.BLOCK_DUST,
                    vec.x, vec.y, vec.z,
                    vel.x, vel.y, vel.z,
                    Block.getStateId(state));
        }
    }

    public void retreat(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.playSound(null, pos, USounds.SLIME_RETRACT, SoundCategory.BLOCKS, 1, 1);
    }

    protected int countNeighbours(World world, BlockPos pos) {
        int count = 0;
        for (BlockPos i : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (world.getBlockState(i).getBlock() == this) {
                count++;
            }
        }

        return count;
    }

    protected boolean exposed(World world, BlockPos pos) {
        return PosHelper.some(pos, world::isAirBlock, EnumFacing.VALUES);
    }

    protected boolean canSpreadInto(World world, BlockPos pos, Axis axis) {
        if (world.isBlockLoaded(pos) && world.isAirBlock(pos)) {
            boolean one = false;

            for (EnumFacing facing : axis.getFacings()) {
                BlockPos op = pos.offset(facing);

                if (!world.isAirBlock(op) && !world.getBlockState(op).getMaterial().isLiquid()) {
                    if (one) {
                        return true;
                    }

                    one = true;
                }
            }

        }

        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(STATE, State.VALUES[meta % State.VALUES.length])
                .withProperty(AXIS, Axis.VALUES[(meta << 2) % Axis.VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return getState(state).ordinal() | (getAxis(state).ordinal() >> 2);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STATE, AXIS);
    }

    public static enum State implements IStringSerializable {
        GROWING,
        STABLE,
        DYING;

        static final State[] VALUES = values();

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        public State upgrade() {
            switch (this) {
                case DYING: return STABLE;
                default: return GROWING;
            }
        }

        public State downGrade() {
            switch (this) {
                case GROWING: return STABLE;
                default: return DYING;
            }
        }
    }

    public static enum Axis implements IStringSerializable {
        X(EnumFacing.Axis.X, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.UP, EnumFacing.DOWN),
        Y(EnumFacing.Axis.Y, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH),
        Z(EnumFacing.Axis.Z, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN);

        static final Axis[] VALUES = values();
        static final Map<EnumFacing.Axis, Axis> AXIS_MAP = Maps.newEnumMap(EnumFacing.Axis.class);

        private final EnumFacing.Axis vanilla;
        private final EnumFacing[] facings;

        static {
            for (Axis i : VALUES) {
                AXIS_MAP.put(i.vanilla, i);
            }
        }

        Axis(EnumFacing.Axis vanilla, EnumFacing... facings) {
            this.vanilla = vanilla;
            this.facings = facings;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        public EnumFacing randomFacing(Random rand) {
            return facings[rand.nextInt(facings.length)];
        }

        public EnumFacing[] getFacings() {
            return facings;
        }

        public static Axis fromVanilla(EnumFacing.Axis axis) {
            return AXIS_MAP.get(axis);
        }
    }
}
