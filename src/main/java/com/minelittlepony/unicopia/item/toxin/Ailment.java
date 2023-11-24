package com.minelittlepony.unicopia.item.toxin;

import java.util.*;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public record Ailment (
        Toxicity toxicity,
        Toxin effect
    ) {
    public static final Ailment INNERT = of(Toxicity.SAFE, Toxin.INNERT);

    @Deprecated
    public static Ailment of(Toxicity toxicity, Toxin effect) {
        return new Ailment(toxicity, effect);
    }

    public void appendTooltip(List<Text> tooltip, TooltipContext context) {
        tooltip.add(toxicity().getTooltip());
        if (context.isAdvanced()) {
            effect().appendTooltip(tooltip);
        }
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!Pony.of(player).getObservedSpecies().hasIronGut()) {
            return TypedActionResult.fail(player.getStackInHand(hand));
        }
        return null;
    }

    public interface Set {
        Set EMPTY = e -> Optional.empty();

        Optional<Ailment> get(LivingEntity entity);

        static Ailment.Set of(Ailment def, Map<Race, Ailment> map) {
            if (map.isEmpty()) {
                return of(def);
            }
            return entity -> Optional.of(entity instanceof PlayerEntity player ? map.getOrDefault(Pony.of(player).getObservedSpecies(), def) : def);
        }

        static Ailment.Set of(Ailment ailment) {
            final Optional<Ailment> value = Optional.of(ailment);
            return entity -> value;
        }
    }
}
