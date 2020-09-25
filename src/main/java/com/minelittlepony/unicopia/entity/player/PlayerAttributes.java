package com.minelittlepony.unicopia.entity.player;

import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.registry.Registry;

public class PlayerAttributes {

    public static final EntityAttribute EXTENDED_REACH_DISTANCE = register("unicopia.pegasus.reach", new ClampedEntityAttribute("player.reachDistance", 0, 0, 10).setTracked(true));
    public static final EntityAttribute ENTITY_GRAVTY_MODIFIER = register("unicopia.player.gravity", (new EntityAttribute("player.gravityModifier", 1) {}).setTracked(true));

    private static final EntityAttributeModifier EARTH_PONY_STRENGTH =
            new EntityAttributeModifier(UUID.fromString("777a5505-521e-480b-b9d5-6ea54f259564"), "Earth Pony Strength", 0.6, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier PEGASUS_SPEED =
            new EntityAttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7"), "Pegasus Speed", 0.2, Operation.MULTIPLY_TOTAL);
    private static final EntityAttributeModifier PEGASUS_REACH =
            new EntityAttributeModifier(UUID.fromString("707b50a8-03e8-40f4-8553-ecf67025fd6d"), "Pegasus Reach", 1.5, Operation.ADDITION);

    public static final EntityAttributeModifier BAT_HANGING =
            new EntityAttributeModifier(UUID.fromString("a54f2595-521e-480b-b9d5-6e750577a564"), "Bat Pony Hanging", -2, Operation.MULTIPLY_TOTAL);

    public void applyAttributes(Pony pony) {
        PlayerEntity entity = pony.getOwner();
        Race race = pony.getSpecies();

        toggleAttribute(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, EARTH_PONY_STRENGTH, race.canUseEarth());
        toggleAttribute(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, EntityAttributes.GENERIC_ATTACK_SPEED, PEGASUS_SPEED, race.canFly());
        toggleAttribute(entity, EXTENDED_REACH_DISTANCE, PEGASUS_REACH, race.canFly());
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

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return Registry.register(Registry.ATTRIBUTE, id, attribute);
    }
}
