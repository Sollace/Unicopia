package com.minelittlepony.unicopia.item.toxin;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class Ailment implements Affliction {
    public static final Ailment INNERT = of(Toxicity.SAFE, Toxin.INNERT);

    private final Toxicity toxicity;
    private final Toxin effect;

    Ailment(Toxicity toxicity, Toxin effect) {
        this.toxicity = toxicity;
        this.effect = effect;
    }

    public Toxicity getToxicity() {
        return toxicity;
    }

    public void appendTooltip(List<Text> tooltip, TooltipContext context) {
        tooltip.add(getToxicity().getTooltip());
        if (context.isAdvanced()) {
            effect.appendTooltip(tooltip);
        }
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        effect.afflict(player, stack);
    }

    public static Ailment of(Toxicity toxicity, Toxin effect) {
        return new Ailment(toxicity, effect);
    }
}
