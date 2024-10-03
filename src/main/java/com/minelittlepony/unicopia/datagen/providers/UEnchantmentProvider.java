package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.datagen.DataGenRegistryProvider;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.item.enchantment.AmbientSoundsEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.DangerSensingEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.GroupBasedAttributeEnchantmentEffect;
import com.minelittlepony.unicopia.item.enchantment.ParticleTrailEnchantmentEntityEffect;
import com.minelittlepony.unicopia.item.enchantment.Rarity;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;

public class UEnchantmentProvider extends DataGenRegistryProvider<Enchantment> {
    public UEnchantmentProvider() {
        super(RegistryKeys.ENCHANTMENT);
    }

    @Override
    public void run(Registerable<Enchantment> registry) {
        var items = registry.getRegistryLookup(RegistryKeys.ITEM);
        var enchantments = registry.getRegistryLookup(RegistryKeys.ENCHANTMENT);

        register(registry, UEnchantments.GEM_FINDER, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.RARE,
                3,
                Enchantment.constantCost(1), Enchantment.constantCost(41),
                4,
                AttributeModifierSlot.HAND
        )));

        register(registry, UEnchantments.PADDED, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                Rarity.UNCOMMON,
                3,
                Enchantment.constantCost(1), Enchantment.constantCost(41),
                4,
                AttributeModifierSlot.ARMOR
        )));
        register(registry, UEnchantments.FEATHER_TOUCH, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.UNCOMMON,
                1,
                Enchantment.constantCost(1), Enchantment.constantCost(31),
                3,
                AttributeModifierSlot.HAND
        )));
        register(registry, UEnchantments.HEAVY, Enchantment.builder(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    Rarity.RARE,
                    4,
                    Enchantment.constantCost(7), Enchantment.constantCost(23),
                    2,
                    AttributeModifierSlot.ARMOR
            )
        ).exclusiveSet(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE_SET))
            .addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(
                Unicopia.id("enchantment.heaviness"),
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                EnchantmentLevelBasedValue.linear(-0.1F, -0.1F),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        ));
        register(registry, UEnchantments.HERDS, Enchantment.builder(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    Rarity.RARE,
                    3,
                    Enchantment.constantCost(8), Enchantment.constantCost(20),
                    1,
                    AttributeModifierSlot.MAINHAND
            )
        ).addEffect(EnchantmentEffectComponentTypes.TICK, new GroupBasedAttributeEnchantmentEffect(new AttributeEnchantmentEffect(
                Unicopia.id("enchantment.team.strength"),
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                EnchantmentLevelBasedValue.linear(0, 1),
                Operation.ADD_VALUE
        ), EnchantmentLevelBasedValue.linear(2, 2))));

        register(registry, UEnchantments.REPULSION, Enchantment.builder(
                Enchantment.definition(
                    items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                    Rarity.VERY_RARE,
                    3,
                    Enchantment.constantCost(9), Enchantment.constantCost(28),
                    3,
                    AttributeModifierSlot.FEET
            )
        ).exclusiveSet(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE_SET))
            .addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(
                Unicopia.id("enchantment.repulsion"),
                UEntityAttributes.ENTITY_GRAVITY_MODIFIER,
                EnchantmentLevelBasedValue.linear(-0.5F, -0.375F),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        ));

        register(registry, UEnchantments.WANT_IT_NEED_IT, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.constantCost(2), Enchantment.constantCost(10),
                4,
                AttributeModifierSlot.ANY
        )).addEffect(EnchantmentEffectComponentTypes.TICK, new ParticleTrailEnchantmentEntityEffect(Optional.empty(), 0.2F, 1, 10)));

        register(registry, UEnchantments.POISONED_JOKE, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.constantCost(2), Enchantment.constantCost(10),
                4,
                AttributeModifierSlot.ANY
        )).addEffect(EnchantmentEffectComponentTypes.TICK, new AmbientSoundsEnchantmentEffect(Unicopia.id("poisoned_joke_level"), UTags.Sounds.POISON_JOKE_EVENTS)));

        register(registry, UEnchantments.STRESSED, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.VERY_RARE,
                3,
                Enchantment.constantCost(2), Enchantment.constantCost(12),
                4,
                AttributeModifierSlot.ANY
        )).addEffect(EnchantmentEffectComponentTypes.TICK, DangerSensingEnchantmentEffect.INSTANCE));

        register(registry, UEnchantments.CLINGY, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.EQUIPPABLE_ENCHANTABLE),
                Rarity.VERY_RARE,
                6,
                Enchantment.constantCost(2), Enchantment.constantCost(12),
                1,
                AttributeModifierSlot.ANY
        )));

        register(registry, UEnchantments.HEART_BOUND, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                Rarity.UNCOMMON,
                5,
                Enchantment.constantCost(6), Enchantment.constantCost(41),
                3,
                AttributeModifierSlot.ANY
        )));

        register(registry, UEnchantments.CONSUMPTION, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                Rarity.VERY_RARE,
                1,
                Enchantment.constantCost(10), Enchantment.constantCost(71),
                5,
                AttributeModifierSlot.MAINHAND
        )));
    }

    static void register(Registerable<Enchantment> registry, RegistryKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.getValue()));
    }

    @Override
    protected void configureTags(TagProvider tagProvider, WrapperLookup lookup) {
        tagProvider.getOrCreateTagBuilder(EnchantmentTags.IN_ENCHANTING_TABLE).add(
                UEnchantments.GEM_FINDER, UEnchantments.PADDED, UEnchantments.FEATHER_TOUCH,
                UEnchantments.HEAVY, UEnchantments.HERDS, UEnchantments.CLINGY, UEnchantments.HEART_BOUND,
                UEnchantments.CONSUMPTION
        );
        tagProvider.getOrCreateTagBuilder(EnchantmentTags.TRADEABLE).add(
                UEnchantments.GEM_FINDER, UEnchantments.PADDED, UEnchantments.FEATHER_TOUCH,
                UEnchantments.HEAVY, UEnchantments.HERDS, UEnchantments.REPULSION, UEnchantments.WANT_IT_NEED_IT,
                UEnchantments.POISONED_JOKE, UEnchantments.STRESSED, UEnchantments.CLINGY, UEnchantments.CONSUMPTION
        );
        tagProvider.getOrCreateTagBuilder(EnchantmentTags.NON_TREASURE).add(
                UEnchantments.FEATHER_TOUCH, UEnchantments.HEAVY, UEnchantments.POISONED_JOKE
        );
        tagProvider.getOrCreateTagBuilder(EnchantmentTags.CURSE).add(
                UEnchantments.WANT_IT_NEED_IT, UEnchantments.POISONED_JOKE, UEnchantments.STRESSED
        );
    }
}
