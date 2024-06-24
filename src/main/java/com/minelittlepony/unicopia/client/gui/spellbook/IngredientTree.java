package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.client.util.render.RenderLayerUtil;
import com.minelittlepony.common.client.gui.*;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.*;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.container.SpellbookState;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class IngredientTree implements SpellbookRecipe.CraftingTreeBuilder {
    private final List<IngredientTree.Entry> entries = new ArrayList<>();
    private Optional<IngredientTree.Entry> result = Optional.empty();

    private final int x;
    private final int y;
    private final int width;

    private boolean addLabels = true;
    private boolean obfuscateResult;

    public IngredientTree(int x, int y, int width) {
        this.x = x + 4;
        this.y = y;
        this.width = width - 5;
    }

    public IngredientTree noLabels() {
        addLabels = false;
        return this;
    }

    public IngredientTree obfuscateResult(boolean obfuscateResult) {
        this.obfuscateResult = obfuscateResult;
        return this;
    }

    @Override
    public void input(ItemStack... stacks) {
        if (stacks.length > 0) {
            entries.add(Entry.of(stacks));
        }
    }

    @Override
    public void input(Trait... traits) {
        if (traits.length > 0) {
            entries.add(Entry.of(1, traits));
        }
    }

    @Override
    public void input(Trait trait, float value) {
        if (value != 0) {
            entries.add(new Traits(trait, value));
        }
    }

    @Override
    public void mystery(ItemStack... stacks) {
        if (stacks.length > 0) {
            entries.add(Multiple.of(Arrays.stream(stacks).map(HiddenStacks::new).toArray(Entry[]::new)));
        }
    }

    @Override
    public void result(ItemStack...stacks) {
        if (stacks.length > 0) {
            result = Optional.of(Entry.of(stacks));
        }
    }

    public int build(IViewRoot container) {

        if (entries.isEmpty()) {
            return 0;
        }

        int ii = 0;

        int colWidth = 22;
        int rowHeight = 20;

        int cols = Math.max(1, width / colWidth - 1);
        int rows = Math.max(1, (int)Math.ceil((float)entries.size() / cols));

        int totalHeight = rowHeight * rows;

        for (IngredientTree.Entry entry : entries) {
            int column = ii % cols;
            int row = ii / cols;

            int left = x + column * colWidth + 3 + (addLabels && row > 0 ? colWidth : 0);
            int top = y + row * rowHeight + 3;

            container.addButton(new IngredientButton(left, top, colWidth, rowHeight, entry, !addLabels || ii == 0 ? "" : "+", false)).onClick(sender -> entry.onClick());

            ii++;
        }
        result.ifPresent(result -> {
            container.addButton(new IngredientButton(x + width - 17, y + totalHeight / 3 - 2, colWidth, totalHeight, result, addLabels ? "=" : "", obfuscateResult));
        });

        return totalHeight + 7;
    }

    static class IngredientButton extends Button {
        private final IngredientTree.Entry entry;
        private String label;

        public IngredientButton(int x, int y, int width, int height, IngredientTree.Entry entry, String label, boolean obfuscated) {
            super(x, y, width, height);
            this.entry = entry;
            this.label = label;
            Tooltip tooltip = entry.getTooltip();
            if (tooltip != null) {
                this.getStyle().setTooltip(obfuscated ? Tooltip.of("???") : tooltip);
            }
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();

            context.drawTexture(SpellbookScreen.SLOT, getX() - 8, getY() - 10, 0, 0, 32, 32, 32, 32);

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            context.drawText(getFont(), label,
                    getX() - MinecraftClient.getInstance().textRenderer.getWidth(label) / 2 - 3,
                    getY() + 4,
                    0,
                    false
            );
            entry.render(context, getX(), getY(), tickDelta);
        }
    }

    interface Entry {

        static Entry of(ItemStack... stacks) {
            return Multiple.of(Arrays.stream(stacks).map(Stacks::new).toArray(Entry[]::new));
        }

        static Entry of(float value, Trait... traits) {
            return Multiple.of(Arrays.stream(traits).map(t -> new Traits(t, value)).toArray(Entry[]::new));
        }

        void render(DrawContext context, int mouseX, int mouseY, float tickDelta);

        Tooltip getTooltip();

        void onClick();
    }

    static class Multiple implements IngredientTree.Entry {
        private int ticker;
        protected int index;
        protected final IngredientTree.Entry[] entries;

        static final IngredientTree.Entry EMPTY = new IngredientTree.Entry() {

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {}

            @Override
            public void onClick() { }

            @Override
            public Tooltip getTooltip() {
                return List::of;
            }
        };

        static IngredientTree.Entry of(IngredientTree.Entry... entries) {
            if (entries.length == 0) {
                return EMPTY;
            }
            if (entries.length == 1) {
                return entries[0];
            }
            return new Multiple(entries);
        }

        Multiple(IngredientTree.Entry[] entries) {
            this.entries = entries;
        }

        @Override
        public void render(DrawContext context, int x, int y, float tickDelta) {
            y -= 2;

            if (ticker++ % 30 == 0) {
                index = (index + 1) % entries.length;
            }

            entries[index].render(context, x, y, tickDelta);
        }

        @Override
        public Tooltip getTooltip() {
            return () -> entries[index].getTooltip().getLines();
        }

        @Override
        public void onClick() {
            entries[index].onClick();
        }
    }

    static class Stacks implements IngredientTree.Entry {

        protected final ItemStack stack;

        protected final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        Stacks(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void render(DrawContext context, int x, int y, float tickDelta) {
            drawItem(context, x, y - 2);
        }

        protected void drawItem(DrawContext context, int x, int y) {
            context.drawItem(stack, x, y);
        }

        @Override
        public Tooltip getTooltip() {
            return () -> {
                if (stack.isEmpty()) {
                    return List.of();
                }
                return stack.getTooltip(MinecraftClient.getInstance().player, TooltipContext.Default.BASIC);
            };
        }

        @Override
        public void onClick() {

        }
    }

    static class HiddenStacks extends Stacks {
        HiddenStacks(ItemStack stack) {
            super(stack);
        }

        @Override
        protected void drawItem(DrawContext context, int x, int y) {
            var model = itemRenderer.getModel(stack, null, null, 0);

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(x, y, 100);
            matrices.translate(8, 8, 0);
            matrices.scale(1, -1, 1);
            matrices.scale(8, 8, 8);
            VertexConsumerProvider.Immediate immediate = context.getVertexConsumers();
            boolean bl = !model.isSideLit();
            if (bl) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            RenderSystem.disableDepthTest();
            itemRenderer.renderItem(stack, ModelTransformationMode.GUI, false, matrices, layer -> {
                return immediate.getBuffer(RenderLayerUtil.getTexture(layer).map(texture -> {
                    return RenderLayers.getMagicColored(texture, 0x09000000);
                }).orElse(RenderLayers.getMagicColored(0x09000000)));
            }, 0, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();

            RenderSystem.enableDepthTest();
            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrices.pop();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        @Override
        public Tooltip getTooltip() {
            return List::of;
        }
    }

    static class Traits implements IngredientTree.Entry {
        private final Trait trait;
        private final float value;

        Traits(Trait trait, float value) {
            this.trait = trait;
            this.value = value;
        }

        @Override
        public void render(DrawContext context, int x, int y, float tickDelta) {
            ItemTraitsTooltipRenderer.renderTraitIcon(trait, value, context, x, y);
        }

        @Override
        public Tooltip getTooltip() {
            return Tooltip.of(ItemTraitsTooltipRenderer.isKnown(trait) ? trait.getTooltip() : trait.getObfuscatedTooltip(), 200);
        }

        @Override
        public void onClick() {
            if (MinecraftClient.getInstance().currentScreen instanceof SpellbookScreen spellbook) {
                spellbook.getState().setCurrentPageId(SpellbookState.TRAIT_DEX_ID);
                spellbook.getTraitDex().pageTo(spellbook, trait);
            }
        }
    }
}