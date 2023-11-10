package com.minelittlepony.unicopia.item.toxin;

import java.util.*;
import java.util.function.Function;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public record Toxic (
        Optional<UseAction> useAction,
        Function<LivingEntity, Optional<FoodComponent>> food,
        Ailment.Set ailment
    ) {
    public void appendTooltip(PlayerEntity player, List<Text> tooltip, TooltipContext context) {
        ailment.get(player).ifPresent(ailment -> ailment.appendTooltip(tooltip, context));
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            ailment.get(entity).ifPresent(ailment -> ailment.effect().afflict(player, stack));
        }
        return stack;
    }

    public static Toxic innert(Toxicity toxicity) {
        return new Builder(Ailment.of(toxicity, Toxin.INNERT)).build();
    }

    public static class Builder {
        private final Ailment def;
        private final Map<Race, Ailment> overrides = new HashMap<>();
        private Optional<UseAction> action = Optional.of(UseAction.EAT);
        private final Map<Race, FoodComponent> components = new HashMap<>();
        private Optional<FoodComponent> component = Optional.empty();

        public Builder(Ailment def) {
            this.def = def;
        }

        public Builder action(UseAction action) {
            this.action = Optional.of(action);
            return this;
        }

        public Builder food(FoodComponent food) {
            component = Optional.ofNullable(food);
            return this;
        }

        public Builder food(Race race, FoodComponent food) {
            components.put(race, food);
            return this;
        }

        public Builder with(Race race, Ailment ailment) {
            overrides.put(race, ailment);
            return this;
        }

        public Toxic build() {
            return new Toxic(action, entity -> {
                if (entity instanceof PlayerEntity player) {
                    return Optional.ofNullable(components.get(Pony.of(player).getObservedSpecies())).or(() -> component);
                }
                return component;
            }, Ailment.Set.of(def, overrides));
        }

        public Optional<Toxic> buildOptional() {
            return Optional.of(build());
        }
    }
}
