package com.minelittlepony.unicopia.block;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class UDoor extends BlockDoor {

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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.AIR : getItem();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return toggleDoor(world, pos, false, true, player);
    }

    @Override
    public void toggleDoor(World world, BlockPos pos, boolean open) {
        toggleDoor(world, pos, open, false, null);
    }

    protected boolean isLockable() {
        return material == Material.IRON;
    }

    protected boolean canBePowered() {
        return true;
    }

    protected boolean canOpen(@Nullable EntityPlayer player) {
        return player == null || material != Material.IRON;
    }

    protected boolean toggleDoor(World world, BlockPos pos, boolean open, boolean force, @Nullable EntityPlayer player) {
        if (!canOpen(player)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() != this) {
            return false;
        }

        BlockPos lower = getPrimaryDoorPos(state, pos);

        IBlockState mainDoor = pos == lower ? state : world.getBlockState(lower);

        if (mainDoor.getBlock() != this) {
            return false;
        }

        if (!force && mainDoor.getValue(OPEN) == open) {
            return false;
        }

        state = mainDoor.cycleProperty(OPEN);

        world.setBlockState(lower, state, 10);

        world.markBlockRangeForRenderUpdate(lower, pos);

        WorldEvent sound = state.getValue(OPEN) ? getOpenSound() : getCloseSound();

        world.playEvent(player, sound.getId(), pos, 0);

        return true;
    }

    protected BlockPos getPrimaryDoorPos(IBlockState state, BlockPos pos) {
        return state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block sender, BlockPos fromPos) {
        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            BlockPos lower = pos.down();

            IBlockState lowerDoor = world.getBlockState(lower);

            // pop us off if we don't have a lower door
            if (lowerDoor.getBlock() != this) {
                world.setBlockToAir(pos);
            } else if (sender != this) {
                lowerDoor.neighborChanged(world, lower, sender, fromPos);
            }

            return;
        }

        boolean destroyed = false;

        BlockPos upper = pos.up();
        IBlockState upperDoor = world.getBlockState(upper);

        // pop us off if we don't have an upper door
        if (upperDoor.getBlock() != this) {
            world.setBlockToAir(pos);
            destroyed = true;
        }

        // pop us off if we don't have support
        if (!world.getBlockState(pos.down()).isSideSolid(world,  pos.down(), EnumFacing.UP)) {
            world.setBlockToAir(pos);

            destroyed = true;

            if (upperDoor.getBlock() == this) {
                world.setBlockToAir(upper);
            }
        }

        if (destroyed) {
            if (!world.isRemote) {
                dropBlockAsItem(world, pos, state, 0);
            }
        } else if (canBePowered()) {
            boolean powered = world.isBlockPowered(pos) || world.isBlockPowered(upper);

            if (sender != this && (powered || sender.getDefaultState().canProvidePower()) && powered != upperDoor.getValue(POWERED)) {
                world.setBlockState(upper, upperDoor.withProperty(POWERED, powered), 2);

                if (onPowerStateChanged(world, state, pos, powered)) {
                    world.markBlockRangeForRenderUpdate(pos, upper);

                    WorldEvent sound = powered ? getOpenSound() : getCloseSound();

                    world.playEvent(null, sound.getId(), pos, 0);
                }
            }
        }
    }

    /**
     * Called by the lower block when the powered state changes.
     */
    protected boolean onPowerStateChanged(World world, IBlockState state, BlockPos pos, boolean powered) {
        if (powered != state.getValue(OPEN)) {
            world.setBlockState(pos, state.withProperty(OPEN, powered), 2);

            return true;
        }

        return false;
    }
}
