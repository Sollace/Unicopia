package com.minelittlepony.unicopia.entity.player;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;

public class PlayerAttributes implements Tickable {
    private final static List<ToggleableAttribute> ATTRIBUTES = List.of(
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("777a5505-521e-480b-b9d5-6ea54f259564"), "Earth Pony Strength", 0.6, Operation.MULTIPLY_TOTAL),
                    List.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().canUseEarth()
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("79e269a8-03e8-b9d5-5853-e25fdcf6706d"), "Earth Pony Knockback Resistance", 6, Operation.ADDITION),
                    List.of(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().canUseEarth() && pony.asEntity().isSneaking()
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("9fc9e269-152e-0b48-9bd5-564a546e59f2"), "Earth Pony Mining Speed", 0.5, Operation.MULTIPLY_TOTAL),
                    List.of(UEntityAttributes.EXTRA_MINING_SPEED),
                    pony -> pony.getCompositeRace().canUseEarth()
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-9d2d-fb92ee19b4f7"), "Pegasus Speed", 0.2, Operation.MULTIPLY_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED),
                    pony -> pony.getCompositeRace().canFly() && !pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("707b50a8-03e8-40f4-8553-ecf67025fd6d"), "Pegasus Reach", 1.5, Operation.ADDITION),
                    List.of(UEntityAttributes.EXTENDED_REACH_DISTANCE),
                    pony -> pony.getCompositeRace().canFly() && !pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("9e2699fc-3b8d-4f71-92dd-bef19b92e4f7"), "Hippogriff Speed", 0.1, Operation.MULTIPLY_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED),
                    pony -> pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("707b50a8-03e8-40f4-5853-fc7e0f625d6d"), "Hippogriff Reach", 1.3, Operation.ADDITION),
                    List.of(UEntityAttributes.EXTENDED_REACH_DISTANCE),
                    pony -> pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("79e269a8-03e8-b9d5-5853-e25fdcf6706e"), "Kirin Knockback Vulnerability", -2, Operation.ADDITION),
                    List.of(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().includes(Race.KIRIN)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(UUID.fromString("4991fde9-c685-4930-bbd2-d7a228728bfe"), "Kirin Rage Speed", 0.7, Operation.MULTIPLY_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                            EntityAttributes.GENERIC_ATTACK_KNOCKBACK
                    ),
                    SpellType.RAGE::isOn
            )
    );

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
        ATTRIBUTES.forEach(attribute -> attribute.update(pony));
    }

    record ToggleableAttribute(EntityAttributeModifier modifier, List<EntityAttribute> attributes, Predicate<Pony> test) {
        public void update(Pony pony) {
            boolean enable = test.test(pony);
            attributes.forEach(attribute -> {
                EntityAttributeInstance instance = pony.asEntity().getAttributeInstance(attribute);

                if (enable) {
                    if (!instance.hasModifier(modifier)) {
                        instance.addPersistentModifier(modifier);
                    }
                } else {
                    instance.tryRemoveModifier(modifier.getId());
                }
            });
        }
    }
}
