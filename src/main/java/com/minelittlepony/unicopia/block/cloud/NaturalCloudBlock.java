package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.UBlocks;

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

    public NaturalCloudBlock(Settings settings, boolean meltable, @Nullable Supplier<Soakable> soggyBlock) {
        super(settings.nonOpaque(), meltable, soggyBlock);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isIn(ItemTags.SHOVELS)) {
            BooleanProperty property = CompactedCloudBlock.FACING_PROPERTIES.get(hit.getSide());
            world.setBlockState(pos, UBlocks.COMPACTED_CLOUD.getDefaultState().with(property, false));
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
