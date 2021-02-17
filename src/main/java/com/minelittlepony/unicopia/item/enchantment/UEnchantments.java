package com.minelittlepony.unicopia.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minelittlepony.unicopia.entity.player.PlayerAttributes;

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
    Enchantment GEM_FINDER = register("gem_finder", new GemFindingEnchantment());

    /**
     * Protects against wall collisions and earth pony attacks!
     */
    Enchantment PADDED = register("padded", new SimpleEnchantment(Rarity.COMMON, EnchantmentTarget.ARMOR, false, 3, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     */
    Enchantment HEAVY = register("heavy", new AttributedEnchantment(Rarity.COMMON, EnchantmentTarget.ARMOR_FEET, false, 4, EquipmentSlot.FEET))
            .addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("a3d5a94f-4c40-48f6-a343-558502a13e10"), "Heavyness", (1 - level/(float)10) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     */
    Enchantment HERDS = register("herds", new CollaboratorEnchantment());

    /**
     * Alters gravity
     */
    Enchantment REPULSION = register("repulsion", new AttributedEnchantment(Rarity.VERY_RARE, false, 3, EquipmentSlot.FEET))
            .addModifier(PlayerAttributes.ENTITY_GRAVTY_MODIFIER, (user, level) -> {
                return new EntityAttributeModifier(UUID.fromString("1734bbd6-1916-4124-b710-5450ea70fbdb"), "Anti Grav", (0.5F - (0.375 * (level - 1))) - 1, Operation.MULTIPLY_TOTAL);
            });

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     */
    Enchantment WANT_IT_NEED_IT = register("want_it_need_it", new WantItNeedItEnchantment());

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     */
    PoisonedJokeEnchantment POISONED_JOKE = register("poisoned_joke", new PoisonedJokeEnchantment());

    /**
     * Who doesn't like a good freakout?
     */
    Enchantment STRESSED = register("stressed", new StressfulEnchantment());

    /**
     * This item just wants to be held.
     */
    Enchantment CLINGY = register("clingy", new SimpleEnchantment(Rarity.VERY_RARE, true, 6, EquipmentSlot.values()));

    static void bootstrap() { }

    static <T extends SimpleEnchantment> T register(String name, T enchantment) {
        REGISTRY.add(enchantment);
        return Registry.register(Registry.ENCHANTMENT, new Identifier("unicopia", name), enchantment);
    }
}
