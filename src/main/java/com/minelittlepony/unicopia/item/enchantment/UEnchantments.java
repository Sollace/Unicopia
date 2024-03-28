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
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment GEM_FINDER = register("gem_finder", new GemFindingEnchantment(Options.create(EnchantmentTarget.DIGGER, UEnchantmentValidSlots.HANDS).rarity(Rarity.RARE).maxLevel(3).treasure().traded().table()));

    /**
     * Protects against wall collisions and earth pony attacks!
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment PADDED = register("padded", new SimpleEnchantment(Options.armor().rarity(Rarity.UNCOMMON).maxLevel(3).traded().table()));

    /**
     * Allows non-flying races to mine and interact with cloud blocks
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment FEATHER_TOUCH = register("feather_touch", new SimpleEnchantment(Options.create(EnchantmentTarget.BREAKABLE, UEnchantmentValidSlots.HANDS).rarity(Rarity.UNCOMMON).traded().table()));

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment HEAVY = register("heavy", new AttributedEnchantment(Options.armor().rarity(Rarity.RARE).maxLevel(4).traded().table()))
            .addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("a3d5a94f-4c40-48f6-a343-558502a13e10"), "Heavyness", (1 - level/(float)10) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment HERDS = register("herds", new CollaboratorEnchantment(Options.create(EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND).rarity(Rarity.RARE).maxLevel(3).treasure().traded().table()));

    /**
     * Alters gravity
     *
     * Appears in:
     *  - Trades
     */
    Enchantment REPULSION = register("repulsion", new AttributedEnchantment(Options.create(EnchantmentTarget.ARMOR_FEET, EquipmentSlot.FEET).rarity(Rarity.VERY_RARE).maxLevel(3).treasure().traded()))
            .addModifier(UEntityAttributes.ENTITY_GRAVITY_MODIFIER, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("1734bbd6-1916-4124-b710-5450ea70fbdb"), "Anti Grav", (0.5F - (0.375 * (level - 1))) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     */
    Enchantment WANT_IT_NEED_IT = register("want_it_need_it", new WantItNeedItEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().treasure().traded()));

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     *
     * Appears in:
     *  - Trades
     */
    PoisonedJokeEnchantment POISONED_JOKE = register("poisoned_joke", new PoisonedJokeEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().traded()));

    /**
     * Who doesn't like a good freakout?
     *
     * Appears in:
     *  - Trades
     */
    Enchantment STRESSED = register("stressed", new StressfulEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).curse().treasure().traded().maxLevel(3)));

    /**
     * This item just wants to be held.
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment CLINGY = register("clingy", new SimpleEnchantment(Options.allItems().rarity(Rarity.VERY_RARE).maxLevel(6).traded().table().treasure()));

    /**
     * Items with loyalty are kept after death.
     * Only works if they don't also have curse of binding.
     *
     * Appears in:
     *  - Enchanting Table
     */
    Enchantment HEART_BOUND = register("heart_bound", new SimpleEnchantment(Options.create(EnchantmentTarget.VANISHABLE, UEnchantmentValidSlots.ANY).rarity(Rarity.UNCOMMON).maxLevel(5).treasure().table()));

    /**
     * Consumes drops whilst mining and produces experience instead
     *
     * Appears in:
     *  - Trades
     *  - Enchanting Table
     */
    Enchantment CONSUMPTION = register("consumption", new ConsumptionEnchantment(Options.create(EnchantmentTarget.DIGGER, UEnchantmentValidSlots.HANDS).rarity(Rarity.VERY_RARE).treasure().table().traded()));

    static void bootstrap() { }

    static <T extends SimpleEnchantment> T register(String name, T enchantment) {
        REGISTRY.add(enchantment);
        return Registry.register(Registries.ENCHANTMENT, Unicopia.id(name), enchantment);
    }
}
