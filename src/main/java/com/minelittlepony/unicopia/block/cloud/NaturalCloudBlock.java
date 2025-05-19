package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NaturalCloudBlock extends PoreousCloudBlock {
    private static final MapCodec<NaturalCloudBlock> CODEC = RecordCodecBuilder.<NaturalCloudBlock>mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("meltable").forGetter(b -> b.meltable),
            CodecUtils.supplierOf(Soakable.CODEC).optionalFieldOf("soggy_block", null).forGetter(b -> b.soggyBlock),
            CodecUtils.supplierOf(Registries.BLOCK.getCodec()).fieldOf("compacted_block").forGetter(b -> b.compactedBlock),
            BedBlock.createSettingsCodec()
    ).apply(instance, NaturalCloudBlock::new));

    private final Supplier<Block> compactedBlock;

    public NaturalCloudBlock(boolean meltable,
            @Nullable Supplier<Soakable> soggyBlock,
            Supplier<Block> compactedBlock,
            Settings settings) {
        super(meltable, soggyBlock, settings.nonOpaque());
        this.compactedBlock = compactedBlock;
    }

    @Override
    public MapCodec<NaturalCloudBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isIn(ItemTags.SHOVELS)) {
            BooleanProperty property = CompactedCloudBlock.FACING_PROPERTIES.get(hit.getSide());
            world.setBlockState(pos, compactedBlock.get().getDefaultState().with(property, false));
            stack.damage(1, player, LivingEntity.getSlotForHand(hand));
            world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS);
            return ItemActionResult.SUCCESS;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }
}
