package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.TraitMap;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.TraitSet;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class VanillaTraitsGenerator extends TraitsGenerator {

    @Override
    public void generate(WrapperLookup registries, BiConsumer<Identifier, TraitStream> exporter) {
        generateOverworld(registries, exporter);
        generateNether(registries, exporter);
    }

    private void generateOverworld(WrapperLookup registries, BiConsumer<Identifier, TraitStream> exporter) {
        var items = registries.getOrThrow(RegistryKeys.ITEM);
        var appleTraits = SpellTraits.of(Trait.LIFE, 3);

        exporter.accept(Identifier.ofVanilla("blocks/overworld/cracked_stone"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 2).with(Trait.POWER, -1).with(Trait.EARTH, 5).with(Trait.ORDER, -1))
                .apply(b -> applyItemsWhere(b, items, path -> path.startsWith("cracked_")))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/infested_stone"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 2).with(Trait.EARTH, 6).with(Trait.BLOOD, 1))
                .apply(b -> {
                    items.streamEntries().forEach(entry -> {
                        if (entry.value() instanceof BlockItem item && item.getBlock() instanceof InfestedBlock) {
                            b.item(item);
                        }
                    });
                    return b;
                })
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/unusual_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 1).with(Trait.CHAOS, 8))
                .apply(b -> applyItemsWhere(b, items, path -> path.startsWith("purpur_")))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/living_matter"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 3).with(Trait.EARTH, 1))
                .item(Items.SWEET_BERRIES)
                .item(Items.SUGAR_CANE)
                .item(Items.MYCELIUM)
                .item(Items.BAMBOO)
                .item(Items.FERN)
                .item(Items.MELON)
                .item(Items.CHORUS_FLOWER)
                .item(Items.BROWN_MUSHROOM)
                .item(Items.POTATO)
                .tag(ItemTags.LEAVES)
                .item(Items.SMALL_DRIPLEAF)
                .item(Items.CACTUS)
                .item(Items.ROSE_BUSH)
                .item(Items.BIG_DRIPLEAF)
                .item(Items.SPORE_BLOSSOM)
                .item(Items.GLOW_LICHEN)
                .item(Items.MOSS_BLOCK)
                .item(Items.SEAGRASS)
                .item(Items.TALL_GRASS)
                .item(Items.LILY_PAD)
                .tag(UConventionalTags.Items.CORALS)
                .item(Items.WITHER_ROSE)
                .item(Items.DRAGON_EGG)
                .item(Items.TURTLE_EGG)
                .item(Items.SUNFLOWER)
                .tag(ItemTags.SAPLINGS)
                .item(Items.PUMPKIN)
                .tag(ItemTags.FLOWERS)
                .item(Items.AZALEA)
                .item(Items.GLOW_BERRIES)
                .item(Items.CARROT)
                .item(Items.SHORT_GRASS)
                .item(Items.VINE)
                .item(Items.AZALEA_LEAVES)
                .tag(UConventionalTags.Items.CORAL_FANS)
                .item(Items.LARGE_FERN)
                .tag(UConventionalTags.Items.CORAL_BLOCKS)
                .item(Items.LILAC)
                .item(Items.HANGING_ROOTS)
                .item(Items.PEONY)
                .item(Items.RED_MUSHROOM)
                .item(Items.FLOWERING_AZALEA)
                .item(Items.SEA_PICKLE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/dirt"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 6))
                .tag(ItemTags.DIRT)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/intellectual_objects_studicious"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 9).with(Trait.LIFE, 3).with(Trait.WATER, 9))
                .item(Items.FROGSPAWN)
                .item(Items.PEARLESCENT_FROGLIGHT)
                .item(Items.OCHRE_FROGLIGHT)
                .item(Items.VERDANT_FROGLIGHT)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/organic_dead_matter"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 1).with(Trait.ROT, 3))
                .item(Items.SKELETON_SKULL)
                .item(Items.BUNDLE)
                .item(Items.WITHER_SKELETON_SKULL)
                .item(Items.CREEPER_HEAD)
                .item(Items.DRAGON_HEAD)
                .item(Items.DRIED_KELP_BLOCK)
                .item(Items.PLAYER_HEAD)
                .item(Items.PIGLIN_HEAD)
                .item(Items.ZOMBIE_HEAD)
                .apply(b -> applyItemsWhere(b, items, path -> {
                    return path.startsWith("dead_");
                }))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/glass_materials"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 3).with(Trait.KNOWLEDGE, 2))
                .tag(ConventionalItemTags.GLASS_BLOCKS)
                .tag(ConventionalItemTags.GLASS_PANES)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/dirt_worked"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 3).with(Trait.EARTH, 5))
                .item(Items.MUD_BRICKS)
                .item(Items.REINFORCED_DEEPSLATE)
                .item(Items.DECORATED_POT)
                .item(Items.FARMLAND)
                .item(Items.PACKED_MUD)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/organic_plant_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 2))
                .item(Items.BROWN_MUSHROOM_BLOCK)
                .item(Items.CHORUS_PLANT)
                .item(Items.MOSSY_COBBLESTONE)
                .item(Items.PAPER)
                .item(Items.BEEHIVE)
                .item(Items.MUSHROOM_STEM)
                .tag(ItemTags.BAMBOO_BLOCKS)
                .item(Items.SLIME_BLOCK)
                .item(Items.CAKE)
                .item(Items.COBWEB)
                .item(Items.RED_MUSHROOM_BLOCK)
                .item(Items.BOWL)
                .item(Items.KELP)
                .tag(ItemTags.WOODEN_SLABS)
                .item(Items.MANGROVE_ROOTS)
                .tag(ItemTags.WOODEN_STAIRS)
                .item(Items.HONEYCOMB_BLOCK)
                .tag(ItemTags.PLANKS)
                .tag(ItemTags.LOGS_THAT_BURN)
                .item(Items.MOSS_CARPET)
                .item(Items.SPONGE)
                .item(Items.BAMBOO_MOSAIC)
                .tag(ItemTags.WOODEN_FENCES)
                .item(Items.HONEY_BLOCK)
                .item(Items.BEE_NEST)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/materials_from_the_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 2).with(Trait.POWER, -0.5F).with(Trait.EARTH, 1))
                .item(Items.DEEPSLATE_GOLD_ORE)
                .item(Items.QUARTZ_PILLAR)
                .item(Items.LAPIS_BLOCK)
                .item(Items.COARSE_DIRT)
                .item(Items.PODZOL)
                .item(Items.ANVIL)
                .item(Items.CRYING_OBSIDIAN)
                .item(Items.AMETHYST_CLUSTER)
                .item(Items.PRISMARINE_BRICKS)
                .item(Items.COAL_ORE)
                .item(Items.DEEPSLATE_IRON_ORE)
                .item(Items.DARK_PRISMARINE)
                .item(Items.COPPER_ORE)
                .item(Items.MAGMA_BLOCK)
                .item(Items.CHISELED_QUARTZ_BLOCK)
                .item(Items.BONE_BLOCK)
                .item(Items.IRON_ORE)
                .item(Items.DEEPSLATE_COPPER_ORE)
                .item(Items.MEDIUM_AMETHYST_BUD)
                .item(Items.EMERALD_ORE)
                .item(Items.RAW_COPPER_BLOCK)
                .item(Items.LAPIS_ORE)
                .item(Items.PRISMARINE_BRICK_STAIRS)
                .item(Items.DEEPSLATE_COAL_ORE)
                .item(Items.RAW_GOLD_BLOCK)
                .item(Items.AMETHYST_BLOCK)
                .item(Items.DEEPSLATE_EMERALD_ORE)
                .item(Items.POINTED_DRIPSTONE)
                .item(Items.IRON_BARS)
                .item(Items.DEEPSLATE_DIAMOND_ORE)
                .item(Items.BRICK_SLAB)
                .item(Items.GOLD_ORE)
                .item(Items.BRICKS)
                .item(Items.REDSTONE_ORE)
                .item(Items.ROOTED_DIRT)
                .item(Items.DEEPSLATE_LAPIS_ORE)
                .item(Items.QUARTZ_STAIRS)
                .item(Items.PETRIFIED_OAK_SLAB)
                .item(Items.LARGE_AMETHYST_BUD)
                .item(Items.CHAIN)
                .item(Items.PRISMARINE_STAIRS)
                .item(Items.DAMAGED_ANVIL)
                .item(Items.QUARTZ_BRICKS)
                .item(Items.QUARTZ_BLOCK)
                .item(Items.PRISMARINE)
                .item(Items.GRASS_BLOCK)
                .item(Items.CHIPPED_ANVIL)
                .item(Items.DIRT_PATH)
                .item(Items.OBSIDIAN)
                .item(Items.SMALL_AMETHYST_BUD)
                .item(Items.DIAMOND_ORE)
                .item(Items.DIRT)
                .item(Items.BUDDING_AMETHYST)
                .item(Items.DARK_PRISMARINE_STAIRS)
                .item(Items.CLAY)
                .item(Items.RAW_IRON_BLOCK)
                .item(Items.DEEPSLATE_REDSTONE_ORE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/refined_rocks_and_rock_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 5).with(Trait.KNOWLEDGE, 2).with(Trait.POWER, -2).with(Trait.EARTH, 2))
                .apply(b -> applyItemsWhere(b, items, path -> {
                    return path.startsWith("polished_");
                }))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/refined_ores"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 1).with(Trait.KNOWLEDGE, 2).with(Trait.EARTH, 1))
                .apply(b -> {
                    items.streamEntries().forEach(entry -> {
                        if (entry.value() instanceof BlockItem item) {
                            var block = item.getBlock();
                            if (HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().containsKey(block)
                                    || HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().containsValue(block)
                                    || Oxidizable.OXIDATION_LEVEL_DECREASES.get().containsKey(block)
                                    || Oxidizable.OXIDATION_LEVEL_DECREASES.get().containsValue(block)) {
                                b.item(item);
                            }
                        }
                    });
                    return b;
                })
                .item(Items.DIAMOND_BLOCK)
                .item(Items.IRON_BLOCK)
                .item(Items.NETHERITE_BLOCK)
                .tag(ConventionalItemTags.ORES)
                .item(Items.EMERALD_BLOCK)
                .item(Items.GOLD_BLOCK)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/soft_and_kind"), TraitSet.builder(SpellTraits.of(Trait.KINDNESS, 3))
                .tag(ItemTags.BEDS)
                .tag(ItemTags.WOOL)
                .tag(ItemTags.WOOL_CARPETS)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/mechanical_powered"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 1).with(Trait.POWER, 4))
                .item(Items.REDSTONE_BLOCK)
                .item(Items.REDSTONE)
                .item(Items.ACTIVATOR_RAIL)
                .item(Items.REDSTONE_TORCH)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/energised"), TraitSet.builder(SpellTraits.of(Trait.POWER, 4))
                .item(Items.COAL_BLOCK)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/conglomerate_artificial_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 1).with(Trait.CHAOS, 1))
                .tag(ConventionalItemTags.COBBLESTONES)
                .tag(ConventionalItemTags.DEEPSLATE_COBBLESTONES)
                .tag(ConventionalItemTags.UNCOLORED_SANDSTONE_BLOCKS)
                .tag(ItemTags.STAIRS)
                .tag(ItemTags.WALLS)
                .tag(ItemTags.SLABS)
                .tag(ConventionalItemTags.UNCOLORED_SANDSTONE_STAIRS)
                .tag(ConventionalItemTags.UNCOLORED_SANDSTONE_SLABS)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/rocks_and_rock_derived_with_life"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 2).with(Trait.LIFE, 1).with(Trait.POWER, -2).with(Trait.EARTH, 1))
                .tag(ConventionalItemTags.MOSSY_COBBLESTONES)
                .apply(b -> applyItemsWhere(b, items, path -> {
                    return path.startsWith("mossy_stone_");
                }))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/conglomerate_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 4).with(Trait.EARTH, 1).with(Trait.ORDER, 1))
                .tag(ItemTags.TERRACOTTA)
                .tag(ConventionalItemTags.GLAZED_TERRACOTTAS)
                .tag(ConventionalItemTags.CONCRETES)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/mechanical_with_organic_components"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 1).with(Trait.LIFE, 1).with(Trait.EARTH, 1).with(Trait.ORDER, 1))
                .tag(ConventionalItemTags.SHULKER_BOXES)
                .item(Items.BOOKSHELF)
                .tag(ItemTags.WOODEN_DOORS)
                .item(Items.BARREL)
                .item(Items.TRAPPED_CHEST)
                .item(Items.CRAFTING_TABLE)
                .item(Items.SCULK_SENSOR)
                .item(Items.COMPOSTER)
                .tag(ItemTags.WOODEN_PRESSURE_PLATES)
                .item(Items.TNT)
                .tag(ConventionalItemTags.CHESTS)
                .tag(ItemTags.FENCE_GATES)
                .item(Items.CARVED_PUMPKIN)
                .item(Items.HAY_BLOCK)
                .item(Items.JACK_O_LANTERN)
                .tag(ItemTags.WOODEN_TRAPDOORS)
                .item(Items.LADDER)
                .tag(ItemTags.WOODEN_BUTTONS)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/loose_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 1).with(Trait.ORDER, -1).with(Trait.CHAOS, 2))
                .tag(ConventionalItemTags.CONCRETE_POWDERS)
                .item(Items.SUSPICIOUS_GRAVEL)
                .item(Items.GRAVEL)
                .tag(ItemTags.SAND)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/mechanical"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.POWER, 0.5F).with(Trait.ORDER, 1))
                .item(Items.STONE_BUTTON)
                .item(Items.ENCHANTING_TABLE)
                .item(Items.GRINDSTONE)
                .item(Items.TRIPWIRE_HOOK)
                .item(Items.BEACON)
                .item(Items.CARTOGRAPHY_TABLE)
                .item(Items.END_ROD)
                .apply(b -> applyItemsWhere(b, items, path -> path.endsWith("_pressure_plate") || path.endsWith("rail")))
                .item(Items.RESPAWN_ANCHOR)
                .item(Items.SCAFFOLDING)
                .item(Items.STONECUTTER)
                .item(Items.DISPENSER)
                .item(Items.BELL)
                .item(Items.REPEATER)
                .item(Items.REDSTONE_LAMP)
                .item(Items.LODESTONE)
                .item(Items.BREWING_STAND)
                .item(Items.LECTERN)
                .item(Items.CAULDRON)
                .item(Items.SMOKER)
                .item(Items.LIGHTNING_ROD)
                .item(Items.LEVER)
                .item(Items.BLAST_FURNACE)
                .item(Items.STICKY_PISTON)
                .item(Items.POLISHED_BLACKSTONE_BUTTON)
                .item(Items.SOUL_LANTERN)
                .item(Items.HOPPER)
                .item(Items.SMITHING_TABLE)
                .item(Items.DROPPER)
                .item(Items.PISTON)
                .item(Items.FURNACE)
                .item(Items.IRON_DOOR)
                .item(Items.NOTE_BLOCK)
                .item(Items.TORCH)
                .item(Items.DAYLIGHT_DETECTOR)
                .item(Items.IRON_TRAPDOOR)
                .item(Items.COMPARATOR)
                .item(Items.ENDER_CHEST)
                .item(Items.CONDUIT)
                .item(Items.FLOWER_POT)
                .item(Items.DETECTOR_RAIL)
                .item(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                .item(Items.TARGET)
                .item(Items.FLETCHING_TABLE)
                .item(Items.OBSERVER)
                .item(Items.JUKEBOX)
                .item(Items.LANTERN)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/skulk"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 2).with(Trait.DARKNESS, 11).with(Trait.BLOOD, 1))
                .item(Items.SCULK_CATALYST)
                .item(Items.SCULK)
                .item(Items.SCULK_SHRIEKER)
                .item(Items.SCULK_VEIN)
                .item(Items.ECHO_SHARD)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/light_emitting_materials"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 2).with(Trait.FIRE, 1))
                .tag(ItemTags.CANDLES)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/rocks_and_rock_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 4).with(Trait.POWER, -1).with(Trait.EARTH, 4))
                .tag(ItemTags.STONE_CRAFTING_MATERIALS)
                .tag(ItemTags.STONE_TOOL_MATERIALS)
                .tag(ConventionalItemTags.STONES)
                .tag(ItemTags.STONE_BRICKS)
                .tag(ItemTags.WALLS)
                .apply(b -> {
                    items.streamEntries().forEach(entry -> {
                        if (entry.value() instanceof BlockItem item) {
                            String path = entry.registryKey().getValue().getPath();
                            if (path.endsWith("_slab") || path.endsWith("_stairs")) {
                                var block = item.getBlock();
                                if (block.getDefaultState().getSoundGroup().getStepSound().id().getPath().contains("stone")) {
                                    b.item(item);
                                }
                            }
                        }
                    });
                    return b;
                })

                .item(Items.SMOOTH_STONE)
                .item(Items.SMOOTH_QUARTZ)
                .item(Items.END_STONE)
                .item(Items.CALCITE)
                .item(Items.TUFF)
                .item(Items.DIORITE)
                .item(Items.STONE_BRICKS)
                .item(Items.ANDESITE)
                .item(Items.GRANITE)

                .item(Items.PRISMARINE_WALL)
                .item(Items.END_STONE_BRICKS)

                .item(Items.SMOOTH_SANDSTONE)
                .item(Items.DEEPSLATE_TILES)

                .item(Items.DEEPSLATE)
                .item(Items.DRIPSTONE_BLOCK)
                .item(Items.DEEPSLATE_BRICKS)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/organic_plant_derived_dark"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 2).with(Trait.DARKNESS, 1))
                .tag(ItemTags.DARK_OAK_LOGS)
                .item(Items.STRIPPED_DARK_OAK_LOG)
                .item(Items.STRIPPED_DARK_OAK_WOOD)
                .apply(b -> applyItemsWhere(b, items, path -> {
                    return path.startsWith("dark_oak_") && !(path.contains("_log") || path.contains("_wood"));
                }))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/skulk_worked"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 18).with(Trait.EARTH, 2).with(Trait.DARKNESS, 11).with(Trait.BLOOD, 1))
                .item(Items.CALIBRATED_SCULK_SENSOR)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/organic_wet"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 2).with(Trait.WATER, 9))
                .item(Items.WET_SPONGE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/intellectual_worked"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 3).with(Trait.KNOWLEDGE, 9).with(Trait.ORDER, 3))
                .item(Items.CHISELED_BOOKSHELF)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/frozen_materials"), TraitSet.builder(SpellTraits.builder().with(Trait.ICE, 3).with(Trait.WATER, 3))
                .item(Items.PACKED_ICE)
                .item(Items.SNOW)
                .item(Items.SNOW_BLOCK)
                .item(Items.ICE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/overworld/firey_matter"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 1).with(Trait.FIRE, 6))
                .item(Items.SOUL_CAMPFIRE)
                .item(Items.CAMPFIRE)
                .build());

        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects_ordered"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 9).with(Trait.ORDER, 9))
                .apply(b -> {
                    items.streamEntries().forEach(entry -> {
                        if (entry.value() instanceof MinecartItem item) {
                            b.item(item);
                        }
                    });
                    return b;
                })
                .tag(ConventionalItemTags.EMPTY_BUCKETS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/clothing_selfish"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.EARTH, 0.5F).with(Trait.GENEROSITY, -1))
                .item(Items.DIAMOND_CHESTPLATE)
                .item(Items.GOLDEN_LEGGINGS)
                .item(Items.GOLDEN_CHESTPLATE)
                .item(Items.DIAMOND_HORSE_ARMOR)
                .item(Items.GOLDEN_HELMET)
                .item(Items.DIAMOND_LEGGINGS)
                .item(Items.DIAMOND_HELMET)
                .item(Items.GOLDEN_BOOTS)
                .item(Items.DIAMOND_BOOTS)
                .item(Items.GOLDEN_HORSE_ARMOR)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_plant_based"), TraitMap.builder()
                .tag(UTags.Items.FRESH_APPLES, appleTraits)
                .item(Items.BREAD, SpellTraits.builder().with(Trait.EARTH, 3).with(Trait.KINDNESS, 1))
                .item(Items.CARROT_ON_A_STICK, SpellTraits.of(Trait.CHAOS, 1))
                .item(Items.MUSHROOM_STEW, SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.ORDER, 2))
                .item(Items.CHORUS_FRUIT, SpellTraits.of(Trait.CHAOS, 2))
                .item(Items.POPPED_CHORUS_FRUIT, SpellTraits.of(Trait.CHAOS, 3))
                .item(Items.BEETROOT, SpellTraits.builder().with(Trait.STRENGTH, 1).with(Trait.EARTH, 1))
                .item(Items.PUMPKIN_PIE, SpellTraits.of(Trait.HAPPINESS, 2))
                .item(Items.DRIED_KELP, SpellTraits.builder().with(Trait.LIFE, -1).with(Trait.ROT, 0.03F))
                .item(Items.BEETROOT_SOUP, SpellTraits.of(Trait.EARTH, 1))
                .item(Items.SUSPICIOUS_STEW, SpellTraits.builder().with(Trait.CHAOS, 3).with(Trait.HAPPINESS, -1))
                .item(Items.BAKED_POTATO, SpellTraits.of(Trait.EARTH, 2))
                .item(Items.POISONOUS_POTATO, SpellTraits.builder().with(Trait.EARTH, 2).with(Trait.POISON, 1))
                .item(Items.MELON_SLICE, SpellTraits.of(Trait.LIFE, 1))
                .item(Items.COOKIE, SpellTraits.of(Trait.HAPPINESS, 6))
                .tag(UConventionalTags.Items.GRAIN, SpellTraits.builder().with(Trait.LIFE, 3).with(Trait.EARTH, 3))
                .tag(UTags.Items.LOW_QUALITY_SEA_VEGETABLES, SpellTraits.builder().with(Trait.LIFE, 2).with(Trait.WATER, 10))
                .tag(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES, SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.WATER, 10).with(Trait.HAPPINESS, 2))
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/from_the_ground"), TraitSet.builder(SpellTraits.of(Trait.EARTH, 4))
                .item(Items.PRISMARINE_SHARD)
                .item(Items.BRICK)
                .item(Items.CLAY_BALL)
                .item(Items.PRISMARINE_CRYSTALS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/banners"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 4).with(Trait.ORDER, 1).with(Trait.HAPPINESS, 1))
                .tag(ItemTags.BANNERS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_raw_meat_from_sea"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.WATER, 5).with(Trait.FAMINE, -2))
                .tag(ConventionalItemTags.RAW_FISH_FOODS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_raw_meat_poisoned"), TraitSet.builder(SpellTraits.builder().with(Trait.FAMINE, 2).with(Trait.POISON, 4).with(Trait.BLOOD, 1))
                .item(Items.ROTTEN_FLESH)
                .item(Items.SPIDER_EYE)
                .tag(UTags.Items.FORAGE_NAUSEATING)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/special"), TraitMap.builder()
                .item(Items.NETHER_STAR, SpellTraits.of(Trait.POWER, 19))
                .item(Items.ARMOR_STAND, SpellTraits.of(Trait.POWER, 9))
                .item(Items.NAME_TAG, SpellTraits.of(Trait.KNOWLEDGE, 10))
                .item(Items.BOOK, SpellTraits.of(Trait.KNOWLEDGE, 1))
                .item(Items.SHIELD, SpellTraits.of(Trait.STRENGTH, 15))
                .item(Items.SUGAR, SpellTraits.of(Trait.HAPPINESS, 1))
                .item(Items.TOTEM_OF_UNDYING, SpellTraits.of(Trait.GENEROSITY, 16))
                .item(Items.HEART_OF_THE_SEA, SpellTraits.of(Trait.KINDNESS, 17))
                .item(Items.ELYTRA, SpellTraits.builder().with(Trait.FOCUS, 2).with(Trait.POWER, 1).with(Trait.AIR, 5))
                .item(Items.TURTLE_HELMET, SpellTraits.builder().with(Trait.STRENGTH, 4).with(Trait.DARKNESS, 1))
                .item(Items.STICK, SpellTraits.of(Trait.EARTH, 1))
                .item(Items.SLIME_BALL, SpellTraits.of(Trait.ROT, 2))
                .item(Items.INK_SAC, SpellTraits.of(Trait.DARKNESS, 4))
                .item(Items.GLOW_INK_SAC, SpellTraits.builder().with(Trait.FOCUS, 1).with(Trait.CHAOS, 3))
                .item(Items.NAUTILUS_SHELL, SpellTraits.builder().with(Trait.LIFE, 3).with(Trait.WATER, 6))
                .item(Items.DISC_FRAGMENT_5, SpellTraits.builder().with(Trait.KNOWLEDGE, 1).with(Trait.DARKNESS, 1))
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_fruits"), TraitSet.builder(SpellTraits.builder().with(Trait.WATER, 2).with(Trait.KINDNESS, 3).with(Trait.HAPPINESS, 9))
                .tag(ConventionalItemTags.FRUIT_FOODS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/from_the_sky_with_kindness"), TraitSet.builder(SpellTraits.builder().with(Trait.AIR, 9).with(Trait.KINDNESS, 8))
                .tag(ConventionalItemTags.FEATHERS)
                .tag(UTags.Items.MAGIC_FEATHERS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_water"), TraitSet.builder(SpellTraits.of(Trait.WATER, 7))
                .tag(ConventionalItemTags.WATER_BUCKETS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/organic_plant_derived_artificial"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.ORDER, 1))
                .tag(ItemTags.CHEST_BOATS)
                .tag(ItemTags.BOATS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/uplifting_trinkets"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 2).with(Trait.HAPPINESS, 7))
                .tag(ConventionalItemTags.MUSIC_DISCS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/weapons"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 9).with(Trait.ORDER, -2).with(Trait.KINDNESS, -8).with(Trait.BLOOD, 2))
                .item(Items.CROSSBOW)
                .tag(ItemTags.SWORDS)
                .item(Items.TRIDENT)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_ice"), TraitSet.builder(SpellTraits.builder().with(Trait.ICE, 3).with(Trait.WATER, 6))
                .item(Items.POWDER_SNOW_BUCKET)
                .item(Items.SNOWBALL)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/from_the_sky"), TraitSet.builder(SpellTraits.of(Trait.AIR, 4))
                .item(Items.TURTLE_SCUTE)
                .tag(ItemTags.ARROWS)
                .item(Items.DRAGON_BREATH)
                .item(Items.LINGERING_POTION)
                .item(Items.END_CRYSTAL)
                .item(Items.PHANTOM_MEMBRANE)
                .item(Items.SPLASH_POTION)
                .item(Items.FISHING_ROD)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_flame_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 2).with(Trait.FIRE, 3))
                .item(Items.COAL)
                .item(Items.FLINT_AND_STEEL)
                .item(Items.FLINT)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects_unusual"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 7).with(Trait.CHAOS, 1))
                .item(Items.ENDER_PEARL)
                .item(Items.GLOW_ITEM_FRAME)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects_studicious"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 9).with(Trait.ORDER, 3))
                .tag(ItemTags.SIGNS)
                .tag(ItemTags.HANGING_SIGNS)
                .item(Items.SPYGLASS)
                .tag(ItemTags.DECORATED_POT_SHERDS)
                .item(Items.MAP)
                .item(Items.ITEM_FRAME)
                .item(Items.COMPASS)
                .item(Items.WRITTEN_BOOK)
                .item(Items.CLOCK)
                .apply(b -> {
                    items.streamEntries().forEach(entry -> {
                        if (entry.value() instanceof SmithingTemplateItem item) {
                            b.item(item);
                        } else if (entry.value().getDefaultStack().contains(DataComponentTypes.PROVIDES_BANNER_PATTERNS)) {
                            b.item(entry.value());
                        }
                    });
                    return b;
                })
                .item(Items.FILLED_MAP)
                .item(Items.FLOWER_BANNER_PATTERN)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/organic_plant_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 2))
                .item(Items.BEEHIVE)
                .tag(ItemTags.WOODEN_FENCES)
                .item(Items.HONEYCOMB_BLOCK)
                .tag(ItemTags.PLANKS)
                .item(Items.MUSHROOM_STEM)
                .item(Items.CHORUS_PLANT)
                .item(Items.SLIME_BLOCK)
                .item(Items.COBWEB)
                .tag(ItemTags.WOODEN_STAIRS)
                .item(Items.RED_MUSHROOM_BLOCK)
                .item(Items.MOSS_CARPET)
                .item(Items.MOSSY_COBBLESTONE)
                .item(Items.KELP)
                .item(Items.HONEY_BLOCK)
                .item(Items.SPONGE)
                .item(Items.BEE_NEST)
                .item(Items.BROWN_MUSHROOM_BLOCK)
                .item(Items.CAKE)
                .tag(ItemTags.WOODEN_SLABS)
                .tag(ItemTags.LOGS_THAT_BURN)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects_focused"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 9).with(Trait.KNOWLEDGE, 9))
                .item(Items.BRUSH)
                .item(Items.POTION)
                .tag(ItemTags.TRIM_MATERIALS)
                .item(Items.ENDER_EYE)
                .item(Items.GLASS_BOTTLE)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/animal_horns"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 3).with(Trait.LIFE, 1).with(Trait.KINDNESS, -1))
                .item(Items.GOAT_HORN)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/clothing"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 3).with(Trait.EARTH, 0.5F))
                .item(Items.IRON_CHESTPLATE)
                .item(Items.IRON_BOOTS)
                .item(Items.IRON_HORSE_ARMOR)
                .item(Items.IRON_LEGGINGS)
                .item(Items.IRON_HELMET)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/refined_rocks_and_rock_derived"), TraitMap.builder()
                .item(Items.DIAMOND, SpellTraits.of(Trait.STRENGTH, 10))
                .item(Items.EMERALD, SpellTraits.of(Trait.GENEROSITY, -2))
                .item(Items.QUARTZ, SpellTraits.builder().with(Trait.STRENGTH, -1).with(Trait.EARTH, -1.3F).with(Trait.DARKNESS, 1))
                .item(Items.AMETHYST_SHARD, SpellTraits.builder().with(Trait.EARTH, -0.1F).with(Trait.ORDER, 9))
                .item(Items.LAPIS_LAZULI, SpellTraits.builder().with(Trait.POWER, 4).with(Trait.ORDER, 5))
                .item(Items.RAW_IRON, SpellTraits.builder().with(Trait.STRENGTH, 4).with(Trait.FOCUS, 2).with(Trait.EARTH, -0.7F))
                .item(Items.RAW_COPPER, SpellTraits.builder().with(Trait.STRENGTH, 4).with(Trait.FOCUS, 1.5F).with(Trait.EARTH, -0.7F))
                .item(Items.RAW_GOLD, SpellTraits.builder().with(Trait.STRENGTH, -2).with(Trait.FOCUS, 0.5F).with(Trait.EARTH, -1.9F).with(Trait.ORDER, 7))
                .item(Items.GOLD_NUGGET, SpellTraits.builder().with(Trait.EARTH, 0.1F).with(Trait.CHAOS, 1).with(Trait.HAPPINESS, 1))
                .item(Items.IRON_NUGGET, SpellTraits.builder().with(Trait.EARTH, 0.1F).with(Trait.CHAOS, 1).with(Trait.GENEROSITY, 1))
                .item(Items.IRON_INGOT, SpellTraits.builder().with(Trait.STRENGTH, 12).with(Trait.FOCUS, 7).with(Trait.EARTH, -2))
                .item(Items.COPPER_INGOT, SpellTraits.builder().with(Trait.STRENGTH, 11).with(Trait.FOCUS, 6).with(Trait.EARTH, -2))
                .item(Items.GOLD_INGOT, SpellTraits.builder().with(Trait.STRENGTH, -1).with(Trait.FOCUS, 2).with(Trait.EARTH, -2.9F).with(Trait.ORDER, 8))
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/chaotic"), TraitSet.builder(SpellTraits.of(Trait.CHAOS, 15))
                .item(Items.STRING)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_raw_meat"), TraitSet.builder(SpellTraits.builder().with(Trait.FAMINE, -2).with(Trait.BLOOD, 1))
                .tag(ConventionalItemTags.RAW_MEAT_FOODS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/clothing_weak"), TraitSet.builder(SpellTraits.of(Trait.STRENGTH, 3))
                .apply(b -> applyItemsWhere(b, items, path -> path.contains("chainmail")))
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects_enchanted"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 8).with(Trait.KNOWLEDGE, 9).with(Trait.POWER, 2).with(Trait.ORDER, 5))
                .item(Items.FIREWORK_ROCKET)
                .item(Items.EXPERIENCE_BOTTLE)
                .item(Items.FIREWORK_STAR)
                .item(Items.ENCHANTED_BOOK)
                .item(Items.RECOVERY_COMPASS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_death"), TraitSet.builder(SpellTraits.builder().with(Trait.DARKNESS, 9).with(Trait.ROT, 1).with(Trait.BLOOD, 5))
                .item(Items.PUFFERFISH)
                .item(Items.RABBIT_FOOT)
                .item(Items.BONE)
                .item(Items.LEATHER)
                .item(Items.SHULKER_SHELL)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_flame"), TraitSet.builder(SpellTraits.of(Trait.FIRE, 5))
                .item(Items.GLISTERING_MELON_SLICE)
                .item(Items.LAVA_BUCKET)
                .item(Items.FIRE_CHARGE)
                .item(Items.MAGMA_CREAM)
                .item(Items.BLAZE_POWDER)
                .item(Items.FURNACE_MINECART)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/chaotic_worked"), TraitSet.builder(SpellTraits.of(Trait.CHAOS, 5))
                .item(Items.GUNPOWDER)
                .item(Items.BOW)
                .item(Items.TNT_MINECART)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_cooked_meat"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 2).with(Trait.LIFE, -1).with(Trait.FAMINE, -0.5F))
                .tag(ConventionalItemTags.COOKED_FISH_FOODS)
                .item(Items.FERMENTED_SPIDER_EYE)
                .tag(ConventionalItemTags.COOKED_MEAT_FOODS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/clothing_bloody"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 5).with(Trait.BLOOD, 1))
                .apply(b -> applyItemsWhere(b, items, path -> path.contains("leather")))
                .item(Items.SADDLE)
                .item(Items.RABBIT_HIDE)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_life_wet"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 5).with(Trait.WATER, 5))
                .tag(ConventionalItemTags.WATER_BUCKETS)
                .tag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/tools"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 3).with(Trait.ORDER, -2).with(Trait.KINDNESS, 1))
                .tag(ConventionalItemTags.TOOLS)
                .tag(ConventionalItemTags.SHEAR_TOOLS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_flame_from_organic"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, 3).with(Trait.FIRE, 2))
                .item(Items.CHARCOAL)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/uplifting_intellectual_trinkets"), TraitSet.builder(SpellTraits.builder().with(Trait.FOCUS, 2).with(Trait.KNOWLEDGE, 7).with(Trait.ORDER, 4).with(Trait.HAPPINESS, 7))
                .item(Items.PAINTING)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/full_o_life"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 10).with(Trait.EARTH, 2))
                .tag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                .item(Items.BONE_MEAL)
                .tag(ConventionalItemTags.SEEDS)
                .item(Items.WHEAT)
                .item(Items.SNIFFER_EGG)
                .item(Items.COCOA_BEANS)
                .item(Items.EGG)
                .item(Items.HONEYCOMB)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/intellectual_objects"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 7).with(Trait.ORDER, 5))
                .item(Items.LEAD)
                .item(Items.LOOM)
                .tag(ConventionalItemTags.DYES)
                .tag(ItemTags.BOOKSHELF_BOOKS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/overworld/edible_plant_based_and_modified"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 7).with(Trait.FOCUS, 14).with(Trait.ORDER, 9).with(Trait.HAPPINESS, 10))
                .tag(UTags.Items.FORAGE_FILLING)
                .item(Items.GOLDEN_CARROT)
                .item(Items.GOLDEN_APPLE)
                .item(Items.HONEY_BOTTLE)
                .item(Items.ENCHANTED_GOLDEN_APPLE)
                .tag(ConventionalItemTags.CROPS)
                .build());
    }

    private void generateNether(WrapperLookup registries, BiConsumer<Identifier, TraitStream> exporter) {
        var items = registries.getOrThrow(RegistryKeys.ITEM);

        exporter.accept(Identifier.ofVanilla("blocks/underworld/glowing"), TraitSet.builder(SpellTraits.builder().with(Trait.POWER, 2).with(Trait.EARTH, -1).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 4))
                .item(Items.GLOWSTONE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/organic_plant_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 3).with(Trait.BLOOD, 3))
                .item(Items.WEEPING_VINES)
                .item(Items.NETHER_SPROUTS)
                .item(Items.TWISTING_VINES)
                .item(Items.STRIPPED_WARPED_STEM)
                .item(Items.STRIPPED_WARPED_HYPHAE)
                .item(Items.WARPED_FUNGUS)
                .item(Items.STRIPPED_CRIMSON_STEM)
                .item(Items.STRIPPED_CRIMSON_HYPHAE)
                .item(Items.NETHER_WART_BLOCK)
                .apply(b -> applyItemsWhere(b, items, path -> {
                    path = path.replaceFirst("^stripped_", "");
                    return path.startsWith("warped_") || path.startsWith("crimson_");
                }))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/materials_from_the_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.POWER, -0.5F).with(Trait.EARTH, -1).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 4))
                .item(Items.NETHER_GOLD_ORE)
                .item(Items.ANCIENT_DEBRIS)
                .item(Items.NETHER_QUARTZ_ORE)
                .item(Items.BASALT)
                .item(Items.RED_NETHER_BRICKS)
                .item(Items.SOUL_SOIL)
                .item(Items.SMOOTH_BASALT)
                .item(Items.POLISHED_BASALT)
                .item(Items.NETHERRACK)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/refined_rocks_and_rock_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 5).with(Trait.KNOWLEDGE, 2).with(Trait.POWER, -2).with(Trait.EARTH, 2).with(Trait.DARKNESS, 3).with(Trait.BLOOD, 4))
                .item(Items.CHISELED_RED_SANDSTONE)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/conglomerate_artificial_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, -1).with(Trait.CHAOS, 1).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 4))
                .item(Items.RED_SAND)
                .apply(b -> applyItemsWhere(b, items, path -> path.contains("red_sandstone")))
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/loose_materials_from_ground"), TraitSet.builder(SpellTraits.builder().with(Trait.EARTH, -1).with(Trait.ORDER, -1).with(Trait.CHAOS, 2).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 4))
                .item(Items.SOUL_SAND)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/light_emitting_materials"), TraitSet.builder(SpellTraits.builder().with(Trait.FIRE, 1).with(Trait.ROT, 1).with(Trait.BLOOD, 1))
                .item(Items.SHROOMLIGHT)
                .build());
        exporter.accept(Identifier.ofVanilla("blocks/underworld/rocks_and_rock_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 1).with(Trait.POWER, -1).with(Trait.EARTH, 1).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 3))
                .apply(b -> applyItemsWhere(b, items, path -> path.contains("blackstone") || path.contains("nether_brick")))
                .build());

        exporter.accept(Identifier.ofVanilla("items/underworld/organic_plant_derived_artificial"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.ORDER, 1).with(Trait.DARKNESS, 2).with(Trait.BLOOD, 4))
                .item(Items.CRIMSON_SIGN)
                .item(Items.WARPED_SIGN)
                .build());
        exporter.accept(Identifier.ofVanilla("items/underworld/weapons"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 13).with(Trait.ORDER, -2).with(Trait.KINDNESS, -3).with(Trait.DARKNESS, 9).with(Trait.BLOOD, 4))
                .item(Items.NETHERITE_SWORD)
                .item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .build());
        exporter.accept(Identifier.ofVanilla("items/underworld/organic_plant_derived"), TraitSet.builder(SpellTraits.builder().with(Trait.DARKNESS, 2).with(Trait.BLOOD, 8))
                .item(Items.WARPED_FUNGUS_ON_A_STICK)
                .item(Items.NETHER_WART)
                .build());
        exporter.accept(Identifier.ofVanilla("items/underworld/clothing"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 12).with(Trait.EARTH, -0.5F).with(Trait.GENEROSITY, -1).with(Trait.DARKNESS, 9).with(Trait.BLOOD, 2))
                .item(Items.NETHERITE_CHESTPLATE)
                .item(Items.NETHERITE_BOOTS)
                .item(Items.NETHERITE_HELMET)
                .item(Items.NETHERITE_LEGGINGS)
                .build());
        exporter.accept(Identifier.ofVanilla("items/underworld/refined_rocks_and_rock_derived"), TraitMap.builder()
                .item(Items.NETHERITE_INGOT, SpellTraits.builder().with(Trait.STRENGTH, 5).with(Trait.POWER, 7).with(Trait.ORDER, 6).with(Trait.DARKNESS, 7))
                .item(Items.NETHERITE_SCRAP, SpellTraits.builder().with(Trait.STRENGTH, 1).with(Trait.POWER, 1).with(Trait.ORDER, 1.5F).with(Trait.DARKNESS, 8))
                .item(Items.GLOWSTONE_DUST, SpellTraits.of(Trait.FIRE, 0.5F))
                .item(Items.NETHER_BRICK, SpellTraits.builder().with(Trait.EARTH, -1).with(Trait.CHAOS, 1).with(Trait.DARKNESS, 3))
                .item(Items.BLAZE_ROD, SpellTraits.of(Trait.FIRE, 3.5F))
                .item(Items.GHAST_TEAR, SpellTraits.builder().with(Trait.WATER, 0.1F).with(Trait.DARKNESS, 5))
                .build());
        exporter.accept(Identifier.ofVanilla("items/underworld/tools"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 13).with(Trait.ORDER, -2).with(Trait.KINDNESS, -3).with(Trait.DARKNESS, 9))
                .item(Items.NETHERITE_HOE)
                .item(Items.NETHERITE_AXE)
                .item(Items.NETHERITE_PICKAXE)
                .item(Items.NETHERITE_SHOVEL)
                .build());
    }
}
