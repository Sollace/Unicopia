package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record SpellAttributeType(Identifier id, Text name) {
    public static final List<SpellAttributeType> REGISTRY = new ArrayList<>();

    @Deprecated
    public static final SpellAttributeType CAST_ON_LOCATION = register("cast_on.location");
    public static final SpellAttributeType FOLLOWS_TARGET = register("follows_target");

    public static final SpellAttributeType PERMIT_ITEMS = register("permit_items");
    public static final SpellAttributeType PERMIT_PASSIVE = register("permit_passive");
    public static final SpellAttributeType PERMIT_HOSTILE = register("permit_hostile");
    public static final SpellAttributeType PERMIT_PLAYER = register("permit_player");

    public static final SpellAttributeType FOCUSED_ENTITY = register("focused_entity");
    public static final SpellAttributeType RANGE = register("range");
    public static final SpellAttributeType DURATION = register("duration");
    public static final SpellAttributeType STRENGTH = register("strength");
    public static final SpellAttributeType VELOCITY = register("velocity");
    public static final SpellAttributeType VERTICAL_VELOCITY = register("vertical_velocity");
    public static final SpellAttributeType HANG_TIME = register("hang_time");
    public static final SpellAttributeType PUSHING_POWER = register("pushing_power");
    public static final SpellAttributeType CAUSES_LEVITATION = register("causes_levitation");
    public static final SpellAttributeType AFFECTS = register("affects");
    public static final SpellAttributeType DAMAGE_TO_TARGET = register("damage_to_target");
    public static final SpellAttributeType SIMULTANIOUS_TARGETS = register("simultanious_targets");
    public static final SpellAttributeType COST_PER_INDIVIDUAL = register("cost_per_individual");
    public static final SpellAttributeType EXPLOSION_STRENGTH = register("explosion_strength");
    public static final SpellAttributeType PROJECTILE_COUNT = register("projectile_count");
    public static final SpellAttributeType ORB_COUNT = register("orb_count");
    public static final SpellAttributeType WAVE_SIZE = register("wave_size");
    public static final SpellAttributeType FOLLOW_RANGE = register("follow_range");
    public static final SpellAttributeType LIGHT_TARGET = register("light_target");
    public static final SpellAttributeType STICK_TO_TARGET = register("stick_to_target");
    public static final SpellAttributeType SOAPINESS = register("soapiness");
    public static final SpellAttributeType CAST_ON = register("cast_on");

    public static final SpellAttributeType TARGET_PREFERENCE = register("target_preference");
    public static final SpellAttributeType CASTER_PREFERENCE = register("caster_preference");
    public static final SpellAttributeType NEGATES_FALL_DAMAGE = register("negates_fall_damage");

    public SpellAttributeType(Identifier id) {
        this(id, Text.translatable(Util.createTranslationKey("spell_attribute", id)));
    }

    public static SpellAttributeType register(String name) {
        SpellAttributeType type = new SpellAttributeType(Unicopia.id(name));
        REGISTRY.add(type);
        return type;
    }
}
