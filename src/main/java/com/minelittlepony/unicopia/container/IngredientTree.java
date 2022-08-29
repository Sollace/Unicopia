package com.minelittlepony.unicopia.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vector4f;

class IngredientTree implements SpellbookRecipe.CraftingTreeBuilder {
    private final List<IngredientTree.Entry> entries = new ArrayList<>();
    private Optional<IngredientTree.Entry> result = Optional.empty();

    private final int x;
    private final int y;
    private final int width;

    public IngredientTree(int x, int y, int width, int height) {
        this.x = x + 4;
        this.y = y;
        this.width = width - 5;
    }

    @Override
    public void input(ItemStack... stacks) {
        if (stacks.length > 0) {
            entries.add(new Stacks(stacks));
        }
    }

    @Override
    public void input(Trait trait, float value) {
        if (value != 0) {
            entries.add(new Traits(trait, value));
        }
    }

    @Override
    public void result(ItemStack...stacks) {
        if (stacks.length > 0) {
            result = Optional.of(new Stacks(stacks));
        }
    }

    public int build(ScrollContainer container) {

        if (entries.isEmpty()) {
            return 0;
        }

        int ii = 0;

        int colWidth = 22;
        int rowHeight = 20;

        int cols = width / colWidth - 1;
        int rows = Math.max(1, (int)Math.ceil((float)entries.size() / cols));

        int totalHeight = rowHeight * rows;

        for (IngredientTree.Entry entry : entries) {
            int column = ii % cols;
            int row = ii / cols;

            int left = x + column * colWidth + 3 + (row > 0 ? colWidth : 0);
            int top = y + row * rowHeight + 3;

            container.addButton(new IngredientButton(left, top, colWidth, rowHeight, entry, ii == 0 ? "" : "+"));
            ii++;
        }
        result.ifPresent(result -> {
            container.addButton(new IngredientButton(x + width - 17, y + totalHeight / 3 - 2, colWidth, totalHeight, result, "="));
        });

        return totalHeight + 7;
    }

    class IngredientButton extends Button {
        private final IngredientTree.Entry entry;
        private String label;

        public IngredientButton(int x, int y, int width, int height, IngredientTree.Entry entry, String label) {
            super(x, y, width, height);
            this.entry = entry;
            this.label = label;
            this.getStyle().setTooltip(entry.getTooltip());
        }


        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, SpellbookScreen.SLOT);
            RenderSystem.enableBlend();

            drawTexture(matrices, x - 8, y - 10, 0, 0, 32, 32, 32, 32);

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            MinecraftClient.getInstance().textRenderer.draw(matrices, label,
                    x - MinecraftClient.getInstance().textRenderer.getWidth(label) / 2 - 3,
                    y + 4,
                    0
            );
            entry.render(matrices, x, y, tickDelta);
        }
    }

    interface Entry {
        void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta);

        Tooltip getTooltip();
    }

    class Stacks implements IngredientTree.Entry {
        private int ticker;
        private int index;
        private final ItemStack[] stacks;

        private final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        Stacks(ItemStack[] stacks) {
            this.stacks = stacks;
        }

        @Override
        public void render(MatrixStack matrices, int x, int y, float tickDelta) {
            y -= 2;

            if (ticker++ % 500 == 0) {
                index = (index + 1) % stacks.length;
            }
            float z = itemRenderer.zOffset;
            itemRenderer.zOffset = -100;

            Vector4f vector4f = new Vector4f(x, y, z, 1);
            vector4f.transform(matrices.peek().getPositionMatrix());

            itemRenderer.renderInGui(stacks[index], (int)vector4f.getX(), (int)vector4f.getY());
            itemRenderer.zOffset = z;
        }

        @Override
        public Tooltip getTooltip() {
            return () -> stacks[index].getTooltip(MinecraftClient.getInstance().player, TooltipContext.Default.NORMAL);
        }
    }

    class Traits implements IngredientTree.Entry {
        private final Trait trait;
        private final float value;

        Traits(Trait trait, float value) {
            this.trait = trait;
            this.value = value;
        }

        @Override
        public void render(MatrixStack matrices, int x, int y, float tickDelta) {
            ItemTraitsTooltipRenderer.renderTraitIcon(trait, value, matrices, x, y);
        }

        @Override
        public Tooltip getTooltip() {
            return trait.getTooltip();
        }
    }
}