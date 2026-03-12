package com.minelittlepony.unicopia.datagen;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.util.Identifier;

final class CustomStructurePools {
    static void bootstrapPools(Registerable<StructurePool> registerable) {
    }

    static void bootstrapProcessors(Registerable<StructureProcessorList> registerable) {
        registerExtra(registerable, StructureProcessorLists.FARM_PLAINS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(
                new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2F), AlwaysTrueRuleTest.INSTANCE, UBlocks.OATS.getDefaultState())
        ))));
        registerExtra(registerable, StructureProcessorLists.FARM_SAVANNA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(
                new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1F), AlwaysTrueRuleTest.INSTANCE, UBlocks.OATS.getDefaultState())
        ))));
    }

    static void register(Registerable<StructurePool> registerable, String name, StructurePool pool) {
        registerable.register(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Unicopia.id(name)), pool);
    }

    static void registerExtra(Registerable<StructurePool> registerable, String name, StructurePool pool) {
        registerable.register(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, name)), pool);
    }

    static void registerExtra(Registerable<StructureProcessorList> registerable, RegistryKey<StructureProcessorList> key, List<StructureProcessor> processors) {
        registerable.register(RegistryKey.of(RegistryKeys.PROCESSOR_LIST, Identifier.of(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, key.getValue().getPath())), new StructureProcessorList(processors));
    }
}
