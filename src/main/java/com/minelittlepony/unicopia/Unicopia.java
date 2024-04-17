package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.state.StateMapLoader;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader;
import com.minelittlepony.unicopia.container.UScreenHandlers;
import com.minelittlepony.unicopia.diet.DietsLoader;
import com.minelittlepony.unicopia.diet.affliction.AfflictionType;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.effect.UPotions;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.NocturnalSleepManager;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

public class Unicopia implements ModInitializer {
    public static final String DEFAULT_NAMESPACE = "unicopia";
    public static final Logger LOGGER = LogManager.getLogger();

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            CONFIG = new Config();
            try {
                CONFIG.load();
            } catch (IllegalStateException ignored) {
                CONFIG.save();
            }
        }
        return CONFIG;
    }

    public static Identifier id(String name) {
        return new Identifier(DEFAULT_NAMESPACE, name);
    }

    @Override
    public void onInitialize() {
        Channel.bootstrap();
        UCriteria.bootstrap();
        UEntities.bootstrap();
        Commands.bootstrap();
        TrinketsDelegate.getInstance(null).bootstrap();

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            ((BlockDestructionManager.Source)w).getDestructionManager().tick();
            ZapAppleStageStore.get(w).tick();
            WeatherConditions.get(w).tick();
            if (Debug.SPELLBOOK_CHAPTERS) {
                SpellbookChapterLoader.INSTANCE.sendUpdate(w.getServer());
            }
            if (Debug.CHECK_GAME_VALUES) {
                Debug.runTests(w);
            }
        });
        NocturnalSleepManager.bootstrap();

        registerServerDataReloaders(ResourceManagerHelper.get(ResourceType.SERVER_DATA));

        UGameEvents.bootstrap();
        UBlocks.bootstrap();
        UPOIs.bootstrap();
        UItems.bootstrap();
        UPotions.bootstrap();
        UParticles.bootstrap();
        USounds.bootstrap();
        Race.bootstrap();
        SpellType.bootstrap();
        AfflictionType.bootstrap();
        Abilities.bootstrap();
        UScreenHandlers.bootstrap();
        UWorldGen.bootstrap();
        UGameRules.bootstrap();
        UDamageTypes.bootstrap();
    }

    private void registerServerDataReloaders(ResourceManagerHelper registry) {
        registry.registerReloadListener(TreeTypeLoader.INSTANCE);
        registry.registerReloadListener(UEnchantments.POISONED_JOKE);
        registry.registerReloadListener(new TraitLoader());
        registry.registerReloadListener(StateMapLoader.INSTANCE);
        registry.registerReloadListener(SpellbookChapterLoader.INSTANCE);
        registry.registerReloadListener(new DietsLoader());
    }
}
