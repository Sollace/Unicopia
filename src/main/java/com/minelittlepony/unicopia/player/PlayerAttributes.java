package com.minelittlepony.unicopia.player;

import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.inventory.InventoryOfHolding;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

class PlayerAttributes {
    public static final int ADD = 0;
    public static final int ADD_PERCENTAGE = 1;
    public static final int MULTIPLY = 2;

    private static final AttributeModifier EARTH_PONY_STRENGTH =
            new AttributeModifier(UUID.fromString("777a5505-521e-480b-b9d5-6ea54f259564"), "Earth Pony Strength", 0.6, MULTIPLY);

    private static final AttributeModifier PEGASUS_SPEED =
            new AttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7"), "Pegasus Speed", 0.2, MULTIPLY);

    private static final AttributeModifier PEGASUS_REACH =
            new AttributeModifier(UUID.fromString("707b50a8-03e8-40f4-8553-ecf67025fd6d"), "Pegasus Reach", 1.5, ADD);

    private double loadStrength = 0;

    public void applyAttributes(EntityPlayer entity, Race race) {
        loadStrength = 0;

        for (ItemStack item : entity.inventory.mainInventory) {
            loadStrength += InventoryOfHolding.decodeStackWeight(item);
        }
        for (ItemStack item : entity.inventory.armorInventory) {
            loadStrength += InventoryOfHolding.decodeStackWeight(item);
        }

        if (entity.world.isRemote) {
            entity.capabilities.setPlayerWalkSpeed(0.1F - (float)(loadStrength / 100000));
        }

        applyAttribute(entity, SharedMonsterAttributes.ATTACK_DAMAGE, EARTH_PONY_STRENGTH, race.canUseEarth());
        applyAttribute(entity, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, EARTH_PONY_STRENGTH, race.canUseEarth());
        applyAttribute(entity, SharedMonsterAttributes.MOVEMENT_SPEED, PEGASUS_SPEED, race.canFly());
        applyAttribute(entity, SharedMonsterAttributes.ATTACK_SPEED, PEGASUS_SPEED, race.canFly());
        applyAttribute(entity, EntityPlayer.REACH_DISTANCE, PEGASUS_REACH, race.canFly());
    }

    private void applyAttribute(EntityPlayer entity, IAttribute attribute, AttributeModifier modifier, boolean enable) {
        IAttributeInstance instance = entity.getEntityAttribute(attribute);

        if (enable) {
            if (instance.getModifier(modifier.getID()) == null) {
                instance.applyModifier(modifier);
            }
        } else {
            if (instance.getModifier(modifier.getID()) != null) {
                instance.removeModifier(modifier);
            }
        }
    }
}
