package com.minelittlepony.unicopia.edibles;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import static net.minecraft.block.BlockDoublePlant.EnumPlantType.*;

public class BushToxicityDeterminent implements IEdible {

    BlockDoublePlant.EnumPlantType getType(ItemStack stack) {
        return byMetadata(stack.getMetadata());
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        switch (getType(stack)) {
            case SUNFLOWER:
            case GRASS: return Toxicity.SAFE;
            case PAEONIA:
            case SYRINGA: return Toxicity.FAIR;
            case FERN:
            case ROSE: return Toxicity.SEVERE;
            default: return Toxicity.SAFE;
        }
    }

    @Override
    public void addSecondaryEffects(EntityPlayer player, Toxicity toxicity, ItemStack stack) {
        BlockDoublePlant.EnumPlantType type = getType(stack);

        if ((type == ROSE || type == FERN)
                && player.world.rand.nextInt(30) == 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 1));
        }

        if (type == GRASS) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30, 1));
        }

        if (type == FERN) {
            player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30, 1));
        }
    }
}
