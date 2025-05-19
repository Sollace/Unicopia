package com.minelittlepony.unicopia.entity.mob;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;

public interface UEntityAttributes {
    List<EntityAttribute> REGISTRY = new ArrayList<>();

    RegistryEntry<EntityAttribute> EXTENDED_REACH_DISTANCE = EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE;
    RegistryEntry<EntityAttribute> EXTENDED_ATTACK_DISTANCE = EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE;
    RegistryEntry<EntityAttribute> EXTRA_MINING_SPEED = register("earth.mining_speed", new ClampedEntityAttribute("player.miningSpeed", 1, 0, 5).setTracked(true));
    RegistryEntry<EntityAttribute> ENTITY_GRAVITY_MODIFIER = register("player.gravity", (new EntityAttribute("player.gravityModifier", 1) {}).setTracked(true));

    private static RegistryEntry<EntityAttribute> register(String name, EntityAttribute attribute) {
        REGISTRY.add(attribute);
        return Registry.registerReference(Registries.ATTRIBUTE, Unicopia.id(name), attribute);
    }

    static void bootstrap() {}
}
