package com.minelittlepony.unicopia.datafix;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

public interface Schemas {
    static void build(DataFixerBuilder builder) {
        new AttributeIdsFixBuilder()
            .add("777a5505-521e-480b-b9d5-6ea54f259564", "Earth Pony Strength", "unicopia:earth_pony_strength")
            .add("79e269a8-03e8-b9d5-5853-e25fdcf6706d", "Earth Pony Knockback Resistance", "unicopia:earth_pony_knockback_resistance")
            .add("9fc9e269-152e-0b48-9bd5-564a546e59f2", "Earth Pony Mining Speed", "unicopia:earth_pony_mining_speed")
            .add("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7", "Pegasus Speed", "unicopia:pegasus_speed")
            .add("707b50a8-03e8-40f4-8553-ecf67025fd6d", "Pegasus Reach", "unicopia:pegasus_reach")
            .add("9e2699fc-3b8d-4f71-92dd-bef19b92e4f7", "Hippogriff Speed", "unicopia:hippogriff_speed")
            .add("707b50a8-03e8-40f4-5853-fc7e0f625d6d", "Hippogriff Reach", "unicopia:hippogriff_reach")
            .add("79e269a8-03e8-b9d5-5853-e25fdcf6706e", "Kirin Knockback Vulnerability", "unicopia:kirin_knockback_vulneravility")
            .add("4991fde9-c685-4930-bbd2-d7a228728bfe", "Kirin Rage Speed", "unicopia:kirin_rage")
            .add("7b93803e-4b25-11ed-951e-00155d43e0a2", "Health Swap", "unicopia:health_swap")
            .add("7b16994b-1edb-4381-be62-94317f39ec8f", "unicopia:knockback_modifier")
            .add("7b16994b-1edb-8431-be62-7f39ec94318f", "unicopia:luck_modifier")
            .add("FA235E1C-4280-A865-B01B-CBAE9985ACA3", "unicopia:attack_range_modifier")
            .add("A7B3659C-AA74-469C-963A-09A391DCAA0F", "unicopia:attack_range_modifier")
            .add("c0a870f5-99ef-4716-a23e-f320ee834b26", "Alicorn Amulet Modifier", "unicopia:alicorn_amulet_modifiers")
            .add("5f08c02d-d959-4763-ac84-16e2acfd4b62", "Team Strength", "unicopia:enchantment.team.strength")
            .add("a3d5a94f-4c40-48f6-a343-558502a13e10", "Heaviness", "unicopia:enchantment.heaviness")
            .add("1734bbd6-1916-4124-b710-5450ea70fbdb", "Anti Grav", "unicopia:enchantment.repulsion")
            .add("9dc7818b-927b-46e0-acbe-48d31a28128f", "Bubble Floating", "unicopia:bubble_floating_modifier")
            .register();
    }

    static void fixComponents(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic) {
        UnicopiaItemStackComponentizations.run(data, dynamic);
    }
}
