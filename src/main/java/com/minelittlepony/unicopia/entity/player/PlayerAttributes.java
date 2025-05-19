package com.minelittlepony.unicopia.entity.player;

import java.util.List;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PlayerAttributes implements Tickable {
    private final static List<ToggleableAttribute> ATTRIBUTES = List.of(
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("earth_pony_strength"), 0.6, Operation.ADD_MULTIPLIED_TOTAL),
                    List.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().canUseEarth()
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("earth_pony_knockback_resistance"), 6, Operation.ADD_VALUE),
                    List.of(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().canUseEarth() && pony.asEntity().isSneaking()
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("earth_pony_mining_speed"), 0.5, Operation.ADD_MULTIPLIED_TOTAL),
                    List.of(UEntityAttributes.EXTRA_MINING_SPEED),
                    pony -> pony.getCompositeRace().canUseEarth()
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("pegasus_speed"), 0.2, Operation.ADD_MULTIPLIED_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED),
                    pony -> pony.getCompositeRace().canFly() && !pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("pegasus_reach"), 1.5, Operation.ADD_VALUE),
                    List.of(UEntityAttributes.EXTENDED_REACH_DISTANCE),
                    pony -> pony.getCompositeRace().canFly() && !pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("hippogriff_speed"), 0.1, Operation.ADD_MULTIPLIED_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED),
                    pony -> pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("hippogriff_reach"), 1.3, Operation.ADD_VALUE),
                    List.of(UEntityAttributes.EXTENDED_REACH_DISTANCE),
                    pony -> pony.getCompositeRace().includes(Race.HIPPOGRIFF)
            ),

            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("kirin_knockback_vulneravility"), -2, Operation.ADD_VALUE),
                    List.of(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE),
                    pony -> pony.getCompositeRace().includes(Race.KIRIN)
            ),
            new ToggleableAttribute(
                    new EntityAttributeModifier(Unicopia.id("kirin_rage"), 0.7, Operation.ADD_MULTIPLIED_TOTAL),
                    List.of(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            EntityAttributes.GENERIC_ATTACK_SPEED,
                            EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                            EntityAttributes.GENERIC_ATTACK_KNOCKBACK
                    ),
                    SpellType.RAGE::isOn
            )
    );

    public static final Identifier HEALTH_SWAPPING_MODIFIER_ID = Unicopia.id("health_swap");

    public static EntityAttributeModifier healthChange(float addition) {
        return new EntityAttributeModifier(HEALTH_SWAPPING_MODIFIER_ID, addition, Operation.ADD_VALUE);
    }

    private final Pony pony;

    public PlayerAttributes(Pony pony) {
        this.pony = pony;
    }

    @Override
    public void tick() {
        ATTRIBUTES.forEach(attribute -> attribute.update(pony));
        EffectUtils.applyStatusEffect(pony.asEntity(), UEffects.FORTIFICATION, pony.getCompositeRace().canUseEarth() && pony.asEntity().isSneaking());
    }

    record ToggleableAttribute(EntityAttributeModifier modifier, List<RegistryEntry<EntityAttribute>> attributes, Predicate<Pony> test) {
        public void update(Pony pony) {
            boolean enable = test.test(pony);
            attributes.forEach(attribute -> pony.applyAttributeModifier(attribute, modifier, true, enable));
        }
    }
}
