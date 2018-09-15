package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.CloudSize;
import com.minelittlepony.unicopia.entity.EntityCloud;

import net.minecraft.creativetab.CreativeTabs;
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

public class ItemCloud extends Item {

	public ItemCloud(String domain, String name) {
		super();
		setHasSubtypes(true);
		setMaxDamage(0);
		setTranslationKey(name);
		setRegistryName(domain, name);
        setCreativeTab(CreativeTabs.MATERIALS);

        maxStackSize = 16;
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

            EntityCloud cloud = CloudSize.byMetadata(stack.getItemDamage()).createEntity(world);
    		cloud.moveToBlockPosAndAngles(pos, 0, 0);
	    	world.spawnEntity(cloud);

	    	if (!player.capabilities.isCreativeMode) {
	    		stack.shrink(1);
	    	}
    	}

    	return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
		return super.getTranslationKey(stack) + "." + CloudSize.byMetadata(stack.getItemDamage()).getName();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subs) {
        if (isInCreativeTab(tab)) {
            for (CloudSize i : CloudSize.values()) {
            	subs.add(new ItemStack(this, 1, i.getMetadata()));
            }
        }
    }
}
