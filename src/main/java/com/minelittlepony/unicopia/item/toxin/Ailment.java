package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Ailment {

    public static final Ailment INNERT = new Ailment(Toxicity.SAFE, Toxin.INNERT);

    private final Toxicity toxicity;
    private final Toxin toxin;

    public Ailment(Toxicity toxicity, Toxin toxin) {
        this.toxicity = toxicity;
        this.toxin = toxin;
    }

    public Toxicity getToxicity() {
        return toxicity;
    }

    public void afflict(PlayerEntity player, FoodType type, ItemStack stack) {
        this.toxin.afflict(player, type, toxicity, stack);
    }

    public static Ailment of(Toxicity toxicity) {
        return new Ailment(toxicity, Toxin.FOOD);
    }
}
