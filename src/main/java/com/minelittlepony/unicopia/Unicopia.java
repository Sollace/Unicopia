package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Optional;

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
import com.minelittlepony.unicopia.container.UScreenHandlers;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.entity.effect.UPotions;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.particle.UParticles;

public class Unicopia implements ModInitializer {
    public static final String DEFAULT_NAMESPACE = "unicopia";
    public static final Logger LOGGER = LogManager.getLogger();

    public static SidedAccess SIDE = Optional::empty;

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            CONFIG = new Config();
        }
        return CONFIG;
    }

    public static Identifier id(String name) {
        return new Identifier(DEFAULT_NAMESPACE, name);
    }

    @Override
    public void onInitialize() {
        Channel.bootstrap();
        UTags.bootstrap();
        UCriteria.bootstrap();
        UEntities.bootstrap();
        Commands.bootstrap();

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            ((BlockDestructionManager.Source)w).getDestructionManager().tick();
        });
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TreeTypeLoader.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(UEnchantments.POISONED_JOKE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TraitLoader.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(StateMapLoader.INSTANCE);

        UBlocks.bootstrap();
        UItems.bootstrap();
        UPotions.bootstrap();
        UParticles.bootstrap();
        USounds.bootstrap();
        Race.bootstrap();
        SpellType.bootstrap();
        Abilities.bootstrap();
        UScreenHandlers.bootstrap();
    }

    public interface SidedAccess {
        Optional<Pony> getPony();

        default Race getPlayerSpecies() {
            return getPony().map(Pony::getSpecies).orElse(Race.HUMAN);
        }
    }
}
