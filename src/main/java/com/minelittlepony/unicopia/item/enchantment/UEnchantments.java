package com.minelittlepony.unicopia.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.item.enchantment.SimpleEnchantment.Options;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public interface UEnchantments {

    List<SimpleEnchantment> REGISTRY = new ArrayList<>();

    /**
     * Makes a sound when there are interesting blocks in your area.
     */
    Enchantment GEM_FINDER = register("gem_finder", new GemFindingEnchantment(Options.create(EnchantmentTarget.DIGGER, UEnchantmentValidSlots.HANDS).rarity(Rarity.RARE).maxLevel(3).treasure()));

    /**
     * Protects against wall collisions and earth pony attacks!
     */
    Enchantment PADDED = register("padded", new SimpleEnchantment(Options.armor().rarity(Rarity.COMMON).maxLevel(3)));

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     */
    Enchantment HEAVY = register("heavy", new AttributedEnchantment(Options.armor().rarity(Rarity.COMMON).maxLevel(4)))
            .addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("a3d5a94f-4c40-48f6-a343-558502a13e10"), "Heavyness", (1 - level/(float)10) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     */
    Enchantment HERDS = register("herds", new CollaboratorEnchantment(Options.create(EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND).rarity(Rarity.RARE).maxLevel(3)));

    /**
     * Alters gravity
     */
    Enchantment REPULSION = register("repulsion", new AttributedEnchantment(Options.create(EnchantmentTarget.ARMOR_FEET, EquipmentSlot.FEET).rarity(Rarity.VERY_RARE).maxLevel(3)))
            .addModifier(UEntityAttributes.ENTITY_GRAVITY_MODIFIER, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("1734bbd6-1916-4124-b710-5450ea70fbdb"), "Anti Grav", (0.5F - (0.375 * (level - 1))) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     */
    Enchantment WANT_IT_NEED_IT = register("want_it_need_it", new WantItNeedItEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().treasure()));

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     */
    PoisonedJokeEnchantment POISONED_JOKE = register("poisoned_joke", new PoisonedJokeEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().tradedOnly()));

    /**
     * Who doesn't like a good freakout?
     */
    Enchantment STRESSED = register("stressed", new StressfulEnchantment(Options.allItems().rarity(Rarity.RARE).curse().treasure().maxLevel(3)));

    /**
     * This item just wants to be held.
     */
    Enchantment CLINGY = register("clingy", new SimpleEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().maxLevel(6)));

    /**
     * Items with loyalty are kept after death.
     * Only works if they don't also have curse of binding.
     */
    Enchantment HEART_BOUND = register("heart_bound", new SimpleEnchantment(Options.create(EnchantmentTarget.VANISHABLE, UEnchantmentValidSlots.ANY).rarity(Rarity.COMMON).maxLevel(5)));

    /**
     * Consumes drops whilst mining and produces experience instead
     */
    Enchantment CONSUMPTION = register("consumption", new ConsumptionEnchantment(Options.create(EnchantmentTarget.DIGGER, UEnchantmentValidSlots.HANDS).rarity(Rarity.VERY_RARE)));

    static void bootstrap() { }

    static <T extends SimpleEnchantment> T register(String name, T enchantment) {
        REGISTRY.add(enchantment);
        return Registry.register(Registries.ENCHANTMENT, Unicopia.id(name), enchantment);
    }
}
