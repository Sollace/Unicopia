package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

public class UItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public UItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, BlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void configure(WrapperLookup arg) {
        copyBlockTags();
        getOrCreateTagBuilder(ItemTags.BOOKSHELF_BOOKS).add(UItems.SPELLBOOK);
        getOrCreateTagBuilder(ItemTags.BEDS).add(UItems.CLOTH_BED, UItems.CLOUD_BED);

        getOrCreateTagBuilder(ItemTags.CHEST_BOATS).add(UItems.PALM_CHEST_BOAT);
        getOrCreateTagBuilder(ItemTags.BOATS).add(UItems.PALM_BOAT);
        getOrCreateTagBuilder(ItemTags.MUSIC_DISCS).add(UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR);
        getOrCreateTagBuilder(ItemTags.CREEPER_DROP_MUSIC_DISCS).add(UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR);

        getOrCreateTagBuilder(ItemTags.SIGNS).add(UBlocks.PALM_SIGN.asItem());
        getOrCreateTagBuilder(ItemTags.HANGING_SIGNS).add(UBlocks.PALM_HANGING_SIGN.asItem());

        getOrCreateTagBuilder(UTags.HORSE_SHOES).add(UItems.IRON_HORSE_SHOE, UItems.GOLDEN_HORSE_SHOE, UItems.COPPER_HORSE_SHOE, UItems.NETHERITE_HORSE_SHOE);
        getOrCreateTagBuilder(UTags.POLEARMS).add(UItems.WOODEN_POLEARM, UItems.STONE_POLEARM, UItems.IRON_POLEARM, UItems.GOLDEN_POLEARM, UItems.DIAMOND_POLEARM, UItems.NETHERITE_POLEARM);

        getOrCreateTagBuilder(ItemTags.TOOLS).addTag(UTags.HORSE_SHOES).addTag(UTags.POLEARMS);
    }

    private void copyBlockTags() {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
    }
}
