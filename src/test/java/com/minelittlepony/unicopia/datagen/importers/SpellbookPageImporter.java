package com.minelittlepony.unicopia.datagen.importers;

import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.mojang.serialization.JsonOps;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

public class SpellbookPageImporter implements DataImportProvider.DataImporter {
    static final String INDENT = "    ";
    static final Map<String, String> ELEMENT_NAMES = Map.of(
            "the", "otherworldly",
            "crystal", "artefacts",
            "dark", "dark_magic"
    );
    static final Bounds CHAPTER_ICON_BOUNDS = new Bounds(-10, 15, 128, 128);
    static final ChapterPageElement AUTHOR_1_SIGN_OFF = text("gui.unicopia.spellbook.author1.sign_off");
    static final ChapterPageElement AUTHOR_1_SIGN_OFF_B = text("gui.unicopia.spellbook.author1.sign_off.b");
    static final ChapterPageElement AUTHOR_1_NAME = text("gui.unicopia.spellbook.author1.name");

    @Override
    public void generateCode(RegistryWrapper.WrapperLookup registries, ResourceCollector collector, Consumer<String> output) {
        collector.collect(registries, SpellbookChapter.CODEC, "spellbook/chapters").forEach(entry -> {
            generateCode(registries, entry.getKey(), entry.getValue(), output);
        });
    }

    @Override
    public String getName() {
        return "spellbook_chapters.java";
    }

    private void generateCode(RegistryWrapper.WrapperLookup registries, Identifier key, SpellbookChapter entry, Consumer<String> lineConsumer) {
        var basename = key.getPath().split("_")[0];
        var elementName = ELEMENT_NAMES.getOrDefault(basename, basename);
        var chapterIdVarName = elementName + "ChapterId";
        String chapterElementVarName;
        lineConsumer.accept(String.format("var %s = Unicopia.id(\"%s\");", chapterIdVarName, key.getPath()));
        if (!key.getPath().contentEquals(elementName)) {
            chapterElementVarName = elementName + "ElementId";
            lineConsumer.accept(String.format("var %s = Unicopia.id(\"%s\");", chapterElementVarName, elementName));
        } else {
            chapterElementVarName = chapterIdVarName;
        }
        lineConsumer.accept(String.format("exporter.accept(%s, builder().side(TabSide.%s).tabY(%d)", chapterIdVarName, entry.side().name(), entry.tabY()));
        entry.pages().forEach(page -> {
            if (page.elements().isEmpty()) {
                lineConsumer.accept(INDENT + INDENT + ".page(page())");
            } else {
                lineConsumer.accept(INDENT + INDENT + String.format(".page(page().title(%s))%s%s",
                        codeForText(key, chapterIdVarName, page.title()),
                        page.level() == 0 ? "" : String.format(".level(%d)", page.level()),
                        page.color() == 0 ? "" : String.format(".color(%d)", page.color())
                ));
                for (int i = 0; i < page.elements().size(); i++) {
                    var element = page.elements().get(i);
                    if (i < page.elements().size() - 1) {
                        if (element.equals(AUTHOR_1_SIGN_OFF) && page.elements().get(i + 1).equals(AUTHOR_1_NAME)) {
                            lineConsumer.accept(INDENT + INDENT + INDENT + ".apply(SpellbookChapterProvider::applyAuthor1Signature))");
                            i++;
                            continue;
                        }
                        if (element.equals(AUTHOR_1_SIGN_OFF_B) && page.elements().get(i + 1).equals(AUTHOR_1_NAME)) {
                            lineConsumer.accept(INDENT + INDENT + INDENT + ".apply(SpellbookChapterProvider::applyAuthor1AltSignature))");
                            i++;
                            continue;
                        }
                    }
                    lineConsumer.accept(INDENT + INDENT + INDENT + String.format(".element(%s)", codeForElement(registries, key, chapterIdVarName, elementName, chapterElementVarName, element)));
                }
                lineConsumer.accept(INDENT + INDENT + ")");
            }
        });
        lineConsumer.accept(INDENT + INDENT + ".build()");
        lineConsumer.accept(");");
    }

    private String codeForElement(RegistryWrapper.WrapperLookup registries, Identifier chapterId, String chapterIdVarName, String elementName, String elementVarName, ChapterPageElement element) {
        if (element instanceof ChapterPageElement.TextBlock block) {
            return codeForTextBlock(chapterId, chapterIdVarName, elementName, elementVarName, block);
        } else if (element instanceof ChapterPageElement.Image image) {
            return codeForImage(chapterId, chapterIdVarName, image);
        }
        try {
            return "/*" + ChapterPageElement.CODEC.encodeStart(registries.getOps(JsonOps.INSTANCE), element).getOrThrow().toString() + "*/";
        } catch (Exception e) {
            return "/*" + e.getMessage() + "*/";
        }
    }

    private String codeForImage(Identifier chapterId, String chapterIdVarName, ChapterPageElement.Image image) {
        if (image.texture().equals(chapterId.withPath(p -> "textures/gui/container/pages/" + p + ".png"))) {
            return String.format("pageIcon(%s)", chapterIdVarName);
        }

        return String.format("new ChapterPageElement.Image(%s, %s, Flow.%s)", createIdentifierReference(image.texture()), codeForBounds(image.bounds()), image.flow().name());
    }

    private String codeForBounds(Bounds bounds) {
        if (bounds.equals(CHAPTER_ICON_BOUNDS)) {
            return "CHAPTER_ICON_BOUNDS";
        }
        return String.format("new Bounds(%d, %d, %d, %d)", bounds.top, bounds.left, bounds.width, bounds.height);
    }

    private String codeForTextBlock(Identifier chapterId, String chapterIdVarName, String elementName, String elementVarName, ChapterPageElement.TextBlock text) {
        if (text.equals(AUTHOR_1_SIGN_OFF)) {
            return "AUTHOR_1_SIGN_OFF";
        }
        if (text.equals(AUTHOR_1_SIGN_OFF_B)) {
            return "AUTHOR_1_SIGN_OFF_B";
        }
        if (text.equals(AUTHOR_1_NAME)) {
            return "AUTHOR_1_NAME";
        }
        if (text.text().getContent() instanceof TranslatableTextContent c && c.getFallback() == null) {
            String key = c.getKey();

            var paragraphMatcher = Pattern.compile("gui\\." + chapterId.getNamespace() + "\\.spellbook\\.chapter\\." + elementName + "\\.p([0-9]+)\\.([0-9]+)\\.body").matcher(key);
            if (paragraphMatcher.hasMatch()) {
                return String.format("paragraph(%s, %s, %s)", elementVarName, paragraphMatcher.group(1), paragraphMatcher.group(2));
            }
            var titleMatcher = Pattern.compile("gui\\." + chapterId.getNamespace() + "\\.spellbook\\.chapter\\." + chapterId.getPath() + "\\.p([0-9]+)\\.title").matcher(key);
            if (titleMatcher.hasMatch()) {
                return String.format("new ChapterPageElement.TextBlock(pageTitle(%s, %s))", chapterIdVarName, titleMatcher.group(1));
            }
            return String.format("text(\"%s\")", key);
        }

        return String.format("new ChapterPageElement.TextBlock(%s)", codeForText(chapterId, chapterIdVarName, text.text()));
    }

    private String codeForText(Identifier chapterId, String chapterIdVarName, Text text) {
        if (text.getString().isEmpty()) {
            return "Text.empty()";
        }
        if (text.getContent() instanceof TranslatableTextContent c) {
            String key = c.getKey();
            if (c.getFallback() != null) {
                return String.format("Text.translatableWithFallback(\"%s\"", key, c.getFallback());
            }
            var titleMatcher = Pattern.compile("gui\\." + chapterId.getNamespace() + "\\.spellbook\\.chapter\\." + chapterId.getPath() + "\\.p([0-9]+)\\.title").matcher(key);
            if (titleMatcher.hasMatch()) {
                return String.format("pageTitle(%s, %s)", chapterIdVarName, titleMatcher.group(1));
            }
            return String.format("Text.translatable(\"%s\"", key);
        }

        return String.format("Text.literal(\"%s\"", text.getString());
    }

    static ChapterPageElement text(String translationKey) {
        return new ChapterPageElement.TextBlock(Text.translatable(translationKey));
    }

    private String createIdentifierReference(Identifier id) {
        if (id.getNamespace().equals(Unicopia.DEFAULT_NAMESPACE)) {
            return String.format("Unicopia.id(\"%s\")", id.getPath());
        }
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return String.format("Identifier.ofVanilla(\"%s\")", id.getPath());
        }
        if (id.getNamespace().equals(FarmersDelightContent.DEFAULT_NAMESPACE)) {
            return String.format("FarmersDelightContent.id(\"%s\")", id.getPath());
        }
        return String.format("Identifier.of(\"%s\", \"%s\")", id.getNamespace(), id.getPath());
    }
}
