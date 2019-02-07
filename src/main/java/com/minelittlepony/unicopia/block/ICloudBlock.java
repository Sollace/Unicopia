package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ICloudBlock {

    CloudType getCloudMaterialType(IBlockState blockState);

    default boolean handleRayTraceSpecialCases(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            EntityPlayer player = UClient.instance().getPlayer();

            if (player.capabilities.isCreativeMode) {
                return false;
            }

            if (!getCanInteract(state, player)) {
                return true;
            }

            CloudType type = getCloudMaterialType(state);

            ItemStack main = player.getHeldItemMainhand();
            if (main.isEmpty()) {
                main = player.getHeldItemOffhand();
            }

            if (!main.isEmpty() && main.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)main.getItem()).getBlock();

                if (block == null || block == Blocks.AIR) {
                    return false;
                }

                if (isPlacementExcempt(block)) {
                    if (Predicates.INTERACT_WITH_CLOUDS.apply(player)) {
                        return type == CloudType.NORMAL;
                    }
                    return type != CloudType.ENCHANTED;
                }

                if (!(block instanceof ICloudBlock)) {
                    return type != CloudType.ENCHANTED;
                }
            }
        }

        return false;
    }

    default boolean isPlacementExcempt(Block block) {
        return block instanceof BlockTorch
            || block instanceof BlockBed
            || block instanceof BlockChest;
    }

    default boolean applyLanding(Entity entity, float fallDistance) {
        if (!entity.isSneaking()) {
            entity.fall(fallDistance, 0);

            return false;
        }

        return true;
    }

    default boolean applyRebound(Entity entity) {
        if (!entity.isSneaking() && entity.motionY < 0) {
            if (Math.abs(entity.motionY) >= 0.25) {
                entity.motionY = -entity.motionY * 1.2;
            } else {
                entity.motionY = 0;
            }

            return true;
        }

        return false;
    }

    default boolean applyBouncyness(IBlockState state, Entity entity) {
        if (getCanInteract(state, entity)) {
            if (!entity.isSneaking() && Math.abs(entity.motionY) >= 0.25) {
                entity.motionY += 0.0155 * (entity.fallDistance < 1 ? 1 : entity.fallDistance);
            } else {
                entity.motionY = 0;
            }

            return true;
        }

        return false;
    }


    default boolean getCanInteract(IBlockState state, Entity e) {
        if (getCloudMaterialType(state).canInteract(e)) {
            if (e instanceof EntityItem) {
                // @FUF(reason = "There is no TickEvents.EntityTickEvent. Waiting on mixins...")
                e.setNoGravity(true);
            }
            return true;
        }

        return false;
    }

    default boolean isDense(IBlockState blockState) {
        return getCloudMaterialType(blockState) != CloudType.NORMAL;
    }

    /**
     * Determines whether falling sand entities should fall through this block.
     * @param state Our block state
     * @param world The current world
     * @param pos   The current position
     *
     * @return True to allow blocks to pass.
     */
    @FUF(reason = "Hacked until we can get mixins to implement a proper hook")
    default boolean allowsFallingBlockToPass(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (isDense(state)) {
            return false;
        }

        Block above = world.getBlockState(pos.up()).getBlock();
        return !(above instanceof ICloudBlock) && above instanceof BlockFalling;
    }
}
