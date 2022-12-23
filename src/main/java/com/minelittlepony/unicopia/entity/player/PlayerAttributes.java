package com.minelittlepony.unicopia.entity.player;

import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.UEntityAttributes;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerAttributes implements Tickable {
    private static final EntityAttributeModifier EARTH_PONY_STRENGTH =
            new EntityAttributeModifier(UUID.fromString("777a5505-521e-480b-b9d5-6ea54f259564"), "Earth Pony Strength", 0.6, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier EARTH_PONY_MINING_SPEED =
            new EntityAttributeModifier(UUID.fromString("9fc9e269-152e-0b48-9bd5-564a546e59f2"), "Earth Pony Mining Speed", 0.4, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier EARTH_PONY_KNOCKBACK_RESISTANCE =
            new EntityAttributeModifier(UUID.fromString("79e269a8-03e8-b9d5-5853-e25fdcf6706d"), "Earth Pony Knockback Resistance", 2, Operation.ADDITION);

    private static final EntityAttributeModifier PEGASUS_SPEED =
            new EntityAttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7"), "Pegasus Speed", 0.2, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier PEGASUS_REACH =
            new EntityAttributeModifier(UUID.fromString("707b50a8-03e8-40f4-8553-ecf67025fd6d"), "Pegasus Reach", 1.5, Operation.ADDITION);

    public static final UUID HEALTH_SWAPPING_MODIFIER_ID = UUID.fromString("7b93803e-4b25-11ed-951e-00155d43e0a2");

    public static EntityAttributeModifier healthChange(float addition) {
        return new EntityAttributeModifier(HEALTH_SWAPPING_MODIFIER_ID, "Health Swap", addition, Operation.ADDITION);
    }

    private final Pony pony;

    public PlayerAttributes(Pony pony) {
        this.pony = pony;
    }

    @Override
    public void tick() {
        PlayerEntity entity = pony.asEntity();
        Race race = pony.getSpecies();

        toggleAttribute(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, EARTH_PONY_KNOCKBACK_RESISTANCE, race.canUseEarth() && entity.isSneaking());
        toggleAttribute(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, EntityAttributes.GENERIC_ATTACK_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, UEntityAttributes.EXTENDED_REACH_DISTANCE, PEGASUS_REACH, race.canFly());
        toggleAttribute(entity, UEntityAttributes.EXTRA_MINING_SPEED, EARTH_PONY_MINING_SPEED, race.canUseEarth());
    }

    private void toggleAttribute(PlayerEntity entity, EntityAttribute attribute, EntityAttributeModifier modifier, boolean enable) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);

        if (enable) {
            if (!instance.hasModifier(modifier)) {
                instance.addPersistentModifier(modifier);
            }
        } else {
            instance.tryRemoveModifier(modifier.getId());
        }
    }

}
