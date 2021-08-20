package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.entity.effect.UPotions;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;

public class Unicopia implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    public static SidedAccess SIDE = Optional::empty;

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            CONFIG = new Config();
        }
        return CONFIG;
    }

    public Unicopia() {
        getConfig();
    }

    @Override
    public void onInitialize() {
        Channel.bootstrap();
        UTags.bootstrap();
        UCriteria.bootstrap();
        UEntities.bootstrap();
        Commands.bootstrap();

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            AwaitTickQueue.tick(w);
            ((BlockDestructionManager.Source)w).getDestructionManager().tick();
        });
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TreeTypeLoader.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(UEnchantments.POISONED_JOKE);

        UItems.bootstrap();
        UPotions.bootstrap();
    }

    public interface SidedAccess {
        Optional<Pony> getPony();

        default Race getPlayerSpecies() {
            return getPony().map(Pony::getSpecies).orElse(Race.HUMAN);
        }
    }
}
