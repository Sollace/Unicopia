package com.minelittlepony.unicopia.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UEnchantments {

    List<SimpleEnchantment> REGISTRY = new ArrayList<>();

    /**
     * Makes a sound when there are interesting blocks in your area.
     */
    Enchantment GEM_LOCATION = register("gem_location", new GemFindingEnchantment());

    /**
     * Protects against wall collisions and earth pony attacks!
     *
     * TODO:
     */
    Enchantment PADDED = register("padded", new SimpleEnchantment(Rarity.COMMON, EnchantmentTarget.ARMOR, false, 3, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     *
     * TODO:
     */
    Enchantment HEAVY = register("heavy", new AttributedEnchantment(Rarity.COMMON, EnchantmentTarget.ARMOR_FEET, false, 4, EquipmentSlot.FEET))
            .addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, level -> {
                // 1 -> 0.9
                // 2 -> 0.8
                // 3 -> 0.7
                return new EntityAttributeModifier(UUID.fromString("a3d5a94f-4c40-48f6-a343-558502a13e10"), "Heavyness", (1 - level/(float)10) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     *
     * TODO:
     */
    Enchantment COLLABORATOR = register("collaborator", new SimpleEnchantment(Rarity.COMMON, EnchantmentTarget.WEAPON, false, 1, EquipmentSlot.MAINHAND));

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     */
    Enchantment DESIRED = register("desired", new WantItNeedItEnchantment());

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     *
     * TODO:
     */
    Enchantment POISON_JOKE = register("poison_joke", new SimpleEnchantment(Rarity.COMMON, true, 3, EquipmentSlot.values()));

    /**
     * Who doesn't like a good freakout?
     */
    Enchantment STRESS = register("stress", new StressfulEnchantment());

    static void bootstrap() { }

    static <T extends SimpleEnchantment> T register(String name, T enchantment) {
        REGISTRY.add(enchantment);
        return Registry.register(Registry.ENCHANTMENT, new Identifier("unicopia", name), enchantment);
    }
}
