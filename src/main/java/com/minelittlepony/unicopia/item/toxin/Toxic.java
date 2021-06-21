package com.minelittlepony.unicopia.item.toxin;

import java.util.Optional;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class Toxic {
    private final UseAction action;

    private final Ailment lowerBound;
    private final Ailment upperBound;

    private final FoodType type;

    private final Optional<FoodComponent> component;

    private final Tag<Item> tag;

    Toxic(UseAction action, FoodType type, Optional<FoodComponent> component, Tag<Item> tag, Ailment lowerBound, Ailment upperBound) {
        this.action = action;
        this.type = type;
        this.component = component;
        this.tag = tag;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean matches(Item item) {
        return tag.contains(item);
    }

    public Optional<FoodComponent> getFoodComponent() {
        return component;
    }

    public UseAction getUseAction(ItemStack stack) {
        return action;
    }

    public Ailment getAilmentFor(PlayerEntity player) {
        Pony pony = Pony.of(player);
        if (pony != null && !pony.getSpecies().canConsume(type)) {
            return upperBound;
        }
        return lowerBound;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            getAilmentFor((PlayerEntity)entity).afflict((PlayerEntity)entity, type, stack);
        }

        return stack;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!Pony.of(player).getSpecies().hasIronGut()) {
            return TypedActionResult.fail(player.getStackInHand(hand));
        }
        return null;
    }
}
