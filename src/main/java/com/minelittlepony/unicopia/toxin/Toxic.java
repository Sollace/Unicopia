package com.minelittlepony.unicopia.toxin;

import java.util.function.Function;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.equine.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class Toxic {

    private final Item item;

    private final UseAction action;
    private final Function<ItemStack, Toxicity> toxicity;
    private final Toxin toxin;

    Toxic(Item item, UseAction action, Toxin toxin, Toxicity toxicity) {
        this(item, action, toxin, stack -> toxicity);
    }

    Toxic(Item item, UseAction action, Toxin toxin, Function<ItemStack, Toxicity> toxicity) {
        this.item = item;
        this.action = action;
        this.toxin = toxin;
        this.toxicity = toxicity;
    }

    public UseAction getUseAction(ItemStack stack) {
        return action;
    }

    public Text getTooltip(ItemStack stack) {
        return toxicity.apply(stack).getTooltip();
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            Race race = Pony.of((PlayerEntity)entity).getSpecies();
            Toxicity t = race.hasIronGut() ? toxicity.apply(stack) : Toxicity.LETHAL;

            toxin.afflict((PlayerEntity)entity, t, stack);
        }

        return new ItemStack(item.getRecipeRemainder());
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand, Supplier<TypedActionResult<ItemStack>> sup) {
        Race race = Pony.of(player).getSpecies();

        if (race.hasIronGut()) {
            return sup.get();
        }

        return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
    }
}
