package com.minelittlepony.unicopia.item;


import com.minelittlepony.unicopia.edibles.Toxicity;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.World;

public class ItemZapApple extends ItemAppleMultiType {

    public ItemZapApple(String domain, String name) {
        super(domain, name);
        setAlwaysEdible();
    }

    @Override
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, EnumHand hand) {
        RayTraceResult mop = VecHelper.getObjectMouseOver(player, 5, 0);

        if (mop != null && mop.typeOfHit == RayTraceResult.Type.ENTITY) {
            ItemStack stack = player.getStackInHand(hand);

            if (canFeedTo(stack, mop.entityHit)) {
                return onFedTo(stack, player, mop.entityHit);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World w, PlayerEntity player) {
        super.onFoodEaten(stack, w, player);

        player.attackEntityFrom(MagicalDamageSource.create("zap"), 120);

        w.addWeatherEffect(new EntityLightningBolt(w, player.posX, player.posY, player.posZ, false));
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof EntityVillager
                || e instanceof EntityCreeper
                || e instanceof EntityPig;
    }

    public TypedActionResult<ItemStack> onFedTo(ItemStack stack, PlayerEntity player, Entity e) {
        e.onStruckByLightning(new LightningEntity(e.world, e.posX, e.posY, e.posZ, false));

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return new TypedActionResult<ItemStack>(ActionResult.SUCCESS, stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, DefaultedList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
        }
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return stack.getMetadata() == 0 ? Toxicity.SEVERE : Toxicity.SAFE;
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
