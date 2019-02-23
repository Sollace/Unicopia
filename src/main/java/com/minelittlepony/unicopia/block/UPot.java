package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.util.LenientState;

import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class UPot extends BlockFlowerPot {

    public static final PropertyEnum<Plant> PLANT = PropertyEnum.create("plant", Plant.class);

    public UPot(String domain, String name) {
        setTranslationKey(name);
        setRegistryName(domain, name);
        setHardness(0);
        setSoundType(SoundType.STONE);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);
        TileEntityFlowerPot tile = getTileEntity(world, pos);

        if (tile == null) {
            return false;
        }

        ItemStack current = tile.getFlowerItemStack();
        IBlockState newState = state;

        if (current.isEmpty()) {
            Plant contents = getContentType(itemstack);

            if (contents.isEmpty()) {
                return false;
            }

            tile.setItemStack(itemstack);

            if (world.getBlockState(pos).getBlock() != this) {
                newState = getDefaultState().withProperty(PLANT, contents);

                world.setBlockState(pos, newState, 0);
                tile = getTileEntity(world, pos);
                tile.setItemStack(itemstack);
            }

            player.addStat(StatList.FLOWER_POTTED);

            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
        } else {
            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, current);
            } else if (!player.addItemStackToInventory(current)) {
                player.dropItem(current, false);
            }

            tile.setItemStack(ItemStack.EMPTY);
            world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState(), 0);
        }

        tile.markDirty();
        world.notifyBlockUpdate(pos, state, newState, 3);

        return true;
    }

    @Nullable
    protected TileEntityFlowerPot getTileEntity(IBlockAccess world, BlockPos pos) {
        TileEntity tileentity = world.getTileEntity(pos);

        return tileentity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)tileentity : null;
    }

    protected Plant getContentType(ItemStack stack) {
        for (Plant i : Plant.values()) {
            if (i.matches(stack)) {
                return i;
            }
        }

        return Plant.EMPTY;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFlowerPot(Items.AIR, 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new LenientState(this, PLANT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityFlowerPot tile = getTileEntity(world, pos);

        return state.withProperty(PLANT, tile == null ? Plant.EMPTY : getContentType(tile.getFlowerItemStack()));
    }

    enum Plant implements IStringSerializable {
        EMPTY("minecraft:air", -1),
        QUILL("minecraft:feather", -1),
        MEADOWBROOK("unicopia:staff_meadow_brook", -1);

        private final int damage;
        private final ResourceLocation item;

        Plant(String item, int damage) {
            this.damage = damage;
            this.item = new ResourceLocation(item);
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        public boolean matches(ItemStack stack) {
            if (damage >= 0 && stack.getItemDamage() != damage) {
                return false;
            }

            return !stack.isEmpty() && stack.getItem().getRegistryName().equals(item);
        }

        public boolean isEmpty() {
            return this == EMPTY;
        }

    }
}
