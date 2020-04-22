package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class BlockGrowingCuccoon extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 7);
    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);

    public static final VoxelShape[] SHAFTS = new VoxelShape[] {
            Block.createCuboidShape(7, 0, 7, 9, 16, 7),
            Block.createCuboidShape(6, 0, 6, 10, 16, 10),
            Block.createCuboidShape(5, 0, 5, 11, 16, 11),
            Block.createCuboidShape(4, 0, 4, 12, 16, 12)
    };
    public static final VoxelShape[] BULBS = new VoxelShape[] {
            Block.createCuboidShape(6, 1, 6, 10, 8, 10),
            Block.createCuboidShape(4, 0, 4, 12, 9, 12),
            Block.createCuboidShape(3, 0, 3, 13, 10, 13),
            Block.createCuboidShape(2, 0, 2, 14, 12, 14),
    };

    public BlockGrowingCuccoon() {
        super(FabricBlockSettings.of(UMaterials.HIVE)
                .ticksRandomly()
                .breakInstantly()
                .lightLevel(9)
                .slipperiness(0.5F)
                .sounds(BlockSoundGroup.SLIME)
                .breakByTool(FabricToolTags.SHOVELS, 2)
                .build()
        );

        setDefaultState(stateManager.getDefaultState()
                .with(AGE, 0)
                .with(SHAPE, Shape.BULB));
    }

    // TODO: loot table
    /*
    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return random.nextInt(3) == 0 ? state.get(AGE) : 0;
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return Items.SLIME_BALL;
    }*/

    @Override
    public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
        return true;
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!checkSupport(world, pos)) {
            breakConnected(world, pos);
            return;
        }

        int age = state.get(AGE);

        BlockPos below = pos.down();

        if (world.isChunkLoaded(below)) {
            boolean spaceBelow = world.isAir(below);

            Shape shape = state.get(SHAPE);

            if (shape == Shape.STRING && spaceBelow) {
                world.setBlockState(pos, state.with(SHAPE, Shape.BULB).with(AGE, age / 2));
            } else if (shape == Shape.BULB && !spaceBelow) {
                world.setBlockState(pos, state.with(SHAPE, Shape.STRING).with(AGE, age / 2));
            } else if (age >= 7) {
                if (rand.nextInt(12) == 0 && spaceBelow) {
                    world.setBlockState(below, state.with(AGE, age / 2));
                    world.setBlockState(pos, getDefaultState().with(AGE, age / 2).with(SHAPE, Shape.STRING));
                    world.playSound(null, pos, USounds.SLIME_ADVANCE, SoundCategory.BLOCKS, 1, 1);
                }
            } else {
                if (age < getMaximumAge(world, pos, state, spaceBelow)) {
                    if (rand.nextInt(5 * (age + 1)) == 0) {
                        world.setBlockState(pos, state.cycle(AGE));
                    }
                }
            }
        }
    }

    protected void breakConnected(World world, BlockPos pos) {
        world.breakBlock(pos, true);

        pos = pos.down();
        if (world.getBlockState(pos).getBlock() == this) {
            breakConnected(world, pos);
        }
    }

    protected int getMaximumAge(World world, BlockPos pos, BlockState state, boolean spaceBelow) {
        if (state.get(SHAPE) == Shape.STRING) {
            BlockState higher = world.getBlockState(pos.up());

            if (higher.getBlock() != this) {
                return 7;
            }

            return Math.min(higher.get(AGE),
                    ((BlockGrowingCuccoon)higher.getBlock()).getMaximumAge(world, pos.up(), higher, false) - 1
                );
        }

        if (!spaceBelow) {
            return 0;
        }

        return 7;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return super.canPlaceAt(state, world, pos) && checkSupport(world, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos otherPos, boolean change) {
        super.neighborUpdate(state, world, pos, otherBlock, otherPos, change);
        if (!checkSupport(world, pos)) {
            breakConnected(world, pos);
        }
    }

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState replacement, boolean boolean_1) {
        world.updateNeighborsAlways(pos, this);
        super.onBlockRemoved(state, world, pos, replacement, boolean_1);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && !entity.removed) {
            LivingEntity living = (LivingEntity)entity;

            if (!EquinePredicates.BUGGY.test(living) && living.getHealth() > 0) {
                living.damage(MagicalDamageSource.ACID, 1);
                living.slowMovement(state, new Vec3d(0.25D, 0.05000000074505806D, 0.25D));

                if (!world.isClient) {
                    if (living.getHealth() <= 0) {
                        living.dropItem(Items.BONE, 3);

                        if (living instanceof PlayerEntity) {
                            if (world.random.nextInt(13000) == 0) {
                                ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
                                PlayerEntity player = (PlayerEntity)living;

                                skull.setTag(new CompoundTag());
                                skull.getTag().put("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), player.getGameProfile()));
                                player.dropItem(skull, true);
                            } else {
                                living.dropItem(Items.SKELETON_SKULL, 1);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkSupport(BlockView world, BlockPos pos) {

        if (PosHelper.some(pos, p -> !world.getBlockState(p).isAir(), Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
            return false;
        }

        pos = pos.up();

        BlockState above = world.getBlockState(pos);

        if (above.getBlock() == this || above.getBlock() == UBlocks.hive) {
            return true;
        }

        return Block.isFaceFullSquare(above.getCollisionShape(world, pos), Direction.DOWN);
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
        Vec3d offset = getOffsetPos(state, view, pos);


        if (state.get(SHAPE) == Shape.BULB) {
            return BULBS[state.get(AGE) / 2].offset(offset.x, offset.y, offset.z);
        }

        return SHAFTS[state.get(AGE) / 2].offset(offset.x, offset.y, offset.z);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, SHAPE);
    }

    // TODO: isLadder
    /*@Override
    public boolean isLadder(BlockState state, BlockView world, BlockPos pos, LivingEntity entity) {
        return true;
    }*/

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (state.get(SHAPE) == Shape.BULB) {
            if (rand.nextInt(8) == 0) {

                Vec3d offset = state.getOffsetPos(world, pos).add(pos.getX(), pos.getY(), pos.getZ());
                Box bounds = BULBS[state.get(AGE) / 2]
                        .offset(offset.x, offset.y, offset.z)
                        .getBoundingBox();

                double x = bounds.x1 + (bounds.x2 - bounds.x1) * rand.nextFloat();
                double y = bounds.y1;
                double z = bounds.z1 + (bounds.z2 - bounds.z1) * rand.nextFloat();

                world.addParticle(ParticleTypes.DRIPPING_LAVA, x, y, z, 0, 0, 0);
            }
        }
    }

    enum Shape implements StringIdentifiable {
        BULB,
        STRING;

        static final Shape[] VALUES = values();

        @Override
        public String toString() {
            return asString();
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }

    }
}
