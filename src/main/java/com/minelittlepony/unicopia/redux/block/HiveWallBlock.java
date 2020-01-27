package com.minelittlepony.unicopia.redux.block;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.USounds;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.core.util.PosHelper;
import com.minelittlepony.unicopia.core.util.shape.IShape;
import com.minelittlepony.unicopia.core.util.shape.Sphere;
import com.minelittlepony.unicopia.redux.UMaterials;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;

public class HiveWallBlock extends FallingBlock {

    public static final EnumProperty<State> STATE = EnumProperty.of("state", State.class);
    public static final EnumProperty<Axis> AXIS = EnumProperty.of("axis", Axis.class);

    private static final IShape shape = new Sphere(false, 1.5);

    public HiveWallBlock() {
        super(FabricBlockSettings.of(UMaterials.hive)
                .noCollision()
                .strength(10, 10)
                .hardness(2)
                .ticksRandomly()
                .lightLevel(1)
                .sounds(BlockSoundGroup.SAND)
                .build()
        );
        setDefaultState(stateFactory.getDefaultState()
                .with(STATE, State.GROWING).with(AXIS, Axis.Y)
        );

        // TODO:
       // setHarvestLevel("pickaxe", 1);
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {

        if (rand.nextInt(300) == 0) {
            world.playSound(null, pos, USounds.INSECT, SoundCategory.BLOCKS, 1, 1);
        }

        State type = getState(state);

        Axis axis = getAxis(state);

        int matchedNeighbours = countNeighbours(world, pos);

        if (type == State.GROWING) {
            if (testForAxis(world, pos, axis)) {
                world.setBlockState(pos, state.with(STATE, State.STABLE));
            } else {
                Axis newAxis = axis;

                for (Axis i : Axis.VALUES) {
                    if (testForAxis(world, pos, i)) {
                        newAxis = i;
                        break;
                    }
                }

                if (newAxis != axis) {
                    world.setBlockState(pos, state.with(AXIS, newAxis).with(STATE, State.STABLE));
                } else if (rand.nextInt(10) == 0) {
                    Direction facing = axis.randomFacing(rand);

                    BlockPos other = pos.offset(facing);

                    if (canSpreadInto(world, other, axis)) {
                        world.playSound(null, pos, USounds.SLIME_RETRACT, SoundCategory.BLOCKS, 1, 1);
                        world.setBlockState(other, state);
                        world.setBlockState(pos, state.with(STATE, State.STABLE));
                    }
                }
            }
        } else if (type == State.DYING) {
            if (matchedNeighbours > 1 && matchedNeighbours < 17) {
                world.setBlockState(pos, state.with(STATE, State.STABLE));
            } else {
                die(world, pos, rand);
            }
        } else {

            if (pos.getX() % 3 == 0 && pos.getZ() % 4 == 0 && isEmptySpace(world, pos.down()) && UBlocks.cuccoon.canPlaceBlockAt(world, pos.down())) {
                world.setBlockState(pos.down(), UBlocks.cuccoon.getDefaultState());
            } else if (!testForAxis(world, pos, axis)) {
                world.setBlockState(pos, state.with(STATE, State.GROWING));
            } else if (matchedNeighbours >= 27) {
                world.setBlockState(pos, state.with(STATE, State.DYING));
            } else {
                return;
            }
        }

        world.getBlockTickScheduler().schedule(pos, this, getTickRate(world));
    }

    public State getState(BlockState state) {
        return state.get(STATE);
    }

    public Axis getAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean uuuuh) {
        if (state.get(STATE) != State.STABLE) {
            super.onBlockAdded(state, world, pos, oldState, uuuuh);
        }
    }

    @Override
    public int getTickRate(ViewableWorld view) {
       return 10;
    }

    protected boolean testForAxis(World world, BlockPos pos, Axis axis) {
        return !PosHelper.some(pos, p -> isEmptySpace(world, p), axis.getFacings());
    }

    protected boolean isEmptySpace(World world, BlockPos pos) {

        if (world.isAir(pos)) {
            return true;
        }

        BlockState state = world.getBlockState(pos);

        return !(state.getMaterial().isLiquid()
                || state.isFullOpaque(world, pos)
                || state.isOpaque());
    }

    protected void die(World world, BlockPos pos, Random rand) {
        world.breakBlock(pos, false);

        PosHelper.all(pos, p -> {
            BlockState s = world.getBlockState(p);

            if (s.getBlock() == this) {
                notifyDying(world, p, s, rand);
            }
        }, Direction.values());
    }

    protected void notifyDying(World world, BlockPos pos, BlockState state, Random rand) {
        State oldState = state.get(STATE);
        State newState = oldState.downGrade();

        if (newState != oldState) {
            world.setBlockState(pos, state.with(STATE, newState));
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        if (entity instanceof PlayerEntity) {
            IPlayer player = SpeciesList.instance().getPlayer((PlayerEntity)entity);

            if (player.getSpecies() != Race.CHANGELING && !world.isClient) {
                if (((isEmptySpace(world, pos.down()) || canFallThrough(world.getBlockState(pos.down()))) && pos.getY() >= 0)) {
                    FallingBlockEntity faller = new FallingBlockEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.getBlockState(pos));
                    configureFallingBlockEntity(faller);
                    world.spawnEntity(faller);
                }
            }
        }
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingState, BlockState hitState) {
        world.breakBlock(pos, true);
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (hand == Hand.MAIN_HAND && player.getStackInHand(hand).isEmpty()) {
            IPlayer iplayer = SpeciesList.instance().getPlayer(player);

            if (iplayer.getSpecies() == Race.CHANGELING) {
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(AXIS, Axis.fromVanilla(context.getPlayerFacing().getAxis()));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(16) == 0) {
            Vec3d vel = shape.computePoint(rand);
            Vec3d vec = vel.add(pos.getX(), pos.getY(), pos.getZ());

            world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), vec.x, vec.y, vec.z, vel.x, vel.y, vel.z);
        }
    }

    public void retreat(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.playSound(null, pos, USounds.SLIME_RETRACT, SoundCategory.BLOCKS, 1, 1);
    }

    protected int countNeighbours(World world, BlockPos pos) {
        int count = 0;
        for (BlockPos i : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (world.getBlockState(i).getBlock() == this) {
                count++;
            }
        }

        return count;
    }

    protected boolean exposed(World world, BlockPos pos) {
        return PosHelper.some(pos, p -> isEmptySpace(world, p), Direction.values());
    }

    protected boolean canSpreadInto(World world, BlockPos pos, Axis axis) {
        if (world.isBlockLoaded(pos) && isEmptySpace(world, pos)) {
            boolean one = false;

            for (Direction facing : axis.getFacings()) {
                BlockPos op = pos.offset(facing);

                if (world.getBlockState(op).getMaterial() == UMaterials.hive) {
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
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.add(STATE).add(AXIS);
    }

    public enum State implements StringIdentifiable {
        GROWING,
        STABLE,
        DYING;

        static final State[] VALUES = values();

        @Override
        public String toString() {
            return asString();
        }

        @Override
        public String asString() {
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

    public enum Axis implements StringIdentifiable {
        X(Direction.Axis.X, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN),
        Y(Direction.Axis.Y, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH),
        Z(Direction.Axis.Z, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN);

        static final Axis[] VALUES = values();
        static final Map<Direction.Axis, Axis> AXIS_MAP = Maps.newEnumMap(Direction.Axis.class);

        private final Direction.Axis vanilla;
        private final Direction[] facings;

        static {
            for (Axis i : VALUES) {
                AXIS_MAP.put(i.vanilla, i);
            }
        }

        Axis(Direction.Axis vanilla, Direction... facings) {
            this.vanilla = vanilla;
            this.facings = facings;
        }

        @Override
        public String toString() {
            return asString();
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }

        public Direction randomFacing(Random rand) {
            return facings[rand.nextInt(facings.length)];
        }

        public Direction[] getFacings() {
            return facings;
        }

        public static Axis fromVanilla(Direction.Axis axis) {
            return AXIS_MAP.get(axis);
        }
    }
}
