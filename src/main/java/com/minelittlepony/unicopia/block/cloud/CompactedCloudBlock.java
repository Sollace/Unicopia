package com.minelittlepony.unicopia.block.cloud;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.minelittlepony.unicopia.EquineContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CompactedCloudBlock extends CloudBlock {
    static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    static final Collection<BooleanProperty> PROPERTIES = FACING_PROPERTIES.values();

    static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        return Block.createCuboidShape(
                state.get(ConnectingBlock.WEST) ? 0 : 2,
                state.get(ConnectingBlock.DOWN) ? 0 : 2,
                state.get(ConnectingBlock.NORTH) ? 0 : 2,
                state.get(ConnectingBlock.EAST) ? 16 : 14,
                state.get(ConnectingBlock.UP) ? 16 : 14,
                state.get(ConnectingBlock.SOUTH) ? 16 : 14
        );
    });

    public CompactedCloudBlock(Settings settings) {
        super(settings, true);
        PROPERTIES.forEach(property -> {
            setDefaultState(getDefaultState().with(property, true));
        });
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isIn(ItemTags.SHOVELS)) {
            BooleanProperty property = FACING_PROPERTIES.get(hit.getSide());
            if (state.get(property)) {
                world.setBlockState(pos, state.with(property, false));
                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
