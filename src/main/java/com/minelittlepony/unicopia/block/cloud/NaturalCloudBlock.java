package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NaturalCloudBlock extends PoreousCloudBlock {

    private final Supplier<Block> compactedBlock;

    public NaturalCloudBlock(Settings settings, boolean meltable,
            @Nullable Supplier<Soakable> soggyBlock,
            Supplier<Block> compactedBlock) {
        super(settings, meltable, soggyBlock);
        this.compactedBlock = compactedBlock;
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isIn(ItemTags.SHOVELS)) {
            BooleanProperty property = CompactedCloudBlock.FACING_PROPERTIES.get(hit.getSide());
            world.setBlockState(pos, compactedBlock.get().getDefaultState().with(property, false));
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS);
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
}
