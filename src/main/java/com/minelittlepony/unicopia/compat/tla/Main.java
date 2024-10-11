package com.minelittlepony.unicopia.compat.tla;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.plugin.TlaApiPlugin;
import io.github.mattidragon.tlaapi.api.recipe.TlaStackComparison;
import net.minecraft.util.Identifier;

public class Main implements TlaApiPlugin {
    static final Identifier WIDGETS = Unicopia.id("textures/gui/widgets.png");
    static final TextureConfig EMPTY_ARROW = TextureConfig.builder().texture(WIDGETS).uv(44, 0).size(24, 17).build();
    static final TextureConfig PLUS = TextureConfig.builder().texture(WIDGETS).size(13, 13).uv(82, 0).build();

    @Override
    public void register(PluginContext registry) {
        RecipeCategory.bootstrap(registry);
        registry.getItemComparisons().register(TlaStackComparison.compareComponents(),
                UItems.GEMSTONE, UItems.BOTCHED_GEM, UItems.MAGIC_STAFF, UItems.FILLED_JAR
        );
    }
}
