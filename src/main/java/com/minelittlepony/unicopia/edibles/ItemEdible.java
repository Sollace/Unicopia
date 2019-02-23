package com.minelittlepony.unicopia.edibles;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UEffects;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public abstract class ItemEdible extends ItemFood implements IEdible {

    private EnumAction useAction = EnumAction.EAT;

    public ItemEdible(int amount, int saturation, boolean isMeat) {
        super(amount, saturation, isMeat);
    }

    public ItemEdible(String domain, String name, int amount, int saturation, boolean isMeat) {
        super(amount, saturation, isMeat);

        setTranslationKey(name);
        setRegistryName(domain, name);
    }

    public Item setUseAction(EnumAction action) {
        useAction = action;

        return this;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return useAction;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        Race race = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();
        Toxicity toxicity = (race.isDefault() || race == Race.CHANGELING) ? Toxicity.LETHAL : getToxicityLevel(stack);

        addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(getToxicityLevel(stack).getTooltip());
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {

        EntityPlayer entityplayer = entityLiving instanceof EntityPlayer ? (EntityPlayer)entityLiving : null;

        if (entityplayer != null) {
            entityplayer.getFoodStats().addStats(this, stack);

            worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);

            onFoodEaten(stack, worldIn, entityplayer);

            // replaced "this" with "stack.getItem()"
            entityplayer.addStat(StatList.getObjectUseStats(stack.getItem()));

            if (entityplayer instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP)entityplayer, stack);
            }
        }

        if (entityplayer == null || !entityplayer.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        ItemStack container = getContainerItem(stack);

        if (!container.isEmpty() && entityplayer != null && !entityplayer.capabilities.isCreativeMode) {
            if (stack.isEmpty()) {
                return getContainerItem(stack);
            }

            entityplayer.inventory.addItemStackToInventory(getContainerItem(stack));
        }

        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        Race race = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();

        if (race.isDefault() || race == Race.CHANGELING) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, player.getHeldItem(hand));
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addSecondaryEffects(EntityPlayer player, Toxicity toxicity, ItemStack stack) {

        if (toxicity.toxicWhenRaw()) {
            player.addPotionEffect(toxicity.getPoisonEffect());
        }

        if (toxicity.isLethal()) {
            player.addPotionEffect(new PotionEffect(UEffects.FOOD_POISONING, 300, 7, false, false));
        } else if (toxicity.toxicWhenCooked()) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3, 1, false, false));
        }
    }
}
