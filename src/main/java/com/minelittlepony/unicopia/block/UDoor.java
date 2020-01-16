package com.minelittlepony.unicopia.block;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.Material;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class UDoor extends DoorBlock {

    private final Supplier<Item> theItem;

    protected UDoor(Material material, String domain, String name, Supplier<Item> theItem) {
        super(material);
        disableStats();
        setTranslationKey(name);
        setRegistryName(domain, name);

        this.theItem = theItem;
    }

    @Override
    public Block setSoundType(SoundType sound) {
        return super.setSoundType(sound);
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.AIR : getItem();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, BlockState state) {
        return new ItemStack(getItem());
    }

    protected Item getItem() {
        return theItem.get();
    }

    protected WorldEvent getCloseSound() {
        return isLockable() ? WorldEvent.IRON_DOOR_SLAM : WorldEvent.WOODEN_DOOR_SLAM;
    }

    protected WorldEvent getOpenSound() {
        return isLockable() ? WorldEvent.IRON_DOOR_OPEN : WorldEvent.WOODEN_DOOR_OPEN;
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return toggleDoor(world, pos, false, true, player);
    }

    @Override
    public void setOpen(World world, BlockPos pos, boolean open) {
        toggleDoor(world, pos, open, false, null);
    }

    protected boolean isLockable() {
        return material == Material.METAL;
    }

    protected boolean canBePowered() {
        return true;
    }

    protected boolean canOpen(@Nullable PlayerEntity player) {
        return player == null || material != Material.METAL;
    }

    protected boolean toggleDoor(World world, BlockPos pos, boolean open, boolean force, @Nullable PlayerEntity player) {
        if (!canOpen(player)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != this) {
            return false;
        }

        BlockPos lower = getPrimaryDoorPos(state, pos);

        BlockState mainDoor = pos == lower ? state : world.getBlockState(lower);

        if (mainDoor.getBlock() != this) {
            return false;
        }

        if (!force && mainDoor.get(OPEN) == open) {
            return false;
        }

        state = mainDoor.cycle(OPEN);

        world.setBlockState(lower, state, 10);

        world.markBlockRangeForRenderUpdate(lower, pos);

        WorldEvent sound = state.getValue(OPEN) ? getOpenSound() : getCloseSound();

        world.playEvent(player, sound.getId(), pos, 0);

        return true;
    }

    protected BlockPos getPrimaryDoorPos(BlockState state, BlockPos pos) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sender, BlockPos fromPos, boolean v) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lower = pos.down();

            BlockState lowerDoor = world.getBlockState(lower);

            // pop us off if we don't have a lower door
            if (lowerDoor.getBlock() != this) {
                world.breakBlock(pos, true);
            } else if (sender != this) {
                lowerDoor.neighborUpdate(world, lower, sender, fromPos, v);
            }

            return;
        }

        boolean destroyed = false;

        BlockPos upper = pos.up();
        BlockState upperDoor = world.getBlockState(upper);

        // pop us off if we don't have an upper door
        if (upperDoor.getBlock() != this) {
            world.breakBlock(pos, true);
            destroyed = true;
        }

        // pop us off if we don't have support
        if (!world.getBlockState(pos.down()).hasSolidTopSurface(world,  pos.down(), null)) {
            world.breakBlock(pos, true);

            destroyed = true;

            if (upperDoor.getBlock() == this) {
                world.breakBlock(upper, true);
            }
        }

        if (destroyed) {
            if (!world.isClient) {
                dropBlockAsItem(world, pos, state, 0);
            }
        } else if (canBePowered()) {
            boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(upper);

            if (sender != this && (powered || sender.getDefaultState().emitsRedstonePower()) && powered != upperDoor.get(POWERED)) {
                world.setBlockState(upper, upperDoor.with(POWERED, powered), 2);

                if (onPowerStateChanged(world, state, pos, powered)) {
                    world.markBlockRangeForRenderUpdate(pos, upper);

                    WorldEvent sound = powered ? getOpenSound() : getCloseSound();

                    world.playGlobalEvent(null, sound.getId(), pos, 0);
                }
            }
        }
    }

    /**
     * Called by the lower block when the powered state changes.
     */
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        if (powered != state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, powered), 2);

            return true;
        }

        return false;
    }
}
