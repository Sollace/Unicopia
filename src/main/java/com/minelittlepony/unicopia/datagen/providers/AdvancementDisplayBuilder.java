package com.minelittlepony.unicopia.datagen.providers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
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

    public AdvancementDisplayBuilder criterion(String name, CriterionConditions conditions) {
        advancementBuilder.criterion(name, new AdvancementCriterion(conditions));
        return this;
    }

    public AdvancementDisplayBuilder criteriaMerger(CriterionMerger merger) {
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

    public Parent build(Consumer<Advancement> exporter, String name) {
        Identifier id = Unicopia.id(group == null ? name : group + "/" + name);
        String key = Util.createTranslationKey("advancements", Unicopia.id(name));
        Advancement advancement = advancementBuilder.display(new AdvancementDisplay(icon.asItem().getDefaultStack(),
                Text.translatable(key + ".title"),
                Text.translatable(key + ".description"), background, frame, toast, announce, hidden))
                .build(id);
        exporter.accept(advancement);
        return new Parent(advancement, group);
    }

    public record Parent(Advancement parent, @Nullable String group) {
        public AdvancementDisplayBuilder child(ItemConvertible icon) {
            return new AdvancementDisplayBuilder(icon, Advancement.Builder.create().parent(parent),
                    parent.getDisplay().shouldShowToast(),
                    parent.getDisplay().shouldAnnounceToChat(),
                    parent.getDisplay().isHidden()
            ).frame(parent.getDisplay().getFrame()).background(parent.getDisplay().getBackground()).group(group);
        }

        public void children(Consumer<Parent> children) {
            children.accept(this);
        }

        public void children(Consumer<Advancement> exporter, BiConsumer<Consumer<Advancement>, Parent> children) {
            children.accept(exporter, this);
        }
    }
}
