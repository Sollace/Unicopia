package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.gas.CloudType;
import com.minelittlepony.unicopia.gas.Gas;
import com.minelittlepony.unicopia.util.particles.UParticles;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GlowingGemBlock extends TorchBlock implements Gas {

    public static BooleanProperty ON = BooleanProperty.of("on");

    private static final double A = 5/16D;
    private static final double B = 6/16D;

    private static final double C = 10/16D;

    // tiltedOffWall
    private static final double F = 10/16D;

    // tiltedMinY
    private static final double E = 3/16D;

    protected static final VoxelShape STANDING_AABB = VoxelShapes.cuboid(new Box(
            7/16D, 0, 7/16D,
            9/16D, 1, 9/16D
    ));
    protected static final VoxelShape TORCH_NORTH_AABB = VoxelShapes.cuboid(new Box(B, E, F, C, 1, 1));
    protected static final VoxelShape TORCH_SOUTH_AABB = VoxelShapes.cuboid(new Box(B, E, 0, C, 1, A));
    protected static final VoxelShape TORCH_WEST_AABB  = VoxelShapes.cuboid(new Box(F, E, B, 1, 1, C));
    protected static final VoxelShape TORCH_EAST_AABB  = VoxelShapes.cuboid(new Box(0, E, B, A, 1, C));

    public GlowingGemBlock() {
        super(FabricBlockSettings.of(Material.PART)
                .noCollision()
                .strength(0, 0)
                .ticksRandomly()
                .lightLevel(1)
                .sounds(BlockSoundGroup.GLASS)
                .build()
        );
        setDefaultState(stateFactory.getDefaultState()
                .with(Properties.FACING, Direction.UP)
                .with(ON, true)
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView source, BlockPos pos, EntityContext context) {
        switch (state.get(Properties.FACING)) {
            case EAST: return TORCH_EAST_AABB;
            case WEST: return TORCH_WEST_AABB;
            case SOUTH: return TORCH_SOUTH_AABB;
            case NORTH: return TORCH_NORTH_AABB;
            default: return STANDING_AABB;
        }
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!state.get(ON)) {
            ItemStack held = player.getStackInHand(hand);
            if (!held.isEmpty() && (held.getItem() == Items.FLINT_AND_STEEL || held.getItem() == Items.FIRE_CHARGE)) {

                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(ON, true));

                if (held.getItem() == Items.FLINT_AND_STEEL) {
                    held.damage(1, player, p -> p.sendToolBreakStatus(hand));
                } else if (!player.abilities.creativeMode) {
                    held.decrement(1);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand) {
        Direction facing = state.get(Properties.FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5;

        double drop = 0.22D;
        double variance = 0.27D;

        if (facing.getAxis().isHorizontal()) {
            facing = facing.getOpposite();

            x += variance * facing.getOffsetX();
            y += drop;
            z += variance * facing.getOffsetZ();
        }

        if (state.get(ON)) {
            for (int i = 0; i < 3; i++) {
                world.addParticle(UParticles.UNICORN_MAGIC,
                        x - 0.3, y - 0.3, z - 0.3,
                        rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            }
        } else {
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.hasRain(pos)) {
            if (state.get(ON)) {
                world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(ON, false));
            }
        } else {
            if (!state.get(ON)) {
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(ON, true));
            }
        }
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return state.get(ON) && side == Direction.DOWN ? state.getWeakRedstonePower(world, pos, side) : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return state.get(ON) && state.get(Properties.FACING) != side ? 12 : 0;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.ENCHANTED;
    }

    // TODO: this is a loot table now
    /*@Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder context) {

        Random rand = context.world instanceof World ? ((World)world).random : random;

        if (rand.nextInt(10) == 0) {
            drops.add(new ItemStack(UItems.spell));
        } else {
            drops.add(new ItemStack(UItems.curse));
        }

        return drops;
    }*/

    @Override
    public int getLuminance(BlockState state) {
        if (state.get(ON)) {
            return super.getLuminance(state);
        }

        return 0;
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING).add(ON);
    }
}
