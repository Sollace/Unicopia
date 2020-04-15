package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockGrowingCuccoon extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 7);
    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);

    public static final Box[] SHAFTS = new Box[] {
            new Box(7/16F, 0, 7/16F, 9/16F, 1, 7/16F),
            new Box(6/16F, 0, 6/16F, 10/16F, 1, 10/16F),
            new Box(5/16F, 0, 5/16F, 11/16F, 1, 11/16F),
            new Box(4/16F, 0, 4/16F, 12/16F, 1, 12/16F)
    };
    public static final Box[] BULBS = new Box[] {
            new Box(6/16F, 1/16F, 6/16F, 10/16F, 8/16F, 10/16F),
            new Box(4/16F, 0, 4/16F, 12/16F, 9/16F, 12/16F),
            new Box(3/16F, 0, 3/16F, 13/16F, 10/16F, 13/16F),
            new Box(2/16F, 0, 2/16F, 14/16F, 12/16F, 14/16F),
    };

    public BlockGrowingCuccoon() {
        super(UMaterials.HIVE);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setResistance(0);
        setSoundType(SoundType.SLIME);
        setDefaultSlipperiness(0.5F);
        setHarvestLevel("shovel", 2);
        setLightLevel(0.6F);
        setLightOpacity(0);

        useNeighborBrightness = true;

        setDefaultState(getBlockState().getBaseState()
                .with(AGE, 0)
                .with(SHAPE, Shape.BULB));
    }

    @Override
    public boolean isTranslucent(BlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    //Push player out of block
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Deprecated
    @Override
    public Box getCollisionBoundingBox(BlockState state, BlockView world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public void updateTick(World world, BlockPos pos, BlockState state, Random rand) {
        if (!checkSupport(world, pos)) {
            breakConnected(world, pos);
            return;
        }

        int age = state.get(AGE);

        BlockPos below = pos.down();

        if (world.isBlockLoaded(below)) {
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

        world.scheduleUpdate(pos, this, tickRate(world));
    }

    protected void breakConnected(World world, BlockPos pos) {
        world.destroyBlock(pos, true);

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
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return random.nextInt(3) == 0 ? state.get(AGE) : 0;
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return Items.SLIME_BALL;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && checkSupport(world, pos);
    }

    @Override
    public void onNeighborChange(BlockView world, BlockPos pos, BlockPos neighbor) {
        if (world instanceof World && !checkSupport(world, pos)) {
            breakConnected((World)world, pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        world.notifyNeighborsOfStateChange(pos, this, true);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, BlockState state) {
        world.scheduleUpdate(pos, this, 10);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity && !entity.removed) {
            LivingEntity living = (LivingEntity)entity;

            if (!EquinePredicates.BUGGY.test(living) && living.getHealth() > 0) {
                living.damage(MagicalDamageSource.ACID, 1);
                living.setInWeb();

                if (!world.isClient) {
                    if (living.getHealth() <= 0) {
                        living.dropItem(Items.BONE, 3);

                        if (living instanceof PlayerEntity) {
                            ItemStack skull = new ItemStack(Items.SKULL, 1);

                            if (world.rand.nextInt(13000) == 0) {
                                PlayerEntity player = (PlayerEntity)living;

                                skull.setTagCompound(new CompoundTag());
                                skull.getTagCompound().setTag("SkullOwner", NBTUtil.writeGameProfile(new CompoundTag(), player.getGameProfile()));
                                skull.setItemDamage(3);
                            } else {
                                living.dropItem(Items.SKULL, 1);
                            }

                            living.entityDropItem(skull, 0);
                        }
                    }
                }
            }
        }
    }

    public boolean checkSupport(BlockView world, BlockPos pos) {

        if (PosHelper.some(pos, p -> !world.isAirBlock(p), Direction.HORIZONTALS)) {
            return false;
        }

        pos = pos.up();

        BlockState above = world.getBlockState(pos);

        if (above.getBlock() == this || above.getBlock() == UBlocks.hive) {
            return true;
        }

        switch (above.getBlockFaceShape(world, pos, Direction.DOWN)) {
            case SOLID:
            case CENTER:
            case CENTER_BIG:
            case CENTER_SMALL: return true;
            default: return false;
        }
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(BlockState state, World world, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(world, pos);
        }

        int age = state.get(AGE) / 2;

        Vec3d offset = state.getOffset(world, pos);

        addCollisionBoxToList(pos, entityBox, collidingBoxes, SHAFTS[age % SHAFTS.length].offset(offset));

        if (state.get(SHAPE) == Shape.BULB) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BULBS[age % BULBS.length].offset(offset));
        }
    }

    @Deprecated
    @Override
    public Box getBoundingBox(BlockState state, BlockView source, BlockPos pos) {
        state = state.getActualState(source, pos);

        if (state.get(SHAPE) == Shape.BULB) {
            return BULBS[state.get(AGE) / 2].offset(state.getOffset(source, pos));
        }

        return SHAFTS[state.get(AGE) / 2].offset(state.getOffset(source, pos));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, SHAPE);
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

                Box bounds = BULBS[state.get(AGE) / 2]
                        .offset(pos)
                        .offset(state.getOffsetPos(world, pos));

                double x = bounds.minX + (bounds.maxX - bounds.minX) * rand.nextFloat();
                double y = bounds.minY;
                double z = bounds.minZ + (bounds.maxZ - bounds.minZ) * rand.nextFloat();

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
