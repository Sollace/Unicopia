package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplier;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.advancement.BOHDeathCriterion;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.container.UContainers;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.mixin.CriterionsRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.structure.UStructures;

public class Unicopia implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

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
        Commands.bootstrap();
        UBlocks.bootstrap();
        UItems.bootstrap();
        UContainers.bootstrap();
        UStructures.bootstrap();
        URecipes.bootstrap();

        CriterionsRegistry.register(BOHDeathCriterion.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Pages.instance());
        WorldTickCallback.EVENT.register(AwaitTickQueue::tick);

        LootTableLoadingCallback.EVENT.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }

            Identifier modId = new Identifier("unicopiamc", id.getPath());
            LootTable table = manager.getSupplier(modId);
            if (table != LootTable.EMPTY) {
                supplier.withPools(((FabricLootSupplier)table).getPools());
            }
        });
    }
}
