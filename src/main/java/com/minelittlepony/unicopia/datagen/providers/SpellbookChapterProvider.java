package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter.Builder;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapterLoader;
import com.minelittlepony.unicopia.container.spellbook.TabSide;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
    static final Text UNREADABLE_PAGE_TITLE_ALT = Text.literal("??? ???? ??");
    static final Text TORN_PAGE_TITLE = Text.translatable("gui.unicopia.spellbook.chapter.artefacts.torn_page.title");

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
        this.collector = new DataCollector<>(output.getResolver(SpellbookChapterLoader.REGISTRY_KEY), SpellbookChapter.CODEC);
        this.recipes = recipes;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryFuture.thenCompose(registries -> {
            generate(registries, collector.prime());
            recipes.getIngredients().keySet().stream().filter(key -> !exportedRecipes.contains(key)).forEach(key -> {
                if (!key.getValue().equals(SpellType.empty().getId())) {
                    Datagen.LOGGER.error("Magic spell recipe is not documented: " + key);
                }
            });
            return collector.upload(writer);
        });
    }

    protected void generate(WrapperLookup registries, BiConsumer<Identifier, SpellbookChapter> exporter) {
        var introductionChapterId = Unicopia.id("introduction");
        exporter.accept(introductionChapterId, builder().side(TabSide.RIGHT).apply(
            page().title(pageTitle(introductionChapterId, 1))
                    .apply(pageBody(introductionChapterId, 1), SpellbookChapterProvider::applyAuthor1Signature),
            page().title(pageTitle(introductionChapterId, 2)).apply(
                    pageBody(introductionChapterId, 2),
                    rightFloatingImage(pageTexture(SpellbookState.PROFILE_ID), CENTERED_BOUNDS)),
            page().title(pageTitle(introductionChapterId, 3)).apply(
                    text(paragraph(introductionChapterId, 3), 1, 2),
                    inlineImage(texture(UItems.GEMSTONE), CENTERED_BOUNDS)),
            page().title(pageTitle(introductionChapterId, 4)).apply(
                    text(paragraph(introductionChapterId, 4), 1, 2), SpellbookChapterProvider::applyAuthor1Signature),
            page().title(pageTitle(introductionChapterId, 5)).apply(
                    text(paragraph(introductionChapterId, 5), 1, 3)),
            page().title(pageTitle(introductionChapterId, 6)).apply(
                    rightFloatingImage(pageTexture(SpellbookState.CRAFTING_ID), CENTERED_BOUNDS),
                    text(paragraph(introductionChapterId, 6), 1, 3)),
            page().title(pageTitle(introductionChapterId, 7)).apply(
                    text(paragraph(introductionChapterId, 7), 1, 2), SpellbookChapterProvider::applyAuthor1Signature),
            page().title(pageTitle(introductionChapterId, 8)).level(1).apply(
                    rightFloatingImage(texture(UItems.BOTCHED_GEM), new Bounds(0, 127, 32, 32)),
                    text(paragraph(introductionChapterId, 8), 1, 3)),
            page().title(pageTitle(introductionChapterId, 9))
                    .apply(text(paragraph(introductionChapterId, 9), 1, 2), SpellbookChapterProvider::applyAuthor1Signature)
        ).build());


        var airChapterId = Unicopia.id("air_magic");
        var airElementId = Unicopia.id("air");
        exporter.accept(airChapterId, builder().side(TabSide.RIGHT).tabY(4).apply(
            coverPage(airChapterId, airElementId, 1),
            page().level(1).title(pageTitle(airElementId, 2)).apply(paragraph(airElementId, 1, 1), paragraph(airElementId, 1, 2)),
            page().level(1).element(paragraph(airElementId, 1, 3)).apply(SpellbookChapterProvider::applyAuthor1Signature),
            article(0, airElementId, 3).paragraph().apply(SpellbookChapterProvider::applyAuthor1AltSignature)
                .pageBreak().level(3).incrementPage().paragraphs(3).apply(p -> applyTraitCornerIcon(p, Trait.AIR))
                .pageBreak().paragraphs(2)
                .apply(SpellbookChapterProvider::applyAuthor1Signature),
            recipe(airElementId, SpellType.CATAPULT).level(4).modificationMessage(),
            recipe(airElementId, SpellType.BUBBLE).level(2).iconB(Trait.WATER),
            article(4, airElementId, 7).paragraph().paragraph()
                .pageBreak().level(5).incrementPage().paragraph()
                .pageBreak().level(6).incrementPage().paragraphs(2)
                .pageBreak().level(7).incrementPage().paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            recipe(airElementId, SpellType.FEATHER_FALL).level(5),
            article(6, airElementId, 12).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            page(),
            article(7, airElementId, 13).paragraphs(2)
                .pageBreak().incrementPage().paragraph()
                .apply(SpellbookChapterProvider::applyAuthor1Signature)
                .pageBreak().level(8).incrementPage().paragraphs(2).pageBreak().paragraphs(2)
        ).build());

        var otherworldlyChapterId = Unicopia.id("the_otherworldly");
        var otherworldlyElementId = Unicopia.id("otherworldly");
        exporter.accept(otherworldlyChapterId, builder().side(TabSide.RIGHT).tabY(6).apply(
            coverPage(otherworldlyChapterId, otherworldlyElementId, 10),
            article(20, otherworldlyElementId, 2).paragraph().paragraph().apply(p -> p.element(AUTHOR_2_NAME)),
            recipe(otherworldlyElementId, SpellType.SIPHONING).level(20).paragraphs(1).iconA(Trait.DARKNESS).iconB(Trait.BLOOD),
            recipe(otherworldlyElementId, SpellType.NECROMANCY).level(21).iconA(Trait.DARKNESS).iconB(Trait.BLOOD),
            recipe(otherworldlyElementId, SpellType.DARK_VORTEX).level(23).iconA(Trait.CHAOS).iconB(Trait.DARKNESS),
            recipe(otherworldlyElementId, SpellType.PORTAL).level(24).iconA(Trait.KNOWLEDGE),
            recipe(otherworldlyElementId, SpellType.MIND_SWAP).level(27)
                .modificationMessage(translationKey(otherworldlyElementId, "mind_swap.3.body"))
                .iconA(pageTexture(Unicopia.id("dark_magic")))
        ).build());

        var iceChapterId = Unicopia.id("ice_magic");
        var iceElementId = Unicopia.id("ice");
        exporter.accept(iceChapterId, builder().side(TabSide.RIGHT).tabY(3).apply(
            coverPage(iceChapterId, iceElementId, 0),
            article(0, iceElementId, 2).paragraph().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature),
            recipe(iceElementId, SpellType.FROST).level(0, 3).iconA(texture(Items.SNOWBALL)).iconB(Trait.ICE),
            chapter -> legacyRecipe(chapter, iceElementId, SpellType.CHILLING_BREATH),
            article(4, iceElementId, 5).paragraphs(3)
                .pageBreak().level(5).incrementPage().paragraphs(2)
                .pageBreak().level(6).incrementPage().paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            page(),
            article(4, iceElementId, 8).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            page(),
            recipe(iceElementId, SpellType.LIGHT).level(5).iconA(texture(Items.LIGHT)).iconB(Trait.ICE).modificationMessage(),
            article(4, iceElementId, 10).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature)
                .pageBreak().incrementPage().paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature)
                .pageBreak().level(6).incrementPage().paragraphs(2)
                .pageBreak().incrementPage().paragraphs(2)
                .pageBreak().level(19).incrementPage().paragraphs(2)
                .pageBreak().incrementPage().paragraphs(2)
                .pageBreak().incrementPage().paragraphs(2).next(20).paragraphs(2)
                .pageBreak().incrementPage().paragraph().paragraph().pageBreak().paragraphs(2)
                .pageBreak().level(20).incrementPage().paragraphs(2).pageBreak().paragraphs(2).apply(bb -> bb.element(AUTHOR_1_NAME))
                .pageBreak().level(7).incrementPage().paragraphs(2)
                .pageBreak().incrementPage().paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature),
            recipe(iceElementId, SpellType.HYDROPHOBIC).level(20).iconA(Trait.STRENGTH).iconB(Trait.ICE).modificationMessage(ChapterPageElement.Ingredients.builder()
                .text(1, Text.translatable(translationKey(iceElementId, "hydrophobic.modifier.1")))
                .text(1, Text.translatable(translationKey(iceElementId, "hydrophobic.modifier.2")))
                .build())
        ).build());

        var fireChapterId = Unicopia.id("fire_magic");
        var fireElementId = Unicopia.id("fire");
        exporter.accept(fireChapterId, builder().side(TabSide.RIGHT).tabY(2).apply(
            coverPage(fireChapterId, fireElementId, 0),
            article(0, fireElementId, 2).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            recipe(fireElementId, SpellType.SCORCH).iconA(Trait.FIRE).iconB(Trait.FIRE),
            recipe(fireElementId, SpellType.FLAME).level(1).iconA(Trait.FIRE).iconB(Trait.FIRE),
            page(),
            article(0, fireElementId, 5).paragraphs(3),
            article(2, fireElementId, 6).paragraphs(2).apply(b -> applyTraitCornerIcon(b, Trait.FIRE, Trait.FOCUS)),
            article(2, fireElementId, 7).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature),
            article(21, fireElementId, 8).paragraphs(2),
            page(),
            recipe(fireElementId, SpellType.FIRE_BOLT).level(2).paragraphs(3).iconA(Trait.FIRE).iconB(Trait.FOCUS),
            article(2, fireElementId, 10).paragraphs(3).apply(b -> applyTraitCornerIcon(b, Trait.FIRE, Trait.STRENGTH)),
            article(2, fireElementId, 11).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            article(2, fireElementId, 12).paragraphs(3)
                .pageBreak().level(13).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature)
                .pageBreak().level(3).paragraphs(2),
            page(),
            page(),
            article(9, fireElementId, 13).paragraphs(2).apply(SpellbookChapterProvider::applyAuthor1Signature),
            article(0, fireElementId, 14).paragraph().apply(SpellbookChapterProvider::applyAuthor1Signature),
            page(),
            recipe(fireElementId, SpellType.SHIELD).level(2).iconA(Trait.STRENGTH).iconB(Trait.FIRE).modificationMessage(),
            article(4, fireElementId, 16).paragraphs(2),
            article(5, fireElementId, 17).paragraph(),
            article(3, fireElementId, 18).paragraphs(2)
        ).build());

        var darkMagicChapterId = Unicopia.id("dark_magic");
        exporter.accept(darkMagicChapterId, builder().side(TabSide.RIGHT).tabY(5).apply(
            coverPage(darkMagicChapterId, darkMagicChapterId, 10),
            article(8, darkMagicChapterId, 2).paragraphs(2),
            page(),
            article(9, darkMagicChapterId, 3).paragraph(),
            page(),
            article(10, darkMagicChapterId, 4).paragraphs(2).apply(AUTHOR_1_NAME),
            recipe(darkMagicChapterId, SpellType.VORTEX).level(5).iconA(Trait.AIR).iconB(Trait.KNOWLEDGE).modificationMessage(ChapterPageElement.Ingredients.builder()
                .text(1, Text.translatable(translationKey(darkMagicChapterId, "vortex.modifier.1")))
                .text(1, Text.translatable(translationKey(darkMagicChapterId, "vortex.modifier.2")))
                .build()),
            article(6, darkMagicChapterId, 6).paragraphs(3),
            article(6, darkMagicChapterId, 7).paragraphs(3),
            article(6, darkMagicChapterId, 8).paragraph().apply(AUTHOR_1_NAME).pageBreak().paragraphs(2),
            article(11, darkMagicChapterId, 9).paragraphs(3).apply(AUTHOR_1_NAME),
            article(11, darkMagicChapterId, 10).paragraphs(3).apply(AUTHOR_1_NAME),
            page(),
            page(),
            article(13, darkMagicChapterId, 11).paragraph().paragraph().paragraph().pageBreak().level(14).paragraphs(3).apply(AUTHOR_1_NAME),
            recipe(darkMagicChapterId, SpellType.TRANSFORMATION).level(13).iconA(Trait.LIFE).iconB(Trait.CHAOS),
            recipe(darkMagicChapterId, SpellType.REVEALING).level(15).iconA(Trait.KNOWLEDGE).iconB(Trait.CHAOS).modificationMessage(),
            article(14, darkMagicChapterId, 14).paragraphs(2),
            article(15, darkMagicChapterId, 15).paragraphs(2),
            article(15, darkMagicChapterId, 16).paragraphs(2).apply(AUTHOR_1_NAME),
            page(),
            article(18, darkMagicChapterId, 17).paragraphs(3).apply(AUTHOR_1_NAME),
            article(14, darkMagicChapterId, 18).paragraphs(2).apply(AUTHOR_1_NAME),
            recipe(darkMagicChapterId, SpellType.ARCANE_PROTECTION).level(16).iconA(Trait.KNOWLEDGE).iconB(Trait.DARKNESS).modificationMessage(),
            recipe(darkMagicChapterId, SpellType.DISPLACEMENT).paragraphs(1).level(17).iconA(Trait.KNOWLEDGE).iconB(Trait.CHAOS),
            page().title(pageTitle(darkMagicChapterId, 21)).level(14).apply(paragraph(darkMagicChapterId, 21, 1), paragraph(darkMagicChapterId, 21, 2), AUTHOR_1_NAME),
            page().title(pageTitle(darkMagicChapterId, 22)).level(15),
            recipe(darkMagicChapterId, SpellType.MIMIC).paragraphs(1).level(17, 18).iconA(pageTexture(darkMagicChapterId)).iconB(Trait.DARKNESS).modificationMessage(),
            page(),
            page(),
            page().title(UNREADABLE_PAGE_TITLE_ALT).level(44).apply(paragraph(darkMagicChapterId, 24, 1), paragraph(darkMagicChapterId, 24, 2)),
            page().title(UNREADABLE_PAGE_TITLE_ALT).level(44).apply(paragraph(darkMagicChapterId, 25, 1), AUTHOR_1_NAME),
            page().title(pageTitle(darkMagicChapterId, 26)).level(21).apply(paragraph(darkMagicChapterId, 26, 1)),
            page().title(UNREADABLE_PAGE_TITLE_ALT).level(33).apply(paragraph(darkMagicChapterId, 27, 1), paragraph(darkMagicChapterId, 27, 2), paragraph(darkMagicChapterId, 27, 3), paragraph(darkMagicChapterId, 27, 4)),
            page().title(UNREADABLE_PAGE_TITLE).level(19).apply(paragraph(darkMagicChapterId, 28, 1), AUTHOR_1_NAME, rightFloatingImage(pageTexture(darkMagicChapterId), new Bounds(-10, 125, 32, 32)), rightFloatingImage(Trait.KINDNESS.getSprite(), TRAIT_CORNER_B_BOUNDS)),
            recipe(darkMagicChapterId, SpellType.DISPEL_EVIL).level(19).modificationMessage().finalPage()
        ).build());
        generateArtefactsChapter(registries, exporter);
    }

    protected void generateArtefactsChapter(WrapperLookup registries, BiConsumer<Identifier, SpellbookChapter> exporter) {
        var artefactsChapterId = Unicopia.id("crystal_heart");
        var artefactsElementId = Unicopia.id("artefacts");
        var altarTitle = Text.translatable("gui.unicopia.spellbook.chapter." + artefactsElementId.getPath() + ".altar.title");
        var braceletParagraph = paragraph(artefactsElementId, UItems.FRIENDSHIP_BRACELET);
        var meadobrooksStaffParagraph = paragraph(artefactsElementId, UItems.MEADOWBROOKS_STAFF);
        var grogarsBellParagraph = paragraph(artefactsElementId, UItems.GROGARS_BELL);
        exporter.accept(artefactsChapterId, builder().side(TabSide.RIGHT).tabY(7).apply(
            coverPage(artefactsChapterId, artefactsElementId, 0),
            page().title(pageTitle(artefactsElementId, 2)).apply(paragraph(artefactsElementId, 2, 1), paragraph(artefactsElementId, 2, 2), SpellbookChapterProvider::applyAuthor1Signature),

            page().title(UItems.CRYSTAL_HEART.getName()).apply(item(UItems.CRYSTAL_HEART), STATUS_LOST, text(paragraph(artefactsElementId, UItems.CRYSTAL_HEART), 1, 2)),
            page().title(pageTitle(artefactsElementId, UItems.CRYSTAL_HEART)).apply(paragraph(artefactsElementId, UItems.CRYSTAL_HEART, 3)),

            page().title(TORN_PAGE_TITLE).apply(text(translationKey(artefactsElementId, "torn_page.%d.body"), 1, 3)).apply(ChapterPageElement.Ingredients.builder()
                    .all(Schematic.CRYSTAL_PODIUM.countMaterials(new Schematic.MaterialsCounter())
                            .count(1, UItems.CRYSTAL_HEART)
                            .materials())
                    .build()),

            page().title(Text.translatable(translationKey(artefactsElementId, "crystal_podium.title"))).apply(
                    new ChapterPageElement.Structure(Schematic.CRYSTAL_PODIUM),
                    item(UItems.CRYSTAL_HEART, new Bounds(-34, 60, 0, 0))),
            page().title(altarTitle).apply(text(translationKey(artefactsElementId, "altar.%d.body"), 1, 2)).apply(ChapterPageElement.Ingredients.builder()
                    .all(Schematic.ALTAR.countMaterials(new Schematic.MaterialsCounter().consume(Schematic.ALTAR_VISUAL_BASE_SIZE, Blocks.SOUL_SAND))
                            .count(1, UItems.SPELLBOOK)
                            .materials())
                    .build()),

            page().title(altarTitle).apply(new ChapterPageElement.Structure(Schematic.ALTAR)),
            page().title(UItems.SPECTRAL_CLOCK.getName()).apply(
                    item(UItems.SPECTRAL_CLOCK), STATUS_UNCONFIRMED,
                    paragraph(artefactsElementId, UItems.SPECTRAL_CLOCK, 1),
                    new ChapterPageElement.TextBlock(Text.translatable(translationKey(artefactsElementId, "altar.3.body")).formatted(Formatting.RED))),
            page().title(pageTitle(artefactsElementId, UItems.SPECTRAL_CLOCK)).apply(
                    paragraph(artefactsElementId, UItems.SPECTRAL_CLOCK, 2),
                    SpellbookChapterProvider::applyAuthor1Signature),

            page().title(UItems.DRAGON_BREATH_SCROLL.getName()).apply(
                    item(UItems.DRAGON_BREATH_SCROLL), STATUS_CONFIRMED,
                    paragraph(artefactsElementId, UItems.DRAGON_BREATH_SCROLL, 2),
                    AUTHOR_3_NAME),
            page().title(pageTitle(artefactsElementId, UItems.DRAGON_BREATH_SCROLL)).apply(
                    paragraph(artefactsElementId, UItems.DRAGON_BREATH_SCROLL, 3),
                    recipe(Unicopia.id("dragon_breath_scroll"))),

            page().title(UItems.FRIENDSHIP_BRACELET.getName()).apply(
                    item(UItems.FRIENDSHIP_BRACELET), STATUS_CONFIRMED,
                    text(braceletParagraph, 1, 2)),
            page().title(pageTitle(artefactsElementId, UItems.FRIENDSHIP_BRACELET)).apply(text(braceletParagraph, 3, 4)),

            page().title(UItems.PEGASUS_AMULET.getName()).apply(item(UItems.PEGASUS_AMULET), STATUS_LOST).apply(text(paragraph(artefactsElementId, UItems.PEGASUS_AMULET), 1, 2)),
            page().title(pageTitle(artefactsElementId, UItems.PEGASUS_AMULET)).apply(paragraph(artefactsElementId, UItems.PEGASUS_AMULET, 3)),

            page().title(UItems.MEADOWBROOKS_STAFF.getName()).level(3).apply(item(UItems.MEADOWBROOKS_STAFF), STATUS_CONFIRMED).apply(text(meadobrooksStaffParagraph, 1, 2)),
            page().title(pageTitle(artefactsElementId, UItems.MEADOWBROOKS_STAFF)).level(3).apply(text(meadobrooksStaffParagraph, 3, 4)),

            page().title(UItems.MAGIC_STAFF.getName()).level(5).apply(item(UItems.MAGIC_STAFF), STATUS_UNCONFIRMED, paragraph(artefactsElementId, UItems.MAGIC_STAFF, 1)),
            page().title(pageTitle(artefactsElementId, UItems.MAGIC_STAFF)).level(5).apply(text(paragraph(artefactsElementId, UItems.MAGIC_STAFF), 2, 3)),

            page().title(UItems.GROGARS_BELL.getName()).apply(item(UItems.GROGARS_BELL), STATUS_LOST).apply(text(grogarsBellParagraph, 1, 2)),
            page().title(pageTitle(artefactsElementId, UItems.GROGARS_BELL)).level(80).apply(text(grogarsBellParagraph, 3, 4)),
            page().title(Text.translatable(translationKey(artefactsElementId, UItems.GROGARS_BELL, "2.title"))).level(80).apply(text(paragraph(artefactsElementId, UItems.GROGARS_BELL), 5, 6)),

            page().title(UItems.ALICORN_AMULET.getName()).apply(item(UItems.ALICORN_AMULET), STATUS_UNCONFIRMED, paragraph(artefactsElementId, UItems.ALICORN_AMULET, 1)),
            page().title(pageTitle(artefactsElementId, UItems.ALICORN_AMULET)).level(999).apply(text(paragraph(artefactsElementId, UItems.ALICORN_AMULET), 2, 3))
        ).build());
    }

    static Page.Builder applyAuthor1Signature(Page.Builder page) {
        return page.apply(AUTHOR_1_SIGN_OFF, AUTHOR_1_NAME);
    }

    static Page.Builder applyAuthor1AltSignature(Page.Builder page) {
        return page.apply(AUTHOR_1_SIGN_OFF_B, AUTHOR_1_NAME);
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Trait trait) {
        return applyTraitCornerIcon(page, trait, Trait.CHAOS);
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Trait a, Trait b) {
        return applyTraitCornerIcon(page, a.getSprite(), b.getSprite());
    }

    static Page.Builder applyTraitCornerIcon(Page.Builder page, Identifier a, Identifier b) {
        return page.apply(
                rightFloatingImage(a, TRAIT_CORNER_A_BOUNDS),
                rightFloatingImage(b, TRAIT_CORNER_B_BOUNDS)
        );
    }

    static Page.Builder coverPage(Identifier chapterId, Identifier elementId, int level) {
        return SpellbookChapter.page().title(pageTitle(elementId, 1)).level(level).apply(pageIcon(chapterId));
    }

    @Deprecated
    private SpellbookChapter.Builder legacyRecipe(SpellbookChapter.Builder chapter, Identifier elementId, SpellType<?> output) {
        return chapter.apply(
            page().title(pageTitle(elementId, 4)).apply(
                paragraph(elementId, 4, 1),
                paragraph(elementId, 4, 2),
                b -> applyTraitCornerIcon(b, texture(Items.OAK_BOAT), Trait.ICE.getSprite())),
            page().apply(
                recipe(output.getId()),
                RECIPE_REQUIRES,
                recipes.getIngredientsOrThrow(RegistryKey.of(RegistryKeys.RECIPE, output.getId())))
        );
    }

    @Deprecated
    static ChapterPageElement pageBody(Identifier elementId, int pageNum) {
        return text(translationKey(elementId, "p" + pageNum + ".body"));
    }

    static ChapterPageElement paragraph(Identifier elementId, int pageNum, int paragraphNum) {
        return text(translationKey(elementId, "p" + pageNum + "." + paragraphNum + ".body"));
    }

    static ChapterPageElement paragraph(Identifier elementId, ItemConvertible item, int paragraphNum) {
        return text(translationKey(elementId, itemPath(item) + "." + paragraphNum + ".body"));
    }

    static String paragraph(Identifier elementId, int pageNum) {
        return translationKey(elementId, "p" + pageNum + ".%d.body");
    }

    static String paragraph(Identifier elementId, ItemConvertible item) {
        return translationKey(elementId, itemPath(item) + ".%d.body");
    }

    static String translationKey(Identifier chapterElement, String suffix) {
        return String.format("gui.%s.spellbook.chapter.%s.%s", chapterElement.getNamespace(), chapterElement.getPath(), suffix);
    }

    static String translationKey(Identifier chapterElement, ItemConvertible item, String suffix) {
        return translationKey(chapterElement, itemPath(item) + "." + suffix);
    }

    @SuppressWarnings("deprecation")
    static String itemPath(ItemConvertible item) {
        return item.asItem().getRegistryEntry().registryKey().getValue().getPath();
    }

    @SuppressWarnings("deprecation")
    static Identifier texture(ItemConvertible item) {
        return item.asItem().getRegistryEntry().registryKey().getValue().withPath(p -> "textures/item/" + p + ".png");
    }

    static ChapterPageElement text(String translationKey) {
        return new ChapterPageElement.TextBlock(Text.translatable(translationKey));
    }

    static UnaryOperator<Page.Builder> text(String translationKey, int from, int to) {
        return builder -> builder.apply(IntStream.rangeClosed(from, to).mapToObj(i -> text(String.format(translationKey, i))).toArray(ChapterPageElement[]::new));
    }

    static ChapterPageElement item(ItemConvertible item) {
        return item(item, Bounds.empty());
    }

    static ChapterPageElement item(ItemConvertible item, Bounds bounds) {
        return new ChapterPageElement.Stack(IngredientWithSpell.mundane(item), bounds);
    }

    static ChapterPageElement pageIcon(Identifier pageId) {
        return inlineImage(pageTexture(pageId), CHAPTER_ICON_BOUNDS);
    }

    static ChapterPageElement inlineImage(Identifier texture, Bounds bounds) {
        return new ChapterPageElement.Image(texture, bounds, Flow.NONE);
    }

    static ChapterPageElement rightFloatingImage(Identifier texture, Bounds bounds) {
        return new ChapterPageElement.Image(texture, bounds, Flow.RIGHT);
    }

    static Identifier pageTexture(Identifier pageId) {
        return pageId.withPath(p -> "textures/gui/container/pages/" + p + ".png");
    }

    static Text pageTitle(Identifier chapterId, int pageNum) {
        return Text.translatable(translationKey(chapterId, "p" + pageNum + ".title"));
    }

    @SuppressWarnings("deprecation")
    static Text pageTitle(Identifier chapterId, ItemConvertible item) {
        return Text.translatable(translationKey(chapterId, item.asItem().getRegistryEntry().registryKey().getValue().getPath() + ".title"));
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
        return new RecipeChapterBuilder(elementId, output, output.getId());
    }

    protected ChapterPageElement recipe(Identifier id) {
        var key = RegistryKey.of(RegistryKeys.RECIPE, id);
        recipes.getOrThrow(key);
        return new ChapterPageElement.Recipe(key);
    }

    static ArticleBuilder article(int level, Identifier elementId, int pageNum) {
        return article(level, elementId, pageNum, pageNum);
    }

    static ArticleBuilder article(int level, Identifier elementId, int pageNum, int titlePageNum) {
        return new ArticleBuilder(level, elementId, pageNum, titlePageNum);
    }

    public class RecipeChapterBuilder implements UnaryOperator<Builder> {
        private final SpellType<?> output;
        private final RegistryKey<Recipe<?>> recipeKey;
        private final RegistryEntry<Recipe<?>> recipe;
        private final String translationBase;

        private int page1Level;
        private int page2Level;
        private int paragraphs = 2;

        private Identifier iconA = Trait.AIR.getSprite();
        private Identifier iconB = Trait.CHAOS.getSprite();

        @Nullable
        private ChapterPageElement modificationMessage;

        private RecipeChapterBuilder(Identifier elementId, SpellType<?> output, Identifier recipeId) {
            this.output = output;
            this.recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);
            this.recipe = recipes.getOrThrow(recipeKey);
            this.translationBase = translationKey(elementId, output.getId().getPath());
            Preconditions.checkArgument(exportedRecipes.add(recipeKey), "Recipe has already been documented: " + recipeKey);
        }

        public RecipeChapterBuilder level(int level) {
            return level(level, level);
        }

        public RecipeChapterBuilder level(int page1Level, int page2Level) {
            this.page1Level = page1Level;
            this.page2Level = page2Level;
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
            return chapter.apply(firstPage(), finalPage());
        }

        public UnaryOperator<Builder> firstPage() {
            return page().title(output.getName()).level(page1Level).apply(
                    text(translationBase + ".%d.body", 1, paragraphs),
                    b -> applyTraitCornerIcon(b, iconA, iconB)
            );
        }

        public UnaryOperator<Page.Builder> recipe() {
            return new ChapterPageElement.Recipe(recipe.getKey().orElseThrow());
        }

        public UnaryOperator<Builder> finalPage() {
            return page().level(page2Level).apply(
                recipe(),
                RECIPE_REQUIRES,
                recipes.getIngredientsOrThrow(recipeKey),
                b -> modificationMessage != null ? b.element(modificationMessage) : b
            );
        }
    }

    static class ArticleBuilder implements UnaryOperator<Builder> {
        private final List<Page.Builder> pages = new ArrayList<>();
        private Page.Builder page;
        private int level;
        private int pageNum;
        private final Identifier elementId;
        private int paragraphNum = 0;

        private ArticleBuilder(int level, Identifier elementId, int pageNum, int titlePageNum) {
            this.page = SpellbookChapter.page().level(level).title(pageTitle(elementId, titlePageNum));
            pages.add(page);
            this.level = level;
            this.pageNum = pageNum;
            this.elementId = elementId;
        }

        public ArticleBuilder pageBreak() {
            page = SpellbookChapter.page().level(level);
            pages.add(page);
            return this;
        }

        public ArticleBuilder level(int level) {
            this.level = level;
            page.level(level);
            return this;
        }

        @Deprecated
        public ArticleBuilder next(int level) {
            this.level = level;
            return pageBreak();
        }

        /**
         * @deprecated This makes it too confusing
         */
        @Deprecated
        public ArticleBuilder incrementPage() {
            pageNum++;
            paragraphNum = 0;
            page.title(pageTitle(elementId, pageNum));
            return this;
        }

        public ArticleBuilder paragraph() {
            page.apply(SpellbookChapterProvider.paragraph(elementId, pageNum, ++paragraphNum));
            return this;
        }

        public ArticleBuilder paragraphs(int num) {
            page.apply(SpellbookChapterProvider.text(SpellbookChapterProvider.paragraph(elementId, pageNum), paragraphNum + 1, paragraphNum = (paragraphNum + num)));
            return this;
        }

        public ArticleBuilder apply(UnaryOperator<Page.Builder> sign) {
            page.apply(sign);
            return this;
        }

        @SafeVarargs
        public final ArticleBuilder apply(UnaryOperator<Page.Builder>...elements) {
            page.apply(elements);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder apply(Builder builder) {
            builder.apply(pages.toArray(UnaryOperator[]::new));
            return builder;
        }
    }
}
