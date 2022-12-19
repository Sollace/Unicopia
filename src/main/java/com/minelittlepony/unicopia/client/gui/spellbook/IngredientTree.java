package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector4f;

import com.minelittlepony.common.client.gui.*;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;

class IngredientTree implements SpellbookRecipe.CraftingTreeBuilder {
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
    public void mystery(ItemStack... stacks) {
        if (stacks.length > 0) {
            entries.add(new HiddenStacks(stacks));
        }
    }

    @Override
    public void result(ItemStack...stacks) {
        if (stacks.length > 0) {
            result = Optional.of(new Stacks(stacks));
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

    class IngredientButton extends Button {
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
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, SpellbookScreen.SLOT);
            RenderSystem.enableBlend();

            drawTexture(matrices, getX() - 8, getY() - 10, 0, 0, 32, 32, 32, 32);

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            MinecraftClient.getInstance().textRenderer.draw(matrices, label,
                    getX() - MinecraftClient.getInstance().textRenderer.getWidth(label) / 2 - 3,
                    getY() + 4,
                    0
            );
            entry.render(matrices, getX(), getY(), tickDelta);
        }
    }

    interface Entry {
        void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta);

        Tooltip getTooltip();

        void onClick();
    }

    class Stacks implements IngredientTree.Entry {
        private int ticker;
        protected int index;
        protected final ItemStack[] stacks;

        protected final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        Stacks(ItemStack[] stacks) {
            this.stacks = stacks;
        }

        @Override
        public void render(MatrixStack matrices, int x, int y, float tickDelta) {
            y -= 2;

            if (ticker++ % 30 == 0) {
                index = (index + 1) % stacks.length;
            }

            Vector4f pos = new Vector4f(x, y, 0, 1);
            pos.mul(matrices.peek().getPositionMatrix());
            drawItem((int)pos.x, (int)pos.y);
        }

        protected void drawItem(int x, int y) {
            itemRenderer.renderInGui(stacks[index], x, y);
        }

        @Override
        public Tooltip getTooltip() {
            return () -> {
                if (stacks[index].isEmpty()) {
                    return List.of();
                }
                return stacks[index].getTooltip(MinecraftClient.getInstance().player, TooltipContext.Default.BASIC);
            };
        }

        @Override
        public void onClick() {

        }
    }

    class HiddenStacks extends Stacks {
        HiddenStacks(ItemStack[] stacks) {
            super(stacks);
        }

        @Override
        protected void drawItem(int x, int y) {
            var model = itemRenderer.getModel(stacks[index], null, null, 0);

            MinecraftClient.getInstance().getTextureManager().getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
            RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1, 1, 1, 0.2F);
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate(x, y, 100 + itemRenderer.zOffset);
            matrixStack.translate(8, 8, 0);
            matrixStack.scale(1, -1, 1);
            matrixStack.scale(8, 8, 8);
            RenderSystem.applyModelViewMatrix();
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            boolean bl = !model.isSideLit();
            if (bl) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            itemRenderer.renderItem(stacks[index], ModelTransformation.Mode.GUI, false, new MatrixStack(), immediate, 0, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();
            RenderSystem.enableDepthTest();
            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        @Override
        public Tooltip getTooltip() {
            return List::of;
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
            return Tooltip.of(ItemTraitsTooltipRenderer.isKnown(trait) ? trait.getTooltip() : trait.getObfuscatedTooltip(), 200);
        }

        @Override
        public void onClick() {
            if (MinecraftClient.getInstance().currentScreen instanceof SpellbookScreen spellbook) {
                spellbook.getState().setCurrentPageId(SpellbookChapterList.TRAIT_DEX_ID);
                spellbook.getTraitDex().pageTo(spellbook, trait);
            }
        }
    }
}