package com.minelittlepony.unicopia.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;

public interface UEnchantments {
    List<RegistryKey<Enchantment>> REGISTRY = new ArrayList<>();

    /**
     * Makes a sound when there are interesting blocks in your area.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> GEM_FINDER = register("gem_finder");

    /**
     * Protects against wall collisions and earth pony attacks!
     *
     * EnchantmentTags.IN_ENCHANTING_TABLE
     * EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> PADDED = register("padded");

    /**
     * Allows non-flying races to mine and interact with cloud blocks
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> FEATHER_TOUCH = register("feather_touch");

    /**
     * Heavy players move more slowly but are less likely to be flung around wildly.
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> HEAVY = register("heavy");

    /**
     * It's dangerous to go alone, take this!
     *
     * Weapons will become stronger the more allies you have around.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> HERDS = register("herds");

    /**
     * Alters gravity
     *
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> REPULSION = register("repulsion");

    /**
     * I want it, I neeeed it!
     *
     * Mobs really want your candy. You'd better give it to them.
     *
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> WANT_IT_NEED_IT = register("want_it_need_it");

    /**
     * Hahaha geddit?
     *
     * Random things happen.
     *
     *  EnchantmentTags.NON_TREASURE
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> POISONED_JOKE = register("poisoned_joke");

    /**
     * Who doesn't like a good freakout?
     *
     *  EnchantmentTags.CURSE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> STRESSED = register("stressed");

    /**
     * This item just wants to be held.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> CLINGY = register("clingy");

    /**
     * Items with loyalty are kept after death.
     * Only works if they don't also have curse of binding.
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     */
    RegistryKey<Enchantment> HEART_BOUND = register("heart_bound");

    /**
     * Consumes drops whilst mining and produces experience instead
     *
     *  EnchantmentTags.IN_ENCHANTING_TABLE
     *  EnchantmentTags.TRADEABLE
     */
    RegistryKey<Enchantment> CONSUMPTION = register("consumption");

    static RegistryKey<Enchantment> register(String name) {
        RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Unicopia.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void register(Registry<Enchantment> registry, RegistryKey<Enchantment> key, Enchantment.Builder builder) {
        Registry.register(registry, key, builder.build(key.getValue()));
    }

    static void bootstrap() {
        UEnchantmentEffects.bootstrap();
        // Options.table -> EnchantmentTags.IN_ENCHANTING_TABLE
        // Optiona.curse -> EnchantmentTags.CURSE
        // Options.traded -> EnchantmentTags.TRADEABLE

        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
                Registry<Item> items = registries.getOptional(RegistryKeys.ITEM).orElseThrow();

                register(registry, GEM_FINDER, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.MINING_ENCHANTABLE).orElseThrow(),
                        Rarity.RARE,
                        3,
                        Enchantment.constantCost(1), Enchantment.constantCost(41),
                        4,
                        AttributeModifierSlot.HAND
                )));

                register(registry, PADDED, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.HEAD_ARMOR_ENCHANTABLE).orElseThrow(),
                        Rarity.UNCOMMON,
                        3,
                        Enchantment.constantCost(1), Enchantment.constantCost(41),
                        4,
                        AttributeModifierSlot.ARMOR
                )));
                register(registry, FEATHER_TOUCH, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.MINING_ENCHANTABLE).orElseThrow(),
                        Rarity.UNCOMMON,
                        1,
                        Enchantment.constantCost(1), Enchantment.constantCost(31),
                        3,
                        AttributeModifierSlot.HAND
                )));
                register(registry, HEAVY, Enchantment.builder(
                        Enchantment.definition(
                            items.getEntryList(ItemTags.ARMOR_ENCHANTABLE).orElseThrow(),
                            Rarity.RARE,
                            4,
                            Enchantment.constantCost(7), Enchantment.constantCost(23),
                            2,
                            AttributeModifierSlot.ARMOR
                    )
                ).exclusiveSet(registry.getEntryList(EnchantmentTags.ARMOR_EXCLUSIVE_SET).orElseThrow())
                    .addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(
                        Unicopia.id("enchantment.heaviness"),
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        EnchantmentLevelBasedValue.linear(-0.1F, -0.1F),
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                ));
                register(registry, HERDS, Enchantment.builder(
                        Enchantment.definition(
                            items.getEntryList(ItemTags.WEAPON_ENCHANTABLE).orElseThrow(),
                            Rarity.RARE,
                            3,
                            Enchantment.constantCost(8), Enchantment.constantCost(20),
                            1,
                            AttributeModifierSlot.MAINHAND
                    )
                ).addEffect(EnchantmentEffectComponentTypes.TICK, new GroupBasedAttributeEnchantmentEffect(new AttributeEnchantmentEffect(
                        Unicopia.id("team_strength"),
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        EnchantmentLevelBasedValue.linear(0, 1),
                        Operation.ADD_VALUE
                ), EnchantmentLevelBasedValue.linear(2, 2))));

                register(registry, REPULSION, Enchantment.builder(
                        Enchantment.definition(
                            items.getEntryList(ItemTags.FOOT_ARMOR_ENCHANTABLE).orElseThrow(),
                            Rarity.VERY_RARE,
                            3,
                            Enchantment.constantCost(9), Enchantment.constantCost(28),
                            3,
                            AttributeModifierSlot.FEET
                    )
                ).exclusiveSet(registry.getEntryList(EnchantmentTags.ARMOR_EXCLUSIVE_SET).orElseThrow())
                    .addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(
                        Unicopia.id("enchantment.repulsion"),
                        UEntityAttributes.ENTITY_GRAVITY_MODIFIER,
                        EnchantmentLevelBasedValue.linear(-0.5F, -0.375F),
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                ));

                register(registry, WANT_IT_NEED_IT, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.VANISHING_ENCHANTABLE).orElseThrow(),
                        Rarity.VERY_RARE,
                        1,
                        Enchantment.constantCost(2), Enchantment.constantCost(10),
                        4,
                        AttributeModifierSlot.ANY
                )).addEffect(EnchantmentEffectComponentTypes.TICK, new ParticleTrailEnchantmentEntityEffect(Optional.empty(), 0.2F, 1, 10)));

                register(registry, POISONED_JOKE, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.VANISHING_ENCHANTABLE).orElseThrow(),
                        Rarity.VERY_RARE,
                        1,
                        Enchantment.constantCost(2), Enchantment.constantCost(10),
                        4,
                        AttributeModifierSlot.ANY
                )).addEffect(EnchantmentEffectComponentTypes.TICK, new AmbientSoundsEnchantmentEffect(Unicopia.id("poisoned_joke_level"), UTags.Sounds.POISON_JOKE_EVENTS)));

                register(registry, CLINGY, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.VANISHING_ENCHANTABLE).orElseThrow(),
                        Rarity.VERY_RARE,
                        3,
                        Enchantment.constantCost(2), Enchantment.constantCost(12),
                        4,
                        AttributeModifierSlot.ANY
                )).addEffect(EnchantmentEffectComponentTypes.TICK, DangerSensingEnchantmentEffect.INSTANCE));

                register(registry, CLINGY, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.EQUIPPABLE_ENCHANTABLE).orElseThrow(),
                        Rarity.VERY_RARE,
                        6,
                        Enchantment.constantCost(2), Enchantment.constantCost(12),
                        1,
                        AttributeModifierSlot.ANY
                )));

                register(registry, HEART_BOUND, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.VANISHING_ENCHANTABLE).orElseThrow(),
                        Rarity.UNCOMMON,
                        5,
                        Enchantment.constantCost(6), Enchantment.constantCost(41),
                        3,
                        AttributeModifierSlot.ANY
                )));

                register(registry, CONSUMPTION, Enchantment.builder(Enchantment.definition(
                        items.getEntryList(ItemTags.MINING_ENCHANTABLE).orElseThrow(),
                        Rarity.VERY_RARE,
                        1,
                        Enchantment.constantCost(10), Enchantment.constantCost(71),
                        5,
                        AttributeModifierSlot.MAINHAND
                )));
            });
        });
    }

}
