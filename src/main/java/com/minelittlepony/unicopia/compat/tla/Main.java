package com.minelittlepony.unicopia.compat.tla;

import com.minelittlepony.unicopia.item.UItems;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.plugin.TlaApiPlugin;
import io.github.mattidragon.tlaapi.api.recipe.TlaStackComparison;

public class Main implements TlaApiPlugin {
    @Override
    public void register(PluginContext registry) {
        RecipeCategory.bootstrap(registry);
        registry.getItemComparisons().register(TlaStackComparison.compareComponents(),
                UItems.GEMSTONE, UItems.BOTCHED_GEM, UItems.MAGIC_STAFF, UItems.FILLED_JAR
        );
    }
}
