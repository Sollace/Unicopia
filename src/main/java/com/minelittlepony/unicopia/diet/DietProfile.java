package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record DietProfile(
        float defaultMultiplier,
        float foragingMultiplier,
        List<Multiplier> multipliers,
        List<FoodGroupEffects> effects,
        Optional<FoodGroupEffects> defaultEffect
    ) {
    public static final DietProfile EMPTY = new DietProfile(1, 1, List.of(), List.of(), Optional.empty());
    public static final Codec<DietProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("default_multiplier").forGetter(DietProfile::defaultMultiplier),
                Codec.FLOAT.fieldOf("foraging_multiplier").forGetter(DietProfile::foragingMultiplier),
                Codec.list(Multiplier.CODEC).fieldOf("multipliers").forGetter(DietProfile::multipliers),
                Codec.list(FoodGroupEffects.createCodec(FoodGroupKey.CODEC)).fieldOf("effects").forGetter(DietProfile::effects),
                FoodGroupEffects.createCodec(FoodGroupKey.CODEC).optionalFieldOf("default_effect").forGetter(DietProfile::defaultEffect)
    ).apply(instance, DietProfile::new));
    public static final PacketCodec<RegistryByteBuf, DietProfile> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, DietProfile::defaultMultiplier,
            PacketCodecs.FLOAT, DietProfile::foragingMultiplier,
            Multiplier.PACKET_CODEC.collect(PacketCodecs.toList()), DietProfile::multipliers,
            FoodGroupEffects.createPacketCodec(FoodGroupKey.PACKET_CODEC).collect(PacketCodecs.toList()), DietProfile::effects,
            PacketCodecs.optional(FoodGroupEffects.createPacketCodec(FoodGroupKey.PACKET_CODEC)), DietProfile::defaultEffect,
            DietProfile::new
    );

    public void validate(Consumer<String> issues, Predicate<Identifier> foodGroupExists) {
        multipliers.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Multiplier referenced unknown food group: " + key.id());
            }
        });
        effects.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Override defined for unknown food group: " + key.id());
            }
        });
        defaultEffect.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Default override defined for unknown food group: " + key.id());
            }
        });
    }

    public Optional<Multiplier> findMultiplier(ItemStack stack) {
        return multipliers.stream().filter(m -> m.test(stack)).findFirst();
    }

    public Optional<Effect> findEffect(ItemStack stack) {
        return effects.stream().filter(m -> m.test(stack)).findFirst().or(this::defaultEffect).map(Effect.class::cast);
    }

    static boolean isForaged(ItemStack stack) {
        return stack.getComponents().get(DataComponentTypes.FOOD) == null;
    }

    @Nullable
    public FoodComponent getAdjustedFoodComponent(ItemStack stack, FoodComponent food) {
        if (this == EMPTY) {
            return food;
        }

        var ratios = getRatios(stack);
        if (isInedible(ratios)) {
            return null;
        }

        float hunger = food.nutrition() * ratios.getFirst();
        int baseline = (int)hunger;

        return new FoodComponent(
            Math.max(1, (hunger - baseline) >= 0.5F ? baseline + 1 : baseline),
            food.saturation() * ratios.getSecond(),
            food.canAlwaysEat(),
            food.eatSeconds(),
            food.usingConvertsTo(),
            food.effects()
        );
    }

    public boolean isInedible(ItemStack stack) {
        return isInedible(getRatios(stack));
    }

    public boolean isInedible(Pair<Float, Float> ratios) {
        return ratios.getFirst() <= 0.01F && ratios.getSecond() <= 0.01F;
    }

    public Pair<Float, Float> getRatios(ItemStack stack) {
        Optional<Multiplier> multiplier = findMultiplier(stack);

        float baseMultiplier = (isForaged(stack) ? foragingMultiplier() : defaultMultiplier());
        float hungerMultiplier = multiplier.map(Multiplier::hunger).orElse(baseMultiplier);
        float saturationMultiplier = multiplier.map(Multiplier::saturation).orElse(baseMultiplier);
        return Pair.of(hungerMultiplier, saturationMultiplier);
    }

    public void appendTooltip(ItemStack stack, @Nullable Pony pony, Consumer<Text> tooltip, TooltipType context) {
        if (this == EMPTY) {
            return;
        }

        var food = stack.get(DataComponentTypes.FOOD);
        var ratios = getRatios(stack);

        if (food == null || isInedible(ratios)) {
            return;
        }

        tooltip.accept(Text.translatable("unicopia.diet.information").formatted(Formatting.DARK_PURPLE));
        findEffect(stack).orElseGet(() -> PonyDiets.getInstance().getEffects(stack)).appendTooltip(stack, tooltip, context);

        float baseMultiplier = (isForaged(stack) ? foragingMultiplier() : defaultMultiplier());

        if (context.isAdvanced()) {
            var nonAdjustedFood = getNonAdjustedFoodComponent(stack, pony).orElse(food);
            tooltip.accept(Text.literal(" ").append(Text.translatable("unicopia.diet.base_multiplier", baseMultiplier).formatted(Formatting.DARK_GRAY)));
            tooltip.accept(Text.literal(" ").append(Text.translatable("unicopia.diet.hunger.detailed", food.nutrition(), nonAdjustedFood.nutrition(), (int)(ratios.getFirst() * 100))).formatted(Formatting.DARK_GRAY));
            tooltip.accept(Text.literal(" ").append(Text.translatable("unicopia.diet.saturation.detailed", food.saturation(), nonAdjustedFood.saturation(), (int)(ratios.getSecond() * 100))).formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.accept(Text.literal(" ").append(Text.translatable("unicopia.diet.hunger", (int)(ratios.getFirst() * 100))).formatted(Formatting.DARK_GRAY));
            tooltip.accept(Text.literal(" ").append(Text.translatable("unicopia.diet.saturation", (int)(ratios.getSecond() * 100))).formatted(Formatting.DARK_GRAY));
        }
    }

    private Optional<FoodComponent> getNonAdjustedFoodComponent(ItemStack stack, @Nullable Pony pony) {
        FoodComponent food = stack.getComponents().get(DataComponentTypes.FOOD);

        if (food != null) {
            return Optional.ofNullable(food);
        }

        if (pony != null && pony.getObservedSpecies().hasIronGut()) {
            return findEffect(stack)
                .flatMap(Effect::foodComponent)
                .or(() -> PonyDiets.getInstance().getEffects(stack).foodComponent());
        }

        return Optional.empty();
    }

    public record Multiplier(
            Set<FoodGroupKey> tags,
            float hunger,
            float saturation
    ) implements Predicate<ItemStack> {
        public static final Codec<Set<FoodGroupKey>> TAGS_CODEC = FoodGroupKey.CODEC.listOf().xmap(
                l -> l.stream().distinct().collect(Collectors.toSet()),
                set -> new ArrayList<>(set)
        );
        public static final Codec<Multiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TAGS_CODEC.fieldOf("tags").forGetter(Multiplier::tags),
                Codec.FLOAT.fieldOf("hunger").forGetter(Multiplier::hunger),
                Codec.FLOAT.fieldOf("saturation").forGetter(Multiplier::saturation)
        ).apply(instance, Multiplier::new));
        public static final PacketCodec<RegistryByteBuf, Multiplier> PACKET_CODEC = PacketCodec.tuple(
                FoodGroupKey.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), Multiplier::tags,
                PacketCodecs.FLOAT, Multiplier::hunger,
                PacketCodecs.FLOAT, Multiplier::saturation,
                Multiplier::new
        );

        @Override
        public boolean test(ItemStack stack) {
            return tags.stream().anyMatch(tag -> tag.contains(stack));
        }

        public static final class Builder {
            private Set<FoodGroupKey> tags = new HashSet<>();
            private float hunger = 1;
            private float saturation = 1;

            public Builder tag(Identifier tag) {
                tags.add(FoodGroupKey.LOOKUP.apply(tag));
                return this;
            }

            public Builder hunger(float hunger) {
                this.hunger = hunger;
                return this;
            }

            public Builder saturation(float saturation) {
                this.saturation = saturation;
                return this;
            }

            public Multiplier build() {
                return new Multiplier(tags, hunger, saturation);
            }
        }
    }
}
