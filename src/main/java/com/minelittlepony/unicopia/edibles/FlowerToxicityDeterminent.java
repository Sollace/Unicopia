package com.minelittlepony.unicopia.edibles;

import net.minecraft.block.BlockFlower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import static net.minecraft.block.BlockFlower.EnumFlowerType.*;

public class FlowerToxicityDeterminent implements IEdible {

    private final BlockFlower.EnumFlowerColor color;

    public FlowerToxicityDeterminent(BlockFlower.EnumFlowerColor color) {
        this.color = color;
    }

    BlockFlower.EnumFlowerType getType(ItemStack stack) {
        return BlockFlower.EnumFlowerType.getType(color, stack.getMetadata());
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        switch (getType(stack)) {
            case DANDELION:
            case PINK_TULIP:
            case RED_TULIP:
            case ORANGE_TULIP:
            case HOUSTONIA: return Toxicity.SAFE;
            case OXEYE_DAISY:
            case POPPY: return Toxicity.SEVERE;
            case BLUE_ORCHID:
            case WHITE_TULIP:
            case ALLIUM: return Toxicity.FAIR;
            default: return Toxicity.SAFE;
        }
    }

    @Override
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        BlockFlower.EnumFlowerType type = getType(stack);

        if (type == HOUSTONIA && player.world.rand.nextInt(30) == 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 10, 1, false, false));
        }

        if (type == OXEYE_DAISY) {
            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 30, 1, false, false));
        }
    }
}
