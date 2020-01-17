package com.minelittlepony.unicopia.item.consumables;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.StatusEffectInstance;

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
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        BlockDoublePlant.EnumPlantType type = getType(stack);

        if ((type == ROSE || type == FERN)
                && player.world.rand.nextInt(30) == 0) {
            player.addStatusEffectInstance(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1, false, false));
        }

        if (type == GRASS) {
            player.addStatusEffectInstance(new StatusEffectInstance(StatusEffects.NAUSEA, 30, 1, false, false));
        }

        if (type == FERN) {
            player.addStatusEffectInstance(new StatusEffectInstance(StatusEffects.STRENGTH, 30, 1, false, false));
        }
    }
}
