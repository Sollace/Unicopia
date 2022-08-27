package com.minelittlepony.unicopia.item.toxin;

import java.util.*;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class Toxic {
    private final UseAction action;

    private final Optional<FoodComponent> component;

    private final Ailment defaultAilment;
    private final Map<Race, Ailment> ailments;

    private final TagKey<Item> tag;

    Toxic(UseAction action, Optional<FoodComponent> component, TagKey<Item> tag, Ailment defaultAilment, Map<Race, Ailment> ailments) {
        this.action = action;
        this.component = component;
        this.tag = tag;
        this.defaultAilment = defaultAilment;
        this.ailments = ailments;
    }

    @SuppressWarnings("deprecation")
    public boolean matches(Item item) {
        return item.getRegistryEntry().isIn(tag);
    }

    public Optional<FoodComponent> getFoodComponent() {
        return component;
    }

    public UseAction getUseAction(ItemStack stack) {
        return action;
    }

    public Ailment getAilmentFor(PlayerEntity player) {
        if (player == null) {
            return defaultAilment;
        }
        return ailments.getOrDefault(Pony.of(player).getSpecies(), defaultAilment);
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            getAilmentFor((PlayerEntity)entity).afflict((PlayerEntity)entity, stack);
        }

        return stack;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!Pony.of(player).getSpecies().hasIronGut()) {
            return TypedActionResult.fail(player.getStackInHand(hand));
        }
        return null;
    }

    public static class Builder {
        private final Ailment def;
        private final Map<Race, Ailment> ailments = new HashMap<>();
        private UseAction action = UseAction.EAT;
        private Optional<FoodComponent> component = Optional.empty();

        public Builder(Ailment def) {
            this.def = def;
        }

        public Builder action(UseAction action) {
            this.action = action;
            return this;
        }

        public Builder food(FoodComponent food) {
            component = Optional.ofNullable(food);
            return this;
        }

        public Builder with(Race race, Ailment ailment) {
            ailments.put(race, ailment);
            return this;
        }

        public Toxic build(String name) {
            return new Toxic(action, component, UTags.item(name), def, ailments);
        }
    }
}
