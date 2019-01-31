package com.minelittlepony.unicopia.edibles;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemEdible extends ItemFood implements IEdible {

    private final IEdible toxicityDeterminant;

    public ItemEdible(@Nonnull IEdible mapper) {
        super(1, 0, false);

        toxicityDeterminant = mapper;
    }

    public ItemEdible(String domain, String name, int amount, int saturation, @Nonnull IEdible mapper) {
        super(amount, saturation, false);

        setTranslationKey(name);
        setRegistryName(domain, name);

        toxicityDeterminant = mapper;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        Race race = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();
        Toxicity toxicity = (race.isDefault() || race == Race.CHANGELING) ? Toxicity.LETHAL : getToxicityLevel(stack);

        addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Toxicity toxicity = getToxicityLevel(stack);

        setAlwaysEdible();

        TextFormatting color = toxicity.toxicWhenCooked() ? TextFormatting.RED : toxicity.toxicWhenRaw() ? TextFormatting.DARK_PURPLE : TextFormatting.GRAY;

        tooltip.add(color + I18n.format(toxicity.getTranslationKey()));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            entityplayer.getFoodStats().addStats(this, stack);

            worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);

            onFoodEaten(stack, worldIn, entityplayer);

            // replaced "this" with "stack.getItem()"
            entityplayer.addStat(StatList.getObjectUseStats(stack.getItem()));

            if (entityplayer instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP)entityplayer, stack);
            }
        }

        ItemStack container = getContainerItem(stack);

        if (!container.isEmpty()) {
            return container;
        }

        stack.shrink(1);

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
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 7));
            player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 300, 7));
        } else if (toxicity.toxicWhenCooked()) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3, 1));
        }

        toxicityDeterminant.addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return toxicityDeterminant.getToxicityLevel(stack);
    }
}
