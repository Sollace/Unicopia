package com.minelittlepony.unicopia.item.toxin;

import java.util.List;

import com.minelittlepony.unicopia.entity.effect.UEffects;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;

public interface Toxin extends Affliction {
    Predicate IF_NOT_PEACEFUL = Predicate.of(Text.of("when not in peaceful "), (player, stack) -> player.getWorld().getDifficulty() != Difficulty.PEACEFUL);

    Toxin INNERT = of(Text.of("No Effect"), (player, stack) -> {});

    Toxin PRICKLING = of(StatusEffects.INSTANT_DAMAGE, 1, 0);
    Toxin RADIOACTIVITY = of(StatusEffects.GLOWING, 15, 0);

    Toxin WEAKNESS = of(StatusEffects.WEAKNESS, 200, 1);

    Toxin WEAK_NAUSEA = of(StatusEffects.NAUSEA, 17, 0);
    Toxin NAUSEA = of(StatusEffects.NAUSEA, 20, 1);
    Toxin STRONG_NAUSEA = of(StatusEffects.NAUSEA, 30, 1);

    Toxin STRENGTH = of(StatusEffects.STRENGTH, 30, 0);
    Toxin BLINDNESS = of(StatusEffects.BLINDNESS, 30, 0);
    Toxin POISON = of(StatusEffects.POISON, 45, 2);
    Toxin FOOD_POISONING = of(UEffects.FOOD_POISONING, 100, 2);
    Toxin WEAK_FOOD_POISONING = of(UEffects.FOOD_POISONING, 50, 1);

    Toxin LOVE_SICKNESS = of(Text.of("Love Sickness "), (player, stack) -> {
        FoodComponent food = stack.getItem().getFoodComponent();
        player.getHungerManager().add(-food.getHunger()/2, -food.getSaturationModifier()/2);
    }).and(STRONG_NAUSEA).and(IF_NOT_PEACEFUL.then(WEAK_FOOD_POISONING.withChance(20))).and(WEAKNESS);

    Toxin LOVE_CONSUMPTION = of(Text.literal("Love"), (player, stack) -> {
        player.heal(stack.isFood() ? stack.getItem().getFoodComponent().getHunger() : 1);
        player.removeStatusEffect(StatusEffects.NAUSEA);
        if (player.getWorld().random.nextInt(10) == 0) {
            player.removeStatusEffect(UEffects.FOOD_POISONING);
        }
    });

    static Toxin healing(int hearts) {
        return of(Text.literal("Healing " + hearts + " Hearts"), (player, stack) -> player.heal(hearts));
    }

    default void appendTooltip(List<Text> tooltip) {
        tooltip.add(getName());
    }

    default Toxin withChance(int max) {
        return Predicate.of(Text.of("1 in " + max + " chance of "), (player, stack) -> player.getWorld().random.nextInt(max) == 0).then(this);
    }

    Text getName();

    default Toxin and(Toxin other) {
        Toxin self = this;
        return new Toxin() {
            @Override
            public void afflict(PlayerEntity player, ItemStack stack) {
                self.afflict(player, stack);
                other.afflict(player, stack);
            }

            @Override
            public void appendTooltip(List<Text> tooltip) {
                self.appendTooltip(tooltip);
                other.appendTooltip(tooltip);
            }

            @Override
            public Text getName() {
                return self.getName().copy().append(" + ").append(other.getName());
            }
        };
    }

    static Toxin of(Text name, Affliction affliction) {
        return new Toxin() {
            @Override
            public void afflict(PlayerEntity player, ItemStack stack) {
                affliction.afflict(player, stack);
            }

            @Override
            public Text getName() {
                return name;
            }
        };
    }

    static Toxin of(StatusEffect effect, int seconds, int amplifier) {
        int ticks = seconds * 20;

        MutableText text = effect.getName().copy();

        if (amplifier > 0) {
            text = Text.translatable("potion.withAmplifier", text, Text.translatable("potion.potency." + amplifier));
        }

        text = Text.translatable("potion.withDuration", text, StringHelper.formatTicks(ticks));

        return of(text, (player, stack) -> {
            StatusEffectInstance current = player.getStatusEffect(effect);
            float health = player.getHealth();
            if (current != null) {
                player.addStatusEffect(new StatusEffectInstance(effect, ticks + current.getDuration(), amplifier + current.getAmplifier(), false, false, false));
            } else {
                player.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, false, false, false));
            }
            // keep original health
            if (effect.getAttributeModifiers().containsKey(EntityAttributes.GENERIC_MAX_HEALTH)) {
                player.setHealth(MathHelper.clamp(health, 0, player.getMaxHealth()));
            }
        });
    }

    interface Predicate {
        static Predicate of(Text name, Affliction.Predicate predicate) {
            return new Predicate() {
                @Override
                public boolean test(PlayerEntity player, ItemStack stack) {
                    return predicate.test(player, stack);
                }

                @Override
                public Text getName() {
                    return name;
                }
            };
        }

        boolean test(PlayerEntity player, ItemStack stack);

        Text getName();

        default Toxin then(Toxin toxin) {
            return new Toxin() {
                @Override
                public void afflict(PlayerEntity player, ItemStack stack) {
                    if (test(player, stack)) {
                        toxin.afflict(player, stack);
                    }
                }

                @Override
                public Text getName() {
                    return Predicate.this.getName().copy().append(toxin.getName());
                }
            };
        }
    }
}
