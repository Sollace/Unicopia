package com.minelittlepony.unicopia.container;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitDiscovery;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler.InputSlot;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler.SpellbookSlot;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.URecipes;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SpellbookScreen extends HandledScreen<SpellbookScreenHandler> {
    public static final Identifier TEXTURE = new Identifier("unicopia", "textures/gui/container/book.png");
    public static final Identifier SLOT = new Identifier("unicopia", "textures/gui/container/slot.png");

    private final ScrollContainer container = new ScrollContainer() {
        {
            backgroundColor = 0xFFf9efd3;
            scrollbar.layoutToEnd = true;
        }
        @Override
        public void init(Runnable contentInitializer) {
            margin.left = SpellbookScreen.this.x + backgroundWidth / 2 + 10;
            margin.top = SpellbookScreen.this.y + 35;
            margin.right = SpellbookScreen.this.width - backgroundWidth - SpellbookScreen.this.x + 30;
            margin.bottom = SpellbookScreen.this.height - backgroundHeight - SpellbookScreen.this.y + 40;
            super.init(contentInitializer);
        }

        @Override
        public void drawOverlays(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            matrices.push();
            matrices.translate(margin.left, margin.top, 0);
            matrices.translate(-2, -2, 0);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, TEXTURE);
            int tileSize = 25;

            final int bottom = height - tileSize + 4;
            final int right = width - tileSize + 9;

            drawTexture(matrices, 0, 0, 405, 62, tileSize, tileSize, 512, 256);
            drawTexture(matrices, right, 0, 425, 62, tileSize, tileSize, 512, 256);

            drawTexture(matrices, 0, bottom, 405, 72, tileSize, tileSize, 512, 256);
            drawTexture(matrices, right, bottom, 425, 72, tileSize, tileSize, 512, 256);

            for (int i = tileSize; i < right; i += tileSize) {
                drawTexture(matrices, i, 0, 415, 62, tileSize, tileSize, 512, 256);
                drawTexture(matrices, i, bottom, 415, 72, tileSize, tileSize, 512, 256);
            }

            for (int i = tileSize; i < bottom; i += tileSize) {
                drawTexture(matrices, 0, i, 405, 67, tileSize, tileSize, 512, 256);
                drawTexture(matrices, right, i, 425, 67, tileSize, tileSize, 512, 256);
            }
            matrices.pop();
            drawSlots(matrices, mouseX, mouseY, tickDelta);

            super.drawOverlays(matrices, mouseX, mouseY, tickDelta);
        }
    };

    public SpellbookScreen(SpellbookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 405;
        backgroundHeight = 219;
        titleX = 30;
        titleY = 20;
    }

    @Override
    public void init() {
        super.init();
        addDrawableChild(new PageButton(x + 350, y + 187, 1));
        addDrawableChild(new PageButton(x + 300, y + 187, -1));
        container.init(this::initPageContent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        renderBackground(matrices, 0);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;

        drawTexture(matrices, left, top, 0, 0, backgroundWidth, backgroundHeight, 512, 256);
    }

    protected void drawSlots(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        matrices.translate(x, y, 0);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, SLOT);
        RenderSystem.enableBlend();

        for (Slot slot : handler.slots) {
            if (slot.isEnabled() && slot instanceof SpellbookSlot) {
                drawTexture(matrices, slot.x - 8, slot.y - 8, 0, 0, 32, 32, 32, 32);

                if (slot instanceof InputSlot) {
                    RenderSystem.setShaderColor(1, 1, 1, 0.3F);
                    RenderSystem.setShaderTexture(0, new Identifier("unicopia", "textures/item/gemstone.png"));
                    drawTexture(matrices, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    RenderSystem.setShaderTexture(0, SLOT);
                }
            }
        }
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        matrices.pop();
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        textRenderer.draw(matrices, title, titleX, titleY, 4210752);
        textRenderer.draw(matrices, SpellbookPage.getCurrent().getLabel(), 220, this.titleY, 4210752);

        Text pageText = new TranslatableText("%s/%s", SpellbookPage.getCurrent().ordinal() + 1, SpellbookPage.VALUES.length);
        textRenderer.draw(matrices, pageText,
                x + 325 - textRenderer.getWidth(pageText) / 2F, y + 188 - textRenderer.fontHeight, 4210752);
    }

    private void initPageContent() {
        container.getContentPadding().setVertical(10);
        container.getContentPadding().bottom = 30;
        addDrawable(container);
        ((IViewRoot)this).getChildElements().add(container);

        switch (SpellbookPage.getCurrent()) {
            case DISCOVERIES: {
                int i = 0;
                int cols = 4;

                int top = 10;
                int left = 25;

                for (Trait trait : Trait.all()) {
                    int x = i % cols;
                    int y = i / cols;

                    container.addButton(new TraitButton(left + x * 32, top + y * 32, trait));
                    i++;
                }
                break;
            }
            case INVENTORY:
                // handled elsewhere
                break;
            case RECIPES:
                int top = 0;
                for (SpellbookRecipe recipe : this.client.world.getRecipeManager().listAllOfType(URecipes.SPELLBOOK)) {
                    IngredientTree tree = new IngredientTree(0, top,
                            container.width - container.scrollbar.getBounds().width + 2,
                            20);
                    recipe.buildCraftingTree(tree);
                    top += tree.build(container);
                }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return hoveredElement(mouseX, mouseY).filter((element) -> {
            setDragging(false);
            return element.mouseReleased(mouseX, mouseY, button);
        }).isPresent() || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return (getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    class PageButton extends ImageButton {
        private final int increment;
        private final TextureSprite sprite = new TextureSprite()
                .setSize(25, 13)
                .setTextureSize(512, 256)
                .setTextureOffset(0, 479)
                .setTexture(TEXTURE);

        public PageButton(int x, int y, int increment) {
            super(x, y, 25, 20);
            this.increment = increment;
            getStyle().setIcon(sprite);
            onClick(sender -> {
                SpellbookPage.swap(increment);
                init(client, SpellbookScreen.this.width, SpellbookScreen.this.height);
            });
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {

            setEnabled(increment < 0 ? !SpellbookPage.getCurrent().isFirst() : !SpellbookPage.getCurrent().isLast());

            if (!active) {
               return;
            }

            int state = hovered ? 1 : 0;

            sprite.setTextureOffset(23 * state, (int)(479 + 6.5F - (increment * 6.5F)));
            super.renderButton(matrices, mouseX, mouseY, tickDelta);
        }
    }

    class TraitButton extends ImageButton {
        private final Trait trait;

        public TraitButton(int x, int y, Trait trait) {
            super(x, y, 16, 16);
            this.trait = trait;
            getStyle().setIcon(new TextureSprite()
                    .setTextureSize(16, 16)
                    .setSize(16, 16)
                    .setTexture(trait.getSprite()));
            getStyle().setTooltip(trait.getTooltip());

            onClick(sender -> Pony.of(client.player).getDiscoveries().markRead(trait));
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            TraitDiscovery discoveries = Pony.of(client.player).getDiscoveries();
            setEnabled(discoveries.isKnown(trait));

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.enableBlend();
            drawTexture(matrices, x - 2, y - 8, 204, 219, 22, 32, 512, 256);

            if (!active) {
                drawTexture(matrices, x - 2, y - 1, 74, 223, 18, 18, 512, 256);
            }

            if (discoveries.isUnread(trait)) {
                drawTexture(matrices, x - 8, y - 8, 225, 219, 35, 32, 512, 256);
            }

            super.renderButton(matrices, mouseX, mouseY, tickDelta);
            hovered &= active;
        }

        @Override
        public Button setEnabled(boolean enable) {
            alpha = enable ? 1 : 0.1125F;
            return super.setEnabled(enable);
        }
    }

    class ImageButton extends Button {

        public ImageButton(int x, int y) {
            super(x, y);
        }

        public ImageButton(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            RenderSystem.setShaderColor(1, 1, 1, alpha);
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(
                    GlStateManager.SrcFactor.SRC_ALPHA,
                    GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

            if (getStyle().hasIcon()) {
                getStyle().getIcon().render(matrices, x, y, mouseX, mouseY, tickDelta);
            }

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
