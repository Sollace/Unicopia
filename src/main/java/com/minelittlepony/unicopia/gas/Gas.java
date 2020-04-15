package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface Gas {

    CloudType getCloudMaterialType(BlockState blockState);

    default boolean handleRayTraceSpecialCases(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            PlayerEntity player = MinecraftClient.getInstance().player;

            if (player.abilities.creativeMode) {
                return false;
            }

            if (!getCanInteract(state, player)) {
                return true;
            }

            CloudType type = getCloudMaterialType(state);

            ItemStack main = player.getMainHandStack();
            if (main.isEmpty()) {
                main = player.getOffHandStack();
            }

            if (!main.isEmpty() && main.getItem() instanceof BlockItem) {
                Block block = ((BlockItem)main.getItem()).getBlock();
                BlockState heldState = block.getDefaultState();

                if (block == null || block.isAir(heldState)) {
                    return false;
                }

                if (block instanceof Gas) {
                    CloudType other = ((Gas)block).getCloudMaterialType(heldState);

                    if (other.canInteract(player)) {
                        return false;
                    }
                }

                if (!EquinePredicates.INTERACT_WITH_CLOUDS.apply(player)) {
                    return type != CloudType.ENCHANTED;
                }

                if (type == CloudType.NORMAL) {
                    return !isPlacementExcempt(block);
                }
            }
        }

        return false;
    }

    default boolean isPlacementExcempt(Block block) {
        return block instanceof TorchBlock
            || block instanceof BedBlock
            || block instanceof ChestBlock;
    }

    default boolean applyLanding(Entity entity, float fallDistance) {
        if (!entity.isSneaking()) {
            entity.handleFallDamage(fallDistance, 0);

            return false;
        }

        return true;
    }

    default boolean applyRebound(Entity entity) {

        Vec3d vel = entity.getVelocity();
        double y = vel.y;

        if (!entity.isSneaking() && y < 0) {
            if (Math.abs(y) >= 0.25) {
                y = -y * 1.2;
            } else {
                y = 0;
            }
            entity.setVelocity(vel.x, y, vel.z);

            return true;
        }

        return false;
    }

    default boolean applyBouncyness(BlockState state, Entity entity) {
        if (getCanInteract(state, entity)) {
            Vec3d vel = entity.getVelocity();
            double y = vel.y;

            if (!entity.isSneaking() && Math.abs(y) >= 0.25) {
                y += 0.0155 * (entity.fallDistance < 1 ? 1 : entity.fallDistance);
            } else {
                y = 0;
            }
            entity.setVelocity(vel.x, y, vel.z);

            return true;
        }

        return false;
    }


    default boolean getCanInteract(BlockState state, Entity e) {
        if (getCloudMaterialType(state).canInteract(e)) {
            if (e instanceof ItemEntity) {
                // @FUF(reason = "There is no TickEvents.EntityTickEvent. Waiting on mixins...")
                e.setNoGravity(true);
            }
            return true;
        }

        return false;
    }

    default boolean isDense(BlockState blockState) {
        return getCloudMaterialType(blockState) != CloudType.NORMAL;
    }

    /**
     * Determines whether falling sand entities should fall through this block.
     * @param state Our block state
     * @param world The current world
     * @param pos   The current position
     *
     * @return True to allow blocks to pass.
     *
     * @fuf Hacked until we can get mixins to implement a proper hook
     */
    default boolean allowsFallingBlockToPass(BlockState state, BlockView world, BlockPos pos) {
        if (isDense(state)) {
            return false;
        }

        Block above = world.getBlockState(pos.up()).getBlock();
        return !(above instanceof Gas) && above instanceof FallingBlock;
    }
}
