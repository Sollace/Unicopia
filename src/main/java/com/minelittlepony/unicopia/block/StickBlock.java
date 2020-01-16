package com.minelittlepony.unicopia.block;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LeadItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;

public class StickBlock extends Block {

    static final VoxelShape BOUNDING_BOX = VoxelShapes.cuboid(new Box(
            7/16F, -1/16F, 7/16F,
            9/16F, 15/16F, 9/16F
    ));

    public StickBlock(String domain, String name) {
        super(FabricBlockSettings.of(Material.PLANT)
                .noCollision()
                .strength(0.2F, 0.2F)
                .ticksRandomly()
                .lightLevel(1)
                .sounds(BlockSoundGroup.WOOD)
                .build()
        );

        // TODO:
        // drops Items.STICK x1
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
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
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            return LeadItem.attachHeldMobsToBlock(player, world, pos);
        }

        ItemStack stack = player.getStackInHand(hand);

        return stack.getItem() == Items.LEAD || stack.isEmpty();
    }

    @Override
    public boolean canPlaceAt(BlockState state, ViewableWorld world, BlockPos pos) {
        Block block = state.getBlock();

        return block instanceof StickBlock || block instanceof FarmlandBlock;
    }
}
