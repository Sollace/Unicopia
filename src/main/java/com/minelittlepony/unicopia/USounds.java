package com.minelittlepony.unicopia;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface USounds {
    SoundEvent AMBIENT_WIND_GUST = register("ambient.wind.gust");

    SoundEvent ENTITY_PLAYER_BATPONY_SCREECH = register("entity.player.batpony.screech");
    SoundEvent ENTITY_PLAYER_REBOUND = register("entity.player.rebound");
    SoundEvent ENTITY_PLAYER_PEGASUS_WINGSFLAP = register("entity.player.pegasus.wingsflap");
    SoundEvent ENTITY_PLAYER_PEGASUS_MOLT = register("entity.player.pegasus.molt");
    SoundEvent ENTITY_PLAYER_CHANGELING_BUZZ = register("entity.player.changeling.buzz");
    SoundEvent ENTITY_PLAYER_CHANGELING_TRANSFORM = register("entity.player.changeling.transform");
    SoundEvent ENTITY_PLAYER_UNICORN_TELEPORT = register("entity.player.unicorn.teleport");

    SoundEvent ENTITY_PLAYER_EARS_RINGING = register("entity.player.ears_ring");
    SoundEvent ENTITY_PLAYER_HEARTBEAT = register("entity.player.heartbeat");

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

    SoundEvent ITEM_AMULET_CHARGING = register("item.amulet.charging");
    SoundEvent ITEM_AMULET_RECHARGE = register("item.amulet.recharge");

    SoundEvent ITEM_ICARUS_WINGS_PURIFY = register("item.icarus_wings.resonate");
    SoundEvent ITEM_ICARUS_WINGS_CORRUPT = register("item.icarus_wings.corrupted");
    SoundEvent ITEM_ICARUS_WINGS_WARN = register("item.icarus_wings.warn");
    SoundEvent ITEM_ICARUS_WINGS_EXHAUSTED = register("item.icarus_wings.break");

    SoundEvent ITEM_ALICORN_AMULET_CURSE = register("item.alicorn_amulet.curse");
    SoundEvent ITEM_ALICORN_AMULET_HALLUCINATION = register("item.alicorn_amulet.hallucination");
    SoundEvent ITEM_ALICORN_AMULET_AMBIENT = register("item.alicorn_amulet.ambient");

    SoundEvent ITEM_APPLE_ROT = register("item.apple.rot");
    SoundEvent ITEM_BRACELET_SIGN = register("item.bracelet.sign");
    SoundEvent ITEM_MAGIC_AURA = register("item.magic.aura");

    SoundEvent SPELL_CAST_FAIL = register("spell.cast.fail");
    SoundEvent SPELL_CAST_SUCCESS = register("spell.cast.success");
    SoundEvent SPELL_CAST_SHOOT = register("spell.cast.shoot");

    SoundEvent SPELL_ILLUSION_DISPERSE = register("spell.illusion.disperse");
    SoundEvent SPELL_FIRE_BOLT_SHOOT = register("spell.fire.shoot.bolt");
    SoundEvent SPELL_SHIELD_BURN_PROJECTILE = register("spell.shield.projectile.burn");
    SoundEvent SPELL_TRANSFORM_TRANSMUTE_ENTITY = register("spell.transform.transmute.entity");

    SoundEvent AMBIENT_DARK_VORTEX_MOOD = register("ambient.dark_vortex.mood");
    SoundEvent AMBIENT_DARK_VORTEX_ADDITIONS = register("ambient.dark_vortex.additions");

    SoundEvent GUI_ABILITY_FAIL = register("gui.ability.fail");
    SoundEvent GUI_SPELL_CRAFT_SUCCESS = register("gui.spellcraft.success");

    SoundEvent RECORD_CRUSADE = register("music_disc.crusade");
    SoundEvent RECORD_PET = register("music_disc.pet");
    SoundEvent RECORD_POPULAR = register("music_disc.popular");
    SoundEvent RECORD_FUNK = register("music_disc.funk");

    static SoundEvent register(String name) {
        Identifier id = new Identifier("unicopia", name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    static void bootstrap() {}
}
