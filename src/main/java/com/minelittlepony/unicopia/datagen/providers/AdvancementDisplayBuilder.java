package com.minelittlepony.unicopia.datagen.providers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class AdvancementDisplayBuilder {
    private static final Identifier BACKGROUND = new Identifier("textures/gui/advancements/backgrounds/stone.png");

    public static AdvancementDisplayBuilder create(ItemConvertible icon) {
        return new AdvancementDisplayBuilder(icon, Advancement.Builder.create(), false, false, false);
    }

    private Identifier background = BACKGROUND;

    private boolean toast;
    private boolean hidden;
    private boolean announce;
    private final ItemConvertible icon;
    private AdvancementFrame frame = AdvancementFrame.TASK;
    @Nullable
    private String group;

    private final Advancement.Builder advancementBuilder;

    AdvancementDisplayBuilder(ItemConvertible icon, Advancement.Builder advancementBuilder, boolean toast, boolean announce, boolean hidden) {
        this.icon = icon;
        this.advancementBuilder = advancementBuilder;
        this.toast = toast;
        this.announce = announce;
        this.hidden = hidden;
    }

    public AdvancementDisplayBuilder frame(AdvancementFrame frame) {
        this.frame = frame;
        return this;
    }

    public AdvancementDisplayBuilder background(Identifier background) {
        this.background = background;
        return this;
    }

    public AdvancementDisplayBuilder showToast() {
        this.toast = true;
        return this;
    }

    public AdvancementDisplayBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public AdvancementDisplayBuilder visible() {
        this.hidden = false;
        return this;
    }

    public AdvancementDisplayBuilder announce() {
        this.announce = true;
        return this;
    }

    public AdvancementDisplayBuilder doNotAnnounce() {
        this.announce = false;
        return this;
    }

    public AdvancementDisplayBuilder group(String group) {
        this.group = group;
        return this;
    }

    public AdvancementDisplayBuilder rewards(AdvancementRewards.Builder builder) {
        advancementBuilder.rewards(builder.build());
        return this;
    }

    public AdvancementDisplayBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        advancementBuilder.criterion(name, criterion);
        return this;
    }

    public AdvancementDisplayBuilder criteriaMerger(AdvancementRequirements.CriterionMerger merger) {
        advancementBuilder.criteriaMerger(merger);
        return this;
    }

    public AdvancementDisplayBuilder parent(Identifier parent) {
        advancementBuilder.parent(Advancement.Builder.createUntelemetered().build(parent));
        return this;
    }

    public AdvancementDisplayBuilder apply(Consumer<AdvancementDisplayBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public Parent build(Consumer<AdvancementEntry> exporter, String name) {
        Identifier id = Unicopia.id(group == null ? name : group + "/" + name);
        String key = Util.createTranslationKey("advancements", Unicopia.id(name));
        AdvancementEntry advancement = advancementBuilder.display(new AdvancementDisplay(icon.asItem().getDefaultStack(),
                Text.translatable(key + ".title"),
                Text.translatable(key + ".description"), background, frame, toast, announce, hidden))
                .build(id);
        exporter.accept(advancement);
        return new Parent(advancement, group);
    }

    public record Parent(AdvancementEntry parent, @Nullable String group) {
        public AdvancementDisplayBuilder child(ItemConvertible icon) {
            AdvancementDisplay display = parent.value().display().orElseThrow();
            return new AdvancementDisplayBuilder(icon, Advancement.Builder.create().parent(parent),
                    display.shouldShowToast(),
                    display.shouldAnnounceToChat(),
                    display.isHidden()
            ).frame(display.getFrame()).background(display.getBackground()).group(group);
        }

        public void children(Consumer<Parent> children) {
            children.accept(this);
        }

        public void children(Consumer<AdvancementEntry> exporter, BiConsumer<Consumer<AdvancementEntry>, Parent> children) {
            children.accept(exporter, this);
        }
    }
}
