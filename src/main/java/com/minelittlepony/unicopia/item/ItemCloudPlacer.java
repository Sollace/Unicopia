package com.minelittlepony.unicopia.item;

import java.util.function.Function;

import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.init.UItems;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemCloudPlacer extends Item implements IDispensable {

    private final Function<World, EntityCloud> cloudSupplier;

    public ItemCloudPlacer(Function<World, EntityCloud> cloudSupplier, String domain, String name) {
        super();
        setTranslationKey(name);
        setRegistryName(domain, name);
        setCreativeTab(CreativeTabs.MATERIALS);

        maxStackSize = 16;

        this.cloudSupplier = cloudSupplier;

        setDispenseable();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this == UItems.racing_cloud_spawner && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            items.add(new ItemStack(UItems.construction_cloud_spawner));
            items.add(new ItemStack(UItems.wild_cloud_spawner));
        }
    }

    public void placeCloud(World world, BlockPos pos) {
        EntityCloud cloud = cloudSupplier.apply(world);
        cloud.moveToBlockPosAndAngles(pos, 0, 0);
        world.spawnEntity(cloud);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            RayTraceResult mop = rayTrace(world, player, true);

            BlockPos pos;

            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                pos = mop.getBlockPos().offset(mop.sideHit);
            } else {
                pos = player.getPosition();
            }

            placeCloud(world, pos);

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ActionResult<ItemStack> dispenseStack(IBlockSource source, ItemStack stack) {
        IPosition pos = BlockDispenser.getDispensePosition(source);

        placeCloud(source.getWorld(), new BlockPos(pos.getX(), pos.getY(), pos.getZ()));

        stack.shrink(1);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
