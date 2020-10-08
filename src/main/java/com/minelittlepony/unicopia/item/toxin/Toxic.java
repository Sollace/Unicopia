package com.minelittlepony.unicopia.item.toxin;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class Toxic {
    private final UseAction action;

    private final Ailment lowerBound;
    private final Ailment upperBound;

    private final FoodType type;

    Toxic(UseAction action, FoodType type, Ailment lowerBound, Ailment upperBound) {
        this.action = action;
        this.type = type;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public UseAction getUseAction(ItemStack stack) {
        return action;
    }

    @Environment(EnvType.CLIENT)
    public Text getTooltip(ItemStack stack) {
        Pony pony = Pony.of(MinecraftClient.getInstance().player);
        if (pony != null && !pony.getSpecies().canConsume(type)) {
            return upperBound.getToxicity().getTooltip();
        }
        return lowerBound.getToxicity().getTooltip();
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            Race race = Pony.of((PlayerEntity)entity).getSpecies();

            (race.canConsume(type) ? lowerBound : upperBound).afflict((PlayerEntity)entity, type, stack);
        }

        return stack;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand, Supplier<TypedActionResult<ItemStack>> sup) {

        if (!Pony.of(player).getSpecies().hasIronGut()) {
            return TypedActionResult.fail(player.getStackInHand(hand));
        }

        return sup.get();
    }
}
