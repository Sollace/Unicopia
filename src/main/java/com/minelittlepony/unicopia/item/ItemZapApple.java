package com.minelittlepony.unicopia.item;

import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemZapApple extends ItemApple {

    public ItemZapApple(String domain, String name) {
        super(domain, name);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        RayTraceResult mop = VecHelper.getObjectMouseOver(player, 5, 0);

        if (mop != null && mop.typeOfHit == RayTraceResult.Type.ENTITY) {
            ItemStack stack = player.getHeldItem(hand);

            if (canFeedTo(stack, mop.entityHit)) {
                return onFedTo(stack, player, mop.entityHit);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World w, EntityPlayer player) {
        super.onFoodEaten(stack, w, player);

        player.attackEntityFrom(MagicalDamageSource.create("zap"), 120);

        w.addWeatherEffect(new EntityLightningBolt(w, player.posX, player.posY, player.posZ, false));
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof EntityVillager
                || e instanceof EntityCreeper
                || e instanceof EntityPig;
    }

    public ActionResult<ItemStack> onFedTo(ItemStack stack, EntityPlayer player, Entity e) {
        e.onStruckByLightning(new EntityLightningBolt(e.world, e.posX, e.posY, e.posZ, false));

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        int meta = stack.getMetadata();

        if (meta == 0) {
            return EnumRarity.EPIC;
        }

        return EnumRarity.RARE;
    }
}
