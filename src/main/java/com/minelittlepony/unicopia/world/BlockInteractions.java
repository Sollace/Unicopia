package com.minelittlepony.unicopia.world;

import java.util.List;

import com.minelittlepony.unicopia.block.ITillable;
import com.minelittlepony.unicopia.init.UBlocks;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInteractions {
    public boolean onBlockTilled(World world, BlockPos pos, EntityPlayer player, ItemStack hoe) {
        IBlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof ITillable)) {
            return false;
        }

        ITillable farm = ((ITillable)state.getBlock());

        if (!farm.canBeTilled(hoe, player, world, state, pos)) {
            return false;
        }

        world.setBlockState(pos, farm.getFarmlandState(hoe, player, world, state, pos));

        return true;
    }

    public EnumActionResult onBlockInteract(World world, IBlockState state, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing facing, EnumHand hand) {
        Item shill = UItems.Shills.getShill(stack.getItem());

        if (shill != null) {
            EnumActionResult result = shill.onItemUse(player, world, pos, hand, facing, 0, 0, 0);

            if (result == EnumActionResult.SUCCESS) {
                return result;
            }
        }

        Block shillBlock = UBlocks.Shills.getShill(state.getBlock());

        if (shillBlock != null && shillBlock.onBlockActivated(world, pos, state, player, hand, player.getHorizontalFacing(), 0.5F, 0.5F, 0.5F)) {
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public void addAuxiliaryDrops(World world, IBlockState state, BlockPos pos, List<ItemStack> drops, int fortune) {
        Block block = state.getBlock();

        int fortuneFactor = 1 + fortune * 15;

        if (block == Blocks.STONE) {
            if (world.rand.nextInt(500 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + fortune; i++) {
                    if (world.rand.nextInt(10) > 3) {
                        drops.add(new ItemStack(UItems.curse));
                    } else {
                        drops.add(new ItemStack(UItems.spell));
                    }
                }
            }

            if (world.rand.nextInt(5000) == 0) {
                drops.add(SpellRegistry.instance().enchantStack(new ItemStack(UItems.spell), "awkward"));
            }
        } else if (block == Blocks.DIRT || block == Blocks.CLAY || block == Blocks.GRASS_PATH || block == Blocks.GRASS || block == UBlocks.hive) {
            if (world.rand.nextInt(25 / fortuneFactor) == 0) {
                drops.add(new ItemStack(UItems.wheat_worms, 1 + fortune));
            }
        } else if (block instanceof BlockTallGrass) {
            if (world.rand.nextInt(25 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + fortune; i++) {
                    int chance = world.rand.nextInt(3);

                    if (chance == 0) {
                        drops.add(new ItemStack(UItems.alfalfa_seeds));
                    } else if (chance == 1) {
                        drops.add(new ItemStack(UItems.apple_seeds));
                    } else {
                        drops.add(new ItemStack(UItems.tomato_seeds));
                    }
                }
            }
        }
    }
}
