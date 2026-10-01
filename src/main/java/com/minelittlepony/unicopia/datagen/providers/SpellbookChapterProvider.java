package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Builder;
import com.minelittlepony.unicopia.container.spellbook.TabSide;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import static com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.*;

public class SpellbookChapterProvider implements DataProvider {
    static final Bounds CHAPTER_ICON_BOUNDS = new Bounds(-10, 15, 128, 128);
    static final Bounds CENTERED_BOUNDS = new Bounds(0, 125, 32, 32);
    static final Bounds TRAIT_CORNER_A_BOUNDS = new Bounds(-10, 125, 32, 32);
    static final Bounds TRAIT_CORNER_B_BOUNDS = new Bounds(-10, 125, 16, 16);
    static final ChapterPageElement AUTHOR_1_SIGN_OFF = text("gui.unicopia.spellbook.author1.sign_off");
    static final ChapterPageElement AUTHOR_1_SIGN_OFF_B = text("gui.unicopia.spellbook.author1.sign_off.b");
    static final ChapterPageElement AUTHOR_1_NAME = text("gui.unicopia.spellbook.author1.name");
    static final ChapterPageElement AUTHOR_2_NAME = text("gui.unicopia.spellbook.author2.name");
    static final ChapterPageElement AUTHOR_3_NAME = text("gui.unicopia.spellbook.author3.name");
    static final ChapterPageElement RECIPE_REQUIRES = text("gui.unicopia.spellbook.recipe.requires");
    static final Text UNREADABLE_PAGE_TITLE = Text.literal("????? ????");

    static final ChapterPageElement STATUS_UNCONFIRMED = text("gui.unicopia.spellbook.chapter.artefacts.status.unconfirmed");
    static final ChapterPageElement STATUS_CONFIRMED = text("gui.unicopia.spellbook.chapter.artefacts.status.confirmed");
    static final ChapterPageElement STATUS_LOST = text("gui.unicopia.spellbook.chapter.artefacts.status.lost");

    protected final FabricDataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryFuture;

    private final DataCollector<SpellbookChapter> collector;

    private final URecipeProvider recipes;
    private final Set<RegistryKey<Recipe<?>>> exportedRecipes = new HashSet<>();

    public SpellbookChapterProvider(FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup, URecipeProvider recipes) {
        this.output = output;
        this.registryFuture = registryLookup;
        this.collector = new DataCollector<>(output.getResolver(OutputType.DATA_PACK, "spellbook/chapters"), SpellbookChapter.CODEC);
        this.recipes = recipes;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryFuture.thenCompose(registries -> {
            generate(registries, collector.prime());
            recipes.getIngredients().keySet().stream().filter(key -> !exportedRecipes.contains(key)).forEach(key -> {
                Datagen.LOGGER.warn("Magic spell recipe is not documented: " + key);
            });
            return collector.upload(writer);
        });
    }

    protected void generate(WrapperLookup registries, BiConsumer<Identifier, SpellbookChapter> exporter) {
        var introductionChapterId = Unicopia.id("introduction");
        exporter.accept(introductionChapterId, builder().side(TabSide.RIGHT)
                .page(page().title(pageTitle(introductionChapterId, 1))
                        .element(paragraph(introductionChapterId, 1, 1))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page().title(pageTitle(introductionChapterId, 2))
                        .element(paragraph(introductionChapterId, 2, 1))
                        .element(new ChapterPageElement.Image(Unicopia.id("textures/gui/container/pages/profile.png"), CENTERED_BOUNDS, Flow.NONE)))
                .page(page().title(pageTitle(introductionChapterId, 3))
                        .apply(text(paragraph(introductionChapterId, 3), 1, 2))
                        .element(new ChapterPageElement.Image(Unicopia.id("textures/item/gemstone.png"), CENTERED_BOUNDS, Flow.NONE)))
                .page(page().title(pageTitle(introductionChapterId, 4))
                        .apply(text(paragraph(introductionChapterId, 4), 1, 2))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page().title(pageTitle(introductionChapterId, 5))
                        .apply(text(paragraph(introductionChapterId, 5), 1, 3)))
                .page(page().title(pageTitle(introductionChapterId, 6))
                        .element(new ChapterPageElement.Image(Unicopia.id("textures/gui/container/pages/crafting.png"), CENTERED_BOUNDS, Flow.NONE))
                        .apply(text(paragraph(introductionChapterId, 6), 1, 3)))
                .page(page().title(pageTitle(introductionChapterId, 7))
                        .apply(text(paragraph(introductionChapterId, 7), 1, 2))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page().title(pageTitle(introductionChapterId, 8)).level(1)
                        .element(new ChapterPageElement.Image(Unicopia.id("textures/item/botched_gem.png"), new Bounds(0, 127, 32, 32), Flow.NONE))
                        .apply(text(paragraph(introductionChapterId, 8), 1, 3)))
                .page(page().title(pageTitle(introductionChapterId, 9))
                        .apply(text(paragraph(introductionChapterId, 9), 1, 2))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .build());


        var airChapterId = Unicopia.id("air_magic");
        var airElementId = Unicopia.id("air");
        exporter.accept(airChapterId, builder().side(TabSide.RIGHT).tabY(4)
                .page(coverPage(airChapterId, airElementId, 1))
                .apply(article(1, airElementId, 1, 2).paragraph().paragraph().next().paragraph()
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(article(0, airElementId, 3).paragraph()
                        .apply(SpellbookChapterProvider::applyAuthor1AltSignature)
                        .incrementPage().next(4)
                        .paragraph().paragraph().paragraph().apply(p -> applyTraitCornerIcon(p, Trait.AIR))
                        .next().paragraph().paragraph()
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(recipe(airElementId, SpellType.CATAPULT).level(4).iconA(Trait.AIR).iconB(Trait.CHAOS).modificationMessage())
                .apply(recipe(airElementId, SpellType.BUBBLE, Trait.AIR, Trait.WATER, 2))
                .apply(article(4, airElementId, 7).paragraph().paragraph()
                        .incrementPage().next(5).paragraph()
                        .incrementPage().next(6).paragraph().paragraph()
                        .incrementPage().next(7).paragraph().paragraph()
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(recipe(airElementId, SpellType.FEATHER_FALL, Trait.AIR, Trait.CHAOS, 5))
                .apply(article(6, airElementId, 12).paragraph().paragraph())
                .page(page())
                .apply(article(7, airElementId, 13).paragraph().paragraph()
                        .incrementPage().next().paragraph()
                        .apply(SpellbookChapterProvider::applyAuthor1Signature)
                        .incrementPage().next(8).paragraph().paragraph().next().paragraph().paragraph())
                .build()
        );

        var otherworldlyChapterId = Unicopia.id("the_otherworldly");
        var otherworldlyElementId = Unicopia.id("otherworldly");
        exporter.accept(otherworldlyChapterId, builder().side(TabSide.RIGHT).tabY(6)
                .page(coverPage(otherworldlyChapterId, otherworldlyElementId, 10))
                .apply(article(20, otherworldlyElementId, 2).paragraph().paragraph().apply(p -> p.element(AUTHOR_2_NAME)))
                .apply(recipe(otherworldlyElementId, SpellType.SIPHONING).level(20).paragraphs(1).iconA(Trait.DARKNESS).iconB(Trait.BLOOD))
                .apply(recipe(otherworldlyElementId, SpellType.NECROMANCY, Trait.DARKNESS, Trait.BLOOD, 21))
                .apply(recipe(otherworldlyElementId, SpellType.DARK_VORTEX, Trait.CHAOS, Trait.DARKNESS, 23))
                .apply(recipe(otherworldlyElementId, SpellType.PORTAL, Trait.KNOWLEDGE, Trait.CHAOS, 24))
                .apply(recipe(otherworldlyElementId, SpellType.MIND_SWAP).level(24)
                        .modificationMessage("gui.unicopia.spellbook.chapter.otherworldly.mind_swap.3.body")
                        .iconA(Unicopia.id("textures/gui/container/pages/dark_magic.png")))
                .build()
        );

        var iceChapterId = Unicopia.id("ice_magic");
        var iceElementId = Unicopia.id("ice");
        exporter.accept(iceChapterId, builder().side(TabSide.RIGHT).tabY(3)
                .page(coverPage(iceChapterId, iceElementId, 0))
                .apply(article(0, airElementId, 2).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(recipe(otherworldlyElementId, SpellType.FROST).level(3).iconA(Identifier.ofVanilla("textures/item/snowball.png")).iconB(Trait.ICE))
                .apply(recipe(otherworldlyElementId, SpellType.CHILLING_BREATH).level(3).iconA(Identifier.ofVanilla("textures/item/snowball.png")).iconA(Identifier.ofVanilla("textures/item/oak_boat.png")).iconB(Trait.ICE))
                .apply(article(4, airElementId, 5).paragraph().paragraph().paragraph()
                        .incrementPage().next(5).paragraph().paragraph()
                        .incrementPage().next(6).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page())
                .apply(article(4, airElementId, 8).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page())
                .apply(recipe(otherworldlyElementId, SpellType.LIGHT).level(5).iconA(Identifier.ofVanilla("textures/item/light.png")).iconB(Trait.ICE).modificationMessage())
                .apply(article(4, airElementId, 10).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature)
                        .incrementPage().next().paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature)
                        .incrementPage().next(6).paragraph().paragraph()
                        .incrementPage().next().paragraph().paragraph()
                        .incrementPage().next(19).paragraph().paragraph()
                        .incrementPage().next().paragraph().paragraph()
                        .incrementPage().next().paragraph().paragraph()
                        .next(20).paragraph().paragraph()
                        .incrementPage().next().paragraph().paragraph().next().paragraph().paragraph()
                        .incrementPage().next().paragraph().paragraph().next().paragraph().paragraph().apply(bb -> bb.element(AUTHOR_1_NAME))
                        .incrementPage().next().paragraph().paragraph()
                        .incrementPage().next().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(recipe(otherworldlyElementId, SpellType.HYDROPHOBIC, Unicopia.id("spells/hydrophobic")).level(20).iconA(Trait.STRENGTH).iconB(Trait.ICE).modificationMessage(ChapterPageElement.Ingredients.builder()
                        .text(1, Text.translatable("gui.unicopia.spellbook.chapter.ice.hydrophobic.modifier.1"))
                        .text(1, Text.translatable("gui.unicopia.spellbook.chapter.ice.hydrophobic.modifier.2"))
                        .build()))
                .build()
        );

        var fireChapterId = Unicopia.id("fire_magic");
        var fireElementId = Unicopia.id("fire");
        exporter.accept(fireChapterId, builder().side(TabSide.RIGHT).tabY(2)
                .page(coverPage(fireChapterId, fireElementId, 0))
                .apply(article(0, fireElementId, 2).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(recipe(fireElementId, SpellType.SCORCH, Unicopia.id("spells/scorch")).iconA(Trait.FIRE).iconB(Trait.FIRE))
                .apply(recipe(fireElementId, SpellType.FLAME, Unicopia.id("spells/flame")).level(1).iconA(Trait.FIRE).iconB(Trait.FIRE))
                .page(page())
                .apply(article(0, fireElementId, 5).paragraph().paragraph().paragraph())
                .apply(article(2, fireElementId, 6).paragraph().paragraph().apply(b -> applyTraitCornerIcon(b, Trait.FIRE, Trait.FOCUS)))
                .apply(article(2, fireElementId, 7).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(article(21, fireElementId, 8).paragraph().paragraph())
                .page(page())
                .apply(recipe(fireElementId, SpellType.FIRE_BOLT).level(2).paragraphs(3).iconA(Trait.FIRE).iconB(Trait.FOCUS))
                .apply(article(2, fireElementId, 10).paragraph().paragraph().paragraph().apply(b -> applyTraitCornerIcon(b, Trait.FIRE, Trait.STRENGTH)))
                .apply(article(2, fireElementId, 11).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(article(2, fireElementId, 12).paragraph().paragraph().paragraph()
                        .next(13).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature)
                        .next(3).paragraph().paragraph())
                .page(page())
                .page(page())
                .apply(article(9, fireElementId, 13).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .apply(article(0, fireElementId, 14).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page())
                .apply(recipe(fireElementId, SpellType.SHIELD).level(2).iconA(Trait.STRENGTH).iconB(Trait.FIRE))
                .apply(article(4, fireElementId, 16).paragraph().paragraph())
                .apply(article(5, fireElementId, 17).paragraph())
                .apply(article(3, fireElementId, 18).paragraph().paragraph())
                .build());

        var darkMagicChapterId = Unicopia.id("dark_magic");
        exporter.accept(darkMagicChapterId, builder().side(TabSide.RIGHT).tabY(5)
                .page(coverPage(darkMagicChapterId, darkMagicChapterId, 10))
                .apply(article(8, darkMagicChapterId, 2).paragraph().paragraph())
                .page(page())
                .apply(article(9, darkMagicChapterId, 3).paragraph())
                .page(page())
                .apply(article(10, darkMagicChapterId, 4).paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .apply(recipe(darkMagicChapterId, SpellType.VORTEX).level(5).iconA(Trait.AIR).iconB(Trait.KNOWLEDGE).modificationMessage(ChapterPageElement.Ingredients.builder()
                        .text(1, Text.translatable("gui.unicopia.spellbook.chapter.dark_magic.vortex.modifier.1"))
                        .text(1, Text.translatable("gui.unicopia.spellbook.chapter.dark_magic.vortex.modifier.2"))
                        .build()))
                .apply(article(6, darkMagicChapterId, 6).paragraph().paragraph().paragraph())
                .apply(article(6, darkMagicChapterId, 7).paragraph().paragraph().paragraph())
                .apply(article(6, darkMagicChapterId, 8).paragraph().apply(p -> p.element(AUTHOR_1_NAME)).next().paragraph().paragraph())
                .apply(article(11, darkMagicChapterId, 9).paragraph().paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .apply(article(11, darkMagicChapterId, 10).paragraph().paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .page(page())
                .page(page())
                .apply(article(13, darkMagicChapterId, 11).paragraph().paragraph().paragraph().next(14).paragraph().paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .apply(recipe(darkMagicChapterId, SpellType.TRANSFORMATION).level(13).iconA(Trait.LIFE).iconB(Trait.CHAOS))
                .apply(recipe(darkMagicChapterId, SpellType.REVEALING).level(15).iconA(Trait.KNOWLEDGE).iconB(Trait.CHAOS).modificationMessage())
                .apply(article(14, darkMagicChapterId, 14).paragraph().paragraph())
                .apply(article(15, darkMagicChapterId, 15).paragraph().paragraph())
                .apply(article(15, darkMagicChapterId, 16).paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .page(page())
                .apply(article(18, darkMagicChapterId, 17).paragraph().paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .apply(article(14, darkMagicChapterId, 18).paragraph().paragraph().apply(p -> p.element(AUTHOR_1_NAME)))
                .apply(recipe(darkMagicChapterId, SpellType.ARCANE_PROTECTION).level(16).iconA(Trait.KNOWLEDGE).iconB(Trait.DARKNESS).modificationMessage())
                .apply(recipe(darkMagicChapterId, SpellType.DISPLACEMENT).level(17).iconA(Trait.KNOWLEDGE).iconB(Trait.CHAOS))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.dark_magic.p21.title")).level(14).element(paragraph(darkMagicChapterId, 21, 1), paragraph(darkMagicChapterId, 21, 2), AUTHOR_1_NAME))
                .page(page())
                .apply(recipe(darkMagicChapterId, SpellType.MIMIC, Unicopia.id("spells/mimic")).level(18).iconA(Trait.DARKNESS).iconB(Trait.DARKNESS).modificationMessage())
                .page(page())
                .page(page())
                .page(page().title(UNREADABLE_PAGE_TITLE).level(44).element(paragraph(darkMagicChapterId, 24, 1), paragraph(darkMagicChapterId, 24, 2)))
                .page(page().title(UNREADABLE_PAGE_TITLE).level(44).element(paragraph(darkMagicChapterId, 25, 1), AUTHOR_1_NAME))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.dark_magic.p26.title")).level(21).element(paragraph(darkMagicChapterId, 26, 1)))
                .page(page().title(UNREADABLE_PAGE_TITLE).level(33).element(paragraph(darkMagicChapterId, 27, 1), paragraph(darkMagicChapterId, 27, 2), paragraph(darkMagicChapterId, 27, 3), paragraph(darkMagicChapterId, 27, 4)))
                .page(page().title(UNREADABLE_PAGE_TITLE).level(19).element(paragraph(darkMagicChapterId, 28, 1), AUTHOR_1_NAME, pageIcon(darkMagicChapterId), new ChapterPageElement.Image(Unicopia.id("textures/gui/trait/kindness.png"), TRAIT_CORNER_B_BOUNDS, Flow.NONE)))
                .apply(recipe(darkMagicChapterId, SpellType.DISPEL_EVIL).level(19).modificationMessage()::applyFinalPage)
                .build());
        generateArtefactsChapter(registries, exporter);
    }

    protected void generateArtefactsChapter(WrapperLookup registries, BiConsumer<Identifier, SpellbookChapter> exporter) {
        var artefactsChapterId = Unicopia.id("crystal_heart");
        var artefactsElementId = Unicopia.id("artefacts");
        exporter.accept(artefactsChapterId, builder().side(TabSide.RIGHT).tabY(7)
                .page(coverPage(artefactsChapterId, artefactsElementId, 0))
                .page(page().title(pageTitle(artefactsElementId, 2))
                        .element(paragraph(artefactsElementId, 2, 1), paragraph(artefactsElementId, 2, 2))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page().title(UItems.CRYSTAL_HEART.getName())
                        .element(item(UItems.CRYSTAL_HEART))
                        .element(STATUS_LOST)
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.crystal_heart.1.body"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.crystal_heart.2.body")))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.crystal_heart.title"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.crystal_heart.3.body")))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.torn_page.title"))
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.torn_page.%d.body", 1, 3))
                        .element(ChapterPageElement.Ingredients.builder()
                                .item(2, Items.END_ROD)
                                .item(20, Items.DIAMOND_BLOCK)
                                .item(1, UItems.CRYSTAL_HEART)
                                .build()))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.crystal_podium.title"))
                        .element(ChapterPageElement.Structure.builder()
                                .fill(Vec3i.ZERO, new Vec3i(2, 0, 2), Blocks.DIAMOND_BLOCK.getDefaultState())
                                .set(new Vec3i(1, 1, 1), Blocks.DIAMOND_BLOCK.getDefaultState())
                                .set(new Vec3i(1, 2, 1), Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.UP))
                                .set(new Vec3i(1, 4, 1), Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.DOWN))
                                .set(new Vec3i(1, 5, 1), Blocks.DIAMOND_BLOCK.getDefaultState())
                                .fill(new Vec3i(0, 6, 0), new Vec3i(2, 6, 2), Blocks.DIAMOND_BLOCK.getDefaultState())
                                .build())
                        .element(item(UItems.CRYSTAL_HEART, new Bounds(60, -34, 0, 0))))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.altar.title"))
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.altar.%d.body", 1, 2))
                        .element(ChapterPageElement.Ingredients.builder()
                                .item(40, Items.OBSIDIAN)
                                .item(1, Items.SOUL_SAND)
                                .item(1, Items.LODESTONE)
                                .item(1, UItems.SPELLBOOK)
                                .build()))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.altar.title"))
                        .element(ChapterPageElement.Structure.builder()
                                .fill(Vec3i.ZERO, new Vec3i(8, 0, 8), Blocks.SOUL_SAND.getDefaultState())
                                .fill(new Vec3i(3, 1, 3), new Vec3i(5, 1, 5), Blocks.OBSIDIAN.getDefaultState())
                                .set(new Vec3i(1, 4, 1), Blocks.SOUL_SAND.getDefaultState())
                                .set(new Vec3i(4, 1, 6), Blocks.LODESTONE.getDefaultState())
                                .fill(new Vec3i(0, 1, 2), new Vec3i(0, 4, 2), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(0, 1, 6), new Vec3i(0, 4, 6), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(2, 1, 0), new Vec3i(2, 4, 0), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(6, 1, 0), new Vec3i(6, 4, 0), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(8, 1, 2), new Vec3i(8, 4, 2), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(8, 1, 6), new Vec3i(8, 4, 6), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(2, 1, 8), new Vec3i(2, 4, 8), Blocks.OBSIDIAN.getDefaultState())
                                .fill(new Vec3i(6, 1, 8), new Vec3i(6, 4, 8), Blocks.OBSIDIAN.getDefaultState())
                                .build()))
                .page(page().title(UItems.SPECTRAL_CLOCK.getName())
                        .element(item(UItems.SPECTRAL_CLOCK))
                        .element(STATUS_UNCONFIRMED)
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.spectral_clock.1.body"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.altar.3.body")))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.spectral_clock.title"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.spectral_clock.2.body"))
                        .apply(SpellbookChapterProvider::applyAuthor1Signature))
                .page(page().title(UItems.DRAGON_BREATH_SCROLL.getName())
                        .element(item(UItems.DRAGON_BREATH_SCROLL), STATUS_CONFIRMED)
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.dragon_breath_scroll.2.body"))
                        .element(AUTHOR_3_NAME))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.dragon_breath_scroll.title"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.dragon_breath_scroll.3.body"))
                        .element(recipe(Unicopia.id("dragon_breath_scroll"))))
                .page(page().title(UItems.FRIENDSHIP_BRACELET.getName())
                        .element(item(UItems.FRIENDSHIP_BRACELET), STATUS_CONFIRMED)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.friendship_bracelet.%d.body", 1, 2)))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.friendship_bracelet.title"))
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.friendship_bracelet.%d.body", 3, 4)))
                .page(page().title(UItems.PEGASUS_AMULET.getName())
                        .element(item(UItems.PEGASUS_AMULET), STATUS_LOST)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.pegasus_amulet.%d.body", 1, 2)))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.pegasus_amulet.title"))
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.pegasus_amulet.3.body")))
                .page(page().title(UItems.MEADOWBROOKS_STAFF.getName()).level(3)
                        .element(item(UItems.MEADOWBROOKS_STAFF), STATUS_CONFIRMED)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.meadowbrooks_staff.%d.body", 1, 2)))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.meadowbrooks_staff.title")).level(3)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.meadowbrooks_staff.%d.body", 3, 4)))
                .page(page().title(UItems.MAGIC_STAFF.getName()).level(5)
                        .element(item(UItems.MAGIC_STAFF), STATUS_UNCONFIRMED)
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.magic_staff.1.body")))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.magic_staff.title")).level(5)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.magic_staff.%d.body", 2, 3)))
                .page(page().title(UItems.GROGARS_BELL.getName())
                        .element(item(UItems.GROGARS_BELL), STATUS_LOST)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.grogars_bell.%d.body", 1, 2)))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.grogars_bell.title")).level(80)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.grogars_bell.%d.body", 3, 4)))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.grogars_bell.2.title")).level(80)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.grogars_bell.%d.body", 5, 6)))
                .page(page().title(UItems.ALICORN_AMULET.getName())
                        .element(item(UItems.ALICORN_AMULET), STATUS_UNCONFIRMED)
                        .element(text("gui.unicopia.spellbook.chapter.artefacts.alicorn_amulet.1.body")))
                .page(page().title(Text.translatable("gui.unicopia.spellbook.chapter.artefacts.alicorn_amulet.title")).level(999)
                        .apply(text("gui.unicopia.spellbook.chapter.artefacts.alicorn_amulet.%d.body", 2, 3)))
                .build()
        );
    }

    protected RecipeChapterBuilder recipe(Identifier elementId, SpellType<?> output, Identifier recipeId, Trait iconA, Trait iconB, int level) {
        return new RecipeChapterBuilder(elementId, output, recipeId)
                .level(level)
                .iconA(iconA)
                .iconB(iconB);
    }

    protected RecipeChapterBuilder recipe(Identifier elementId, SpellType<?> output, Trait iconA, Trait iconB, int level) {
        return new RecipeChapterBuilder(elementId, output, output.getId().withPath(p -> "spells/" + p))
                .level(level)
                .iconA(iconA)
                .iconB(iconB);
    }

    static Page.Builder applyAuthor1Signature(Page.Builder page) {
        return page
                .element(AUTHOR_1_SIGN_OFF)
                .element(AUTHOR_1_NAME);
    }

    static Page.Builder applyAuthor1AltSignature(Page.Builder page) {
        return page
                .element(AUTHOR_1_SIGN_OFF_B)
                .element(AUTHOR_1_NAME);
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Trait trait) {
        return applyTraitCornerIcon(page, trait, Trait.CHAOS);
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Trait a, Trait b) {
        return applyTraitCornerIcon(page, a.getSprite(), b.getSprite());
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Identifier a, Identifier b) {
        return page
            .element(new ChapterPageElement.Image(a, TRAIT_CORNER_A_BOUNDS, Flow.NONE))
            .element(new ChapterPageElement.Image(b, TRAIT_CORNER_B_BOUNDS, Flow.NONE));
    }

    static Page.Builder coverPage(Identifier chapterId, Identifier elementId, int level) {
        return SpellbookChapter.page().title(pageTitle(elementId, 1)).level(level).element(pageIcon(chapterId));
    }

    static ChapterPageElement paragraph(Identifier elementId, int pageNum, int paragraphNum) {
        return text("gui." + elementId.getNamespace() + ".spellbook.chapter." + elementId.getPath() + ".p" + pageNum + "." + paragraphNum + ".body");
    }

    static String paragraph(Identifier elementId, int pageNum) {
        return "gui." + elementId.getNamespace() + ".spellbook.chapter." + elementId.getPath() + ".p" + pageNum + ".%d.body";
    }

    static ChapterPageElement text(String translationKey) {
        return new ChapterPageElement.TextBlock(Text.translatable(translationKey));
    }

    static UnaryOperator<Page.Builder> text(String translationKey, int from, int to) {
        return builder -> {
            for (int i = from; i <= to; i++) {
                builder.element(text(String.format(translationKey, i)));
            }
            return builder;
        };
    }

    static ChapterPageElement item(ItemConvertible item) {
        return item(item, Bounds.empty());
    }

    static ChapterPageElement item(ItemConvertible item, Bounds bounds) {
        return new ChapterPageElement.Stack(IngredientWithSpell.mundane(item), bounds);
    }


    static ChapterPageElement pageIcon(Identifier pageId) {
        return new ChapterPageElement.Image(pageId.withPath(p -> "textures/gui/container/pages/" + p + ".png"), CHAPTER_ICON_BOUNDS, Flow.NONE);
    }

    static Text pageTitle(Identifier chapterId, int pageNum) {
        return Text.translatable("gui." + chapterId.getNamespace() + ".spellbook.chapter." + chapterId.getPath() + ".p" + pageNum + ".title");
    }

    @Override
    public String getName() {
        return "Spellbook Chapters";
    }

    public interface MagicRecipeExporter extends RecipeExporter {
        void acceptChapterIngredients(RegistryKey<Recipe<?>> key, ChapterPageElement.Ingredients ingredients);
    }

    protected RecipeChapterBuilder recipe(Identifier elementId, SpellType<?> output, Identifier recipeId) {
        return new RecipeChapterBuilder(elementId, output, recipeId);
    }

    protected RecipeChapterBuilder recipe(Identifier elementId, SpellType<?> output) {
        return new RecipeChapterBuilder(elementId, output, output.getId().withPath(p -> "spells/" + p));
    }

    static ArticleBuilder article(int level, Identifier elementId, int pageNum) {
        return article(level, elementId, pageNum, pageNum);
    }

    static ArticleBuilder article(int level, Identifier elementId, int pageNum, int titlePageNum) {
        return new ArticleBuilder(level, elementId, pageNum, titlePageNum);
    }

    protected ChapterPageElement recipe(Identifier id) {
        var recipe = recipes.getOrThrow(RegistryKey.of(RegistryKeys.RECIPE, id));
        return new ChapterPageElement.Recipe(recipe, recipe.value().getDisplays());
    }

    public class RecipeChapterBuilder implements UnaryOperator<Builder> {
        private final SpellType<?> output;
        private final RegistryKey<Recipe<?>> recipeKey;
        private final RegistryEntry<Recipe<?>> recipe;
        private final String translationBase;

        private int level;
        private int paragraphs = 2;

        private Identifier iconA = Trait.AIR.getSprite();
        private Identifier iconB = Trait.CHAOS.getSprite();

        @Nullable
        private ChapterPageElement modificationMessage;

        private RecipeChapterBuilder(Identifier elementId, SpellType<?> output, Identifier recipeId) {
            this.output = output;
            this.recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);
            this.recipe = recipes.getOrThrow(recipeKey);
            this.translationBase = "gui." + elementId.getNamespace() + ".spellbook.chapter." + elementId.getPath() + "." + output.getId().getPath();
            Preconditions.checkArgument(exportedRecipes.add(recipeKey), "Recipe has already been documented: " + recipeKey);
        }

        public RecipeChapterBuilder level(int level) {
            this.level = level;
            return this;
        }

        public RecipeChapterBuilder iconA(Trait trait) {
            return iconA(trait.getSprite());
        }

        public RecipeChapterBuilder iconA(Identifier icon) {
            this.iconA = icon;
            return this;
        }

        public RecipeChapterBuilder iconB(Trait trait) {
            return iconB(trait.getSprite());
        }

        public RecipeChapterBuilder iconB(Identifier icon) {
            this.iconB = icon;
            return this;
        }

        public RecipeChapterBuilder modificationMessage() {
            return modificationMessage(translationBase + ".modifier.1");
        }

        public RecipeChapterBuilder modificationMessage(String message) {
            return modificationMessage(text(message));
        }

        public RecipeChapterBuilder modificationMessage(ChapterPageElement message) {
            modificationMessage = message;
            return this;
        }


        public RecipeChapterBuilder paragraphs(int count) {
            paragraphs = count;
            return this;
        }

        @Override
        public Builder apply(Builder chapter) {
            return chapter.apply(this::applyFirstPage).apply(this::applyFinalPage);
        }

        public Builder applyFirstPage(Builder chapter) {
            return chapter.page(page().title(output.getName()).level(level)
                    .apply(text(translationBase + ".%d.body", 1, paragraphs))
                    .apply(b -> applyTraitCornerIcon(b, iconA, iconB))
            );
        }

        public Page.Builder applyRecipeOnly(Page.Builder page) {
            return page.element(new ChapterPageElement.Recipe(recipe, recipe.value().getDisplays()));
        }

        public Builder applyFinalPage(Builder chapter) {
            return chapter.page(page().level(level)
                    .apply(this::applyRecipeOnly)
                    .element(RECIPE_REQUIRES)
                    .element(recipes.getIngredientsOrThrow(recipeKey))
                    .apply(b -> modificationMessage != null ? b.element(modificationMessage) : b)
            );
        }
    }

    static class ArticleBuilder implements UnaryOperator<Builder> {
        private final List<Page.Builder> pages = new ArrayList<>();
        private Page.Builder page;
        private int level;
        private int pageNum;
        private final Identifier elementId;
        private int paragraphNum = 1;

        private ArticleBuilder(int level, Identifier elementId, int pageNum, int titlePageNum) {
            this.page = page().level(level).title(pageTitle(elementId, titlePageNum));
            pages.add(page);
            this.level = level;
            this.pageNum = pageNum;
            this.elementId = elementId;
        }

        public ArticleBuilder next() {
            page = page().level(level);
            pages.add(page);
            return this;
        }

        public ArticleBuilder next(int level) {
            this.level = level;
            return next();
        }

        public ArticleBuilder incrementPage() {
            pageNum++;
            paragraphNum = 1;
            page.title(pageTitle(elementId, pageNum));
            return this;
        }

        public ArticleBuilder paragraph() {
            page.element(SpellbookChapterProvider.paragraph(elementId, pageNum, paragraphNum++));
            return this;
        }

        public ArticleBuilder paragraph(int num) {
            page.apply(SpellbookChapterProvider.text(SpellbookChapterProvider.paragraph(elementId, pageNum), paragraphNum, paragraphNum = (paragraphNum + num)));
            return this;
        }

        public ArticleBuilder apply(UnaryOperator<Page.Builder> sign) {
            page.apply(sign);
            return this;
        }

        public ArticleBuilder element(ChapterPageElement...elements) {
            page.element(elements);
            return this;
        }

        @Override
        public Builder apply(Builder builder) {
            pages.forEach(builder::page);
            return builder;
        }
    }
}
