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
import com.minelittlepony.unicopia.item.ItemDuck;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

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
                Codec.list(FoodGroupEffects.CODEC).fieldOf("effects").forGetter(DietProfile::effects),
                FoodGroupEffects.CODEC.optionalFieldOf("default_effect").forGetter(DietProfile::defaultEffect)
    ).apply(instance, DietProfile::new));

    public DietProfile(PacketByteBuf buffer) {
        this(buffer.readFloat(), buffer.readFloat(),
                buffer.readList(Multiplier::new),
                buffer.readList(b -> new FoodGroupEffects(b, FoodGroupKey.LOOKUP)),
                buffer.readOptional(b -> new FoodGroupEffects(b, FoodGroupKey.LOOKUP))
        );
    }

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

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(defaultMultiplier);
        buffer.writeFloat(foragingMultiplier);
        buffer.writeCollection(multipliers, (b, t) -> t.toBuffer(b));
        buffer.writeCollection(effects, (b, t) -> t.toBuffer(b));
        buffer.writeOptional(defaultEffect, (b, t) -> t.toBuffer(b));
    }

    public Optional<Multiplier> findMultiplier(ItemStack stack) {
        return multipliers.stream().filter(m -> m.test(stack)).findFirst();
    }

    public Optional<Effect> findEffect(ItemStack stack) {
        return effects.stream().filter(m -> m.test(stack)).findFirst().or(this::defaultEffect).map(Effect.class::cast);
    }

    static boolean isForaged(ItemStack stack) {
        return ((ItemDuck)stack.getItem()).getOriginalFoodComponent().isEmpty();
    }

    @Nullable
    public FoodComponent getAdjustedFoodComponent(ItemStack stack) {
        var food = stack.getItem().getFoodComponent();
        if (this == EMPTY) {
            return food;
        }

        var ratios = getRatios(stack);
        if (isInedible(ratios)) {
            return null;
        }

        float hunger = food.getHunger() * ratios.getFirst();
        int baseline = (int)hunger;

        return FoodAttributes.copy(food)
            .hunger(Math.max(1, (hunger - baseline) >= 0.5F ? baseline + 1 : baseline))
            .saturationModifier(food.getSaturationModifier() * ratios.getSecond())
            .build();
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

    public void appendTooltip(ItemStack stack, @Nullable PlayerEntity user, List<Text> tooltip, TooltipContext context) {
        var food = stack.getItem().getFoodComponent();

        var ratios = getRatios(stack);
        if (food == null || isInedible(ratios)) {
            if (stack.getUseAction() != UseAction.DRINK) {
                tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.not_edible")).formatted(Formatting.DARK_GRAY));
            }
            return;
        }

        float baseMultiplier = (isForaged(stack) ? foragingMultiplier() : defaultMultiplier());

        if (context.isAdvanced()) {
            var nonAdjustedFood = getNonAdjustedFoodComponent(stack, user).orElse(food);
            tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.base_multiplier", baseMultiplier).formatted(Formatting.DARK_GRAY)));
            tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.hunger.detailed", food.getHunger(), nonAdjustedFood.getHunger(), (int)(ratios.getFirst() * 100))).formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.saturation.detailed", food.getSaturationModifier(), nonAdjustedFood.getSaturationModifier(), (int)(ratios.getSecond() * 100))).formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.hunger", (int)(ratios.getFirst() * 100))).formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal(" ").append(Text.translatable("unicopia.diet.saturation", (int)(ratios.getSecond() * 100))).formatted(Formatting.DARK_GRAY));
        }
    }

    private Optional<FoodComponent> getNonAdjustedFoodComponent(ItemStack stack, @Nullable PlayerEntity user) {
        @Nullable
        Pony pony = Pony.of(user);
        Optional<FoodComponent> food = ((ItemDuck)stack.getItem()).getOriginalFoodComponent();

        if (food.isEmpty() && pony.getObservedSpecies().hasIronGut()) {
            return findEffect(stack)
                .flatMap(Effect::foodComponent)
                .or(() -> PonyDiets.getInstance().getEffects(stack).foodComponent());
        }

        return food;
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

        public Multiplier(PacketByteBuf buffer) {
            this(buffer.readCollection(HashSet::new, p -> FoodGroupKey.LOOKUP.apply(p.readIdentifier())), buffer.readFloat(), buffer.readFloat());
        }

        @Override
        public boolean test(ItemStack stack) {
            return tags.stream().anyMatch(tag -> tag.contains(stack));
        }

        public void toBuffer(PacketByteBuf buffer) {
            buffer.writeCollection(tags, (p, t) -> p.writeIdentifier(t.id()));
            buffer.writeFloat(hunger);
            buffer.writeFloat(saturation);
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
