package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapterLoader;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapters;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public record MsgServerResources (
        Map<Identifier, SpellTraits> traits,
        SpellbookChapters chapters,
        Map<Identifier, TreeTypeLoader.TreeTypeDef> treeTypes,
        CuttingRecipeDisplay.Grouping<CloudShapingRecipe> cloudCuttingRecipes,
        PonyDiets diets
    ) {
    public static final PacketCodec<RegistryByteBuf, MsgServerResources> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, SpellTraits.PACKET_CODEC), MsgServerResources::traits,
            SpellbookChapters.PACKET_CODEC, MsgServerResources::chapters,
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, TreeTypeLoader.TreeTypeDef.PACKET_CODEC), MsgServerResources::treeTypes,
            CuttingRecipeDisplay.Grouping.codec(), MsgServerResources::cloudCuttingRecipes,
            PonyDiets.PACKET_CODEC, MsgServerResources::diets,
            MsgServerResources::new
    );

    public MsgServerResources(MinecraftServer server) {
        this(
            SpellTraits.all(),
            SpellbookChapterLoader.INSTANCE.getChapters(server),
            TreeTypeLoader.INSTANCE.getEntries(),
            getCloudCuttingRecipes(server),
            PonyDiets.getInstance()
        );
    }

    static CuttingRecipeDisplay.Grouping<CloudShapingRecipe> getCloudCuttingRecipes(MinecraftServer server) {
        return new CuttingRecipeDisplay.Grouping<>(server.getRecipeManager().values().stream()
                .filter(recipe -> recipe.value().getType() == URecipes.CLOUD_SHAPING && recipe.value() instanceof CloudShapingRecipe)
                .map(recipe -> new CuttingRecipeDisplay.GroupEntry<CloudShapingRecipe>(
                    ((CloudShapingRecipe)recipe.value()).ingredient(),
                    new CuttingRecipeDisplay<>(((CloudShapingRecipe)recipe.value()).createResultDisplay(), Untyped.cast(Optional.of(recipe)))
            )).toList());
    }
}
