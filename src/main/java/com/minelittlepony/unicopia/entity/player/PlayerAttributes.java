package com.minelittlepony.unicopia.entity.player;

import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.container.HeavyInventory;
import com.minelittlepony.unicopia.mixin.Walker;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

class PlayerAttributes {

    static final EntityAttribute EXTENDED_REACH_DISTANCE = new ClampedEntityAttribute(null, "player.reachDistance", 0, 0, 10);

    private static final EntityAttributeModifier EARTH_PONY_STRENGTH =
            new EntityAttributeModifier(UUID.fromString("777a5505-521e-480b-b9d5-6ea54f259564"), "Earth Pony Strength", 0.6, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier PEGASUS_SPEED =
            new EntityAttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7"), "Pegasus Speed", 0.2, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier PEGASUS_REACH =
            new EntityAttributeModifier(UUID.fromString("707b50a8-03e8-40f4-8553-ecf67025fd6d"), "Pegasus Reach", 1.5, Operation.ADDITION);

    private double loadStrength = 0;

    public void applyAttributes(PlayerEntity entity, Race race) {
        loadStrength = HeavyInventory.getContentsTotalWorth(entity.inventory, false);

        ((Walker)entity.abilities).setWalkSpeed(0.1F - (float)(loadStrength / 100000));

        toggleAttribute(entity, EntityAttributes.ATTACK_DAMAGE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.KNOCKBACK_RESISTANCE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.MOVEMENT_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, EntityAttributes.ATTACK_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, EXTENDED_REACH_DISTANCE, PEGASUS_REACH, race.canFly());
    }

    private void toggleAttribute(PlayerEntity entity, EntityAttribute attribute, EntityAttributeModifier modifier, boolean enable) {

        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);

        if (enable) {
            if (!instance.hasModifier(modifier)) {
                instance.addModifier(modifier);
            }
        } else {
            if (instance.hasModifier(modifier)) {
                instance.removeModifier(modifier);
            }
        }
    }
}
