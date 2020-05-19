package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.gas.CloudFarmlandBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class StickPlantBlock extends CropBlock {

    private static final VoxelShape BOUNDING_BOX = VoxelShapes.cuboid(new Box(
            7/16F, -1/16F, 7/16F,
            9/16F, 15/16F, 9/16F
    ));

    private final ItemConvertible seeds;
    private final ItemConvertible crop;
    private final ItemConvertible waste;

    public StickPlantBlock(Settings settings, ItemConvertible seeds, ItemConvertible crop, ItemConvertible waste) {
        super(settings);
        this.seeds = seeds;
        this.crop = crop;
        this.waste = waste;
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView source, BlockPos pos, EntityContext context) {
        Vec3d off = state.getOffsetPos(source, pos);
        return BOUNDING_BOX.offset(off.x, off.y, off.z);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }

    @Override
    public Item getSeedsItem() {
        return seeds.asItem();
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView view, BlockPos pos) {
        Block block = floor.getBlock();

        if (seeds.asItem() == Items.AIR) {
            return block instanceof StickPlantBlock
                || block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL || block == Blocks.FARMLAND || block == UBlocks.CLOUD_FARMLAND;
        }
        return super.canPlantOnTop(floor, view, pos) || block == UBlocks.CLOUD_FARMLAND;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (hand == Hand.MAIN_HAND && isMature(state)) {
            if (player.getStackInHand(hand).isEmpty()) {
                int good = getAge(state);
                int rotten = world.random.nextInt(good);

                good -= rotten;

                if (good > 0) {
                    dropStack(world, pos, new ItemStack(crop.asItem(), good));
                }
                if (rotten > 0) {
                    dropStack(world, pos, new ItemStack(waste.asItem(), rotten));
                }

                world.setBlockState(pos, state.with(getAgeProperty(), 0));

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int age = Math.min(getAge(state) + getGrowthAmount(world), getMaxAge());

        world.setBlockState(pos, state.with(getAgeProperty(), age), 2);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getPlacedState(context.getWorld(), context.getBlockPos().down(), context.getWorld().getBlockState(context.getBlockPos().down()));
    }

    public BlockState getPlacedState(World world, BlockPos pos, BlockState state) {

        if (state.getBlock() instanceof CloudFarmlandBlock) {
            return UBlocks.CLOUDSDALE_TOMATO_PLANT.getDefaultState();
        }
        if (state.getBlock() instanceof FarmlandBlock) {
            return UBlocks.TOMATO_PLANT.getDefaultState();
        }

        return getDefaultState();
    }
}
