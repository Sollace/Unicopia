package com.minelittlepony.unicopia.world;

import com.minelittlepony.unicopia.mixin.CriterionsRegistry;
import com.minelittlepony.unicopia.world.advancement.BOHDeathCriterion;
import com.minelittlepony.unicopia.world.block.UBlocks;
import com.minelittlepony.unicopia.world.container.UContainers;
import com.minelittlepony.unicopia.world.item.UItems;
import com.minelittlepony.unicopia.world.recipe.URecipes;
import com.minelittlepony.unicopia.world.recipe.enchanting.Pages;
import com.minelittlepony.unicopia.world.structure.UStructures;

import net.fabricmc.fabric.api.loot.v1.FabricLootSupplier;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class UnicopiaWorld {
    public static void bootstrap() {
        UBlocks.bootstrap();
        UItems.bootstrap();
        UContainers.bootstrap();
        UStructures.bootstrap();
        URecipes.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Pages.instance());
        CriterionsRegistry.register(BOHDeathCriterion.INSTANCE);
        LootTableLoadingCallback.EVENT.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }

            Identifier modId = new Identifier("unicopiamc", id.getPath());
            LootTable table = manager.getTable(modId);
            if (table != LootTable.EMPTY) {
                supplier.withPools(((FabricLootSupplier)table).getPools());
            }
        });
    }
}
