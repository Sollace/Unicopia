package com.minelittlepony.unicopia;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;

import static net.minecraft.sound.SoundEvents.*;

public interface USounds {
    SoundEvent ENTITY_GENERIC_BUTTER_FINGERS = BLOCK_HONEY_BLOCK_SLIDE;

    SoundEvent ENTITY_PLAYER_CORRUPTION = PARTICLE_SOUL_ESCAPE;
    SoundEvent ENTITY_PLAYER_BATPONY_SCREECH = register("entity.player.batpony.screech");
    SoundEvent ENTITY_PLAYER_HIPPOGRIFF_SCREECH = register("entity.player.hippogriff.screech");
    SoundEvent ENTITY_PLAYER_HIPPOGRIFF_PECK = ENTITY_CHICKEN_STEP;
    SoundEvent ENTITY_PLAYER_REBOUND = register("entity.player.rebound");
    SoundEvent ENTITY_PLAYER_PEGASUS_WINGSFLAP = register("entity.player.pegasus.wingsflap");
    SoundEvent ENTITY_PLAYER_PEGASUS_FLYING = register("entity.player.pegasus.flying");
    SoundEvent ENTITY_PLAYER_PEGASUS_DASH = register("entity.player.pegasus.dash");
    SoundEvent ENTITY_PLAYER_PEGASUS_MOLT = register("entity.player.pegasus.molt");
    SoundEvent ENTITY_PLAYER_EARTHPONY_DASH = ENTITY_RAVAGER_STEP;
    SoundEvent ENTITY_PLAYER_CHANGELING_BUZZ = register("entity.player.changeling.buzz");
    SoundEvent ENTITY_PLAYER_CHANGELING_TRANSFORM = register("entity.player.changeling.transform");
    SoundEvent ENTITY_PLAYER_CHANGELING_FEED = ENTITY_GENERIC_DRINK;
    SoundEvent ENTITY_PLAYER_CHANGELING_CLIMB = ENTITY_CHICKEN_STEP;
    SoundEvent ENTITY_PLAYER_UNICORN_TELEPORT = register("entity.player.unicorn.teleport");
    SoundEvent ENTITY_PLAYER_KIRIN_RAGE = ENTITY_POLAR_BEAR_WARNING;
    SoundEvent ENTITY_PLAYER_KIRIN_RAGE_LOOP = register("entity.player.kirin.rage.loop");

    SoundEvent ENTITY_PLAYER_EARS_RINGING = register("entity.player.ears_ring");
    SoundEvent ENTITY_PLAYER_HEARTBEAT = register("entity.player.heartbeat");
    SoundEvent ENTITY_PLAYER_HEARTBEAT_LOOP = register("entity.player.heartbeat_loop");

    SoundEvent ENTITY_PLAYER_WOLOLO = register("entity.player.wololo");
    SoundEvent ENTITY_PLAYER_WHISTLE = register("entity.player.whistle");
    SoundEvent ENTITY_PLAYER_KICK = register("entity.player.kick");

    SoundEvent ENTITY_ARTEFACT_AMBIENT = register("entity.artefact.ambient");

    SoundEvent ENTITY_BUTTERFLY_HURT = register("entity.butterfly.hurt");

    SoundEvent ENTITY_TWITTERMITE_AMBIENT = register("entity.twittermite.ambient");
    SoundEvent ENTITY_TWITTERMITE_HURT = register("entity.twittermite.hurt");
    SoundEvent ENTITY_TWITTERMITE_DEATH = register("entity.twittermite.death");

    SoundEvent ENTITY_JAR_THROW = register("entity.jar.throw");

    SoundEvent ENTITY_CRYSTAL_HEART_ACTIVATE = register("entity.crystal_heart.activate");
    SoundEvent ENTITY_CRYSTAL_HEART_DEACTIVATE = register("entity.crystal_heart.deactivate");

    SoundEvent ENTITY_HOT_AIR_BALLOON_BOOST = ENTITY_GHAST_SHOOT;
    SoundEvent ENTITY_HOT_AIR_BALLOON_BURNER_FIRE = ENTITY_GHAST_SHOOT;
    SoundEvent ENTITY_HOT_AIR_BALLOON_STEP = BLOCK_WOOL_STEP;
    SoundEvent ENTITY_HOT_AIR_BALLOON_BASKET_STEP = BLOCK_BAMBOO_STEP;
    SoundEvent ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY = ITEM_ARMOR_EQUIP_LEATHER;
    SoundEvent ENTITY_HOT_AIR_BALLOON_EQUIP_BURNER = ENTITY_IRON_GOLEM_DAMAGE;

    SoundEvent ENTITY_SOMBRA_AMBIENT = register("entity.sombra.ambient");
    SoundEvent ENTITY_SOMBRA_LAUGH = register("entity.sombra.laugh");
    SoundEvent ENTITY_SOMBRA_SNICKER = register("entity.sombra.snicker");
    SoundEvent ENTITY_SOMBRA_SCARY = USounds.Vanilla.ENTITY_GHAST_AMBIENT;

    SoundEvent ENTITY_CRYSTAL_SHARDS_AMBIENT = BLOCK_AMETHYST_BLOCK_HIT;
    SoundEvent ENTITY_CRYSTAL_SHARDS_JOSTLE = BLOCK_AMETHYST_BLOCK_BREAK;

    SoundEvent ITEM_AMULET_CHARGING = register("item.amulet.charging");
    SoundEvent ITEM_AMULET_RECHARGE = register("item.amulet.recharge");

    SoundEvent ITEM_DRAGON_BREATH_SCROLL_USE = ITEM_FIRECHARGE_USE;
    SoundEvent ITEM_DRAGON_BREATH_ARRIVE = ITEM_FIRECHARGE_USE;

    SoundEvent ITEM_ICARUS_WINGS_PURIFY = register("item.icarus_wings.resonate");
    SoundEvent ITEM_ICARUS_WINGS_CORRUPT = register("item.icarus_wings.corrupted");
    SoundEvent ITEM_ICARUS_WINGS_WARN = register("item.icarus_wings.warn");
    SoundEvent ITEM_ICARUS_WINGS_EXHAUSTED = register("item.icarus_wings.break");

    SoundEvent ITEM_ALICORN_AMULET_CURSE = register("item.alicorn_amulet.curse");
    SoundEvent ITEM_ALICORN_AMULET_HALLUCINATION = register("item.alicorn_amulet.hallucination");
    SoundEvent ITEM_ALICORN_AMULET_AMBIENT = register("item.alicorn_amulet.ambient");

    SoundEvent ITEM_GROGAR_BELL_USE = BLOCK_BELL_USE;
    SoundEvent ITEM_GROGAR_BELL_STOP_USING = BLOCK_BELL_USE;
    SoundEvent ITEM_GROGAR_BELL_CHARGE = BLOCK_BELL_RESONATE;
    SoundEvent ITEM_GROGAR_BELL_DRAIN = ENTITY_GUARDIAN_ATTACK;

    SoundEvent ITEM_STAFF_STRIKE = ENTITY_PLAYER_ATTACK_CRIT;
    SoundEvent ITEM_MAGIC_STAFF_CHARGE = ENTITY_GUARDIAN_ATTACK;

    SoundEvent ITEM_ROCK_LAND = BLOCK_STONE_HIT;
    RegistryEntry.Reference<SoundEvent> ITEM_MUFFIN_BOUNCE = BLOCK_NOTE_BLOCK_BANJO;

    SoundEvent ITEM_SUNGLASSES_SHATTER = BLOCK_GLASS_BREAK;

    SoundEvent ITEM_APPLE_ROT = register("item.apple.rot");
    SoundEvent ITEM_BRACELET_SIGN = register("item.bracelet.sign");
    SoundEvent ITEM_MAGIC_AURA = register("item.magic.aura");

    SoundEvent BLOCK_CHITIN_AMBIENCE = register("block.chitin.ambience");
    SoundEvent BLOCK_SLIME_PUSTULE_POP = register("block.slime_pustule.pop");

    SoundEvent BLOCK_WEATHER_VANE_ROTATE = BLOCK_LANTERN_STEP;
    SoundEvent BLOCK_PIE_SLICE = BLOCK_BEEHIVE_SHEAR;
    SoundEvent BLOCK_PIE_SLICE_POP = ENTITY_ITEM_PICKUP;

    SoundEvent SPELL_CAST_FAIL = register("spell.cast.fail");
    SoundEvent SPELL_CAST_SUCCESS = register("spell.cast.success");
    SoundEvent SPELL_CAST_SHOOT = register("spell.cast.shoot");

    SoundEvent SPELL_ILLUSION_DISPERSE = register("spell.illusion.disperse");
    SoundEvent SPELL_FIRE_BOLT_SHOOT = register("spell.fire.shoot.bolt");
    SoundEvent SPELL_SHIELD_BURN_PROJECTILE = register("spell.shield.projectile.burn");
    SoundEvent SPELL_TRANSFORM_TRANSMUTE_ENTITY = register("spell.transform.transmute.entity");

    SoundEvent SPELL_AMBIENT = BLOCK_BEACON_AMBIENT;
    SoundEvent SPELL_MINDSWAP_SWAP = ENTITY_ZOMBIE_INFECT;
    SoundEvent SPELL_MINDSWAP_UNSWAP = ENTITY_ZOMBIE_VILLAGER_CURE;

    SoundEvent SPELL_BUBBLE_DISTURB = BLOCK_HONEY_BLOCK_STEP;
    SoundEvent SPELL_FIRE_CRACKLE = BLOCK_FURNACE_FIRE_CRACKLE;

    SoundEvent SPELL_NECROMANCY_ACTIVATE = BLOCK_BELL_USE;

    SoundEvent SPELL_DISPLACEMENT_TELEPORT = ENTITY_HUSK_CONVERTED_TO_ZOMBIE;

    SoundEvent ENCHANTMENT_CONSUMPTION_CONSUME = ENTITY_PLAYER_BURP;

    SoundEvent PARTICLE_RAINBOOM_THUNDER = ENTITY_LIGHTNING_BOLT_THUNDER;
    SoundEvent PARTICLE_RAINBOOM_REVERB = ENTITY_LIGHTNING_BOLT_THUNDER;

    SoundEvent AMBIENT_WIND_GUST = register("ambient.wind.gust");
    SoundEvent AMBIENT_DARK_VORTEX_MOOD = register("ambient.dark_vortex.mood");
    SoundEvent AMBIENT_DARK_VORTEX_ADDITIONS = register("ambient.dark_vortex.additions");

    SoundEvent GUI_ABILITY_FAIL = register("gui.ability.fail");
    SoundEvent GUI_SPELL_CRAFT_SUCCESS = register("gui.spellcraft.success");
    RegistryEntry.Reference<SoundEvent> GUI_SPELL_EQUIP = UI_BUTTON_CLICK;

    SoundEvent RECORD_CRUSADE = register("music_disc.crusade");
    SoundEvent RECORD_PET = register("music_disc.pet");
    SoundEvent RECORD_POPULAR = register("music_disc.popular");
    SoundEvent RECORD_FUNK = register("music_disc.funk");

    static SoundEvent register(String name) {
        Identifier id = Unicopia.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void bootstrap() {}

    static final class Vanilla extends SoundEvents {}
}
