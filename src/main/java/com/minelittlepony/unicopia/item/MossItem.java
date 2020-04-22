package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.toxin.ToxicItem;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.util.collection.ReversableBlockStateMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MossItem extends ToxicItem {

    public static final ReversableBlockStateMap AFFECTED = Util.make(new ReversableBlockStateMap(), a -> {
        // TODO: move to resourcepack
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.COBBLESTONE_SLAB);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.COBBLESTONE_STAIRS);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.STONE_BRICK_SLAB);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS);
        a.replaceBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
    });

    public MossItem(Item.Settings settings) {
        super(settings, 2, 1, UseAction.EAT, Toxicity.FAIR);
    }

    public boolean tryConvert(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player) {
        BlockState converted = AFFECTED.getConverted(state);

        if (!state.equals(converted)) {
            world.setBlockState(pos, converted, 3);

            world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1, 1);

            int amount = 1;

            if (player != null && Pony.of(player).getSpecies().canUseEarth()) {
                amount = world.random.nextInt(4);
            }

            Block.dropStack(world, pos, new ItemStack(this, amount));

            return true;
        }

        return false;
    }

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return Toxicity.MILD;
    }
}
