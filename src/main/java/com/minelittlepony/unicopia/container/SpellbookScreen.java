package com.minelittlepony.unicopia.container;

import java.util.Optional;
import java.util.function.IntConsumer;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.container.SpellbookChapterList.*;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpellbookScreen extends HandledScreen<SpellbookScreenHandler> implements RecipeBookProvider {
    public static final Identifier TEXTURE = Unicopia.id("textures/gui/container/book.png");
    public static final Identifier SLOT = Unicopia.id("textures/gui/container/slot.png");
    public static final Identifier GEM = Unicopia.id("textures/item/gemstone.png");

    private static final int CONTENT_PADDING = 30;

    public static final int TITLE_X = 30;
    public static final int TITLE_Y = 20;
    public static final int TITLE_COLOR = 0xFF404040;

    private final RecipeBookWidget recipeBook = new RecipeBookWidget();

    private final Chapter craftingChapter = new Chapter(SpellbookChapterList.CRAFTING_ID, TabSide.LEFT, 0, 0, Optional.of(new SpellbookCraftingPageContent(this)));
    private final Chapter profileChapter = new Chapter(SpellbookChapterList.PROFILE_ID, TabSide.LEFT, 1, 0, Optional.of(new SpellbookProfilePageContent(this)));
    private final Chapter traitdexChapter = new Chapter(SpellbookChapterList.TRAIT_DEX_ID, TabSide.LEFT, 3, 0, Optional.of(new SpellbookTraitDexPageContent(this)));

    private final SpellbookChapterList chapters = new SpellbookChapterList(craftingChapter, profileChapter, traitdexChapter);
    private final SpellbookTabBar tabs = new SpellbookTabBar(this, chapters);

    private Bounds contentBounds = Bounds.empty();

    public SpellbookScreen(SpellbookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 405;
        backgroundHeight = 219;
        contentBounds = new Bounds(CONTENT_PADDING, CONTENT_PADDING, backgroundWidth - CONTENT_PADDING * 2, backgroundHeight - CONTENT_PADDING * 3 - 2);

        handler.addSlotShowingCondition(slotType -> {
            if (slotType == SlotType.INVENTORY) {
                return chapters.getCurrentChapter() == profileChapter
                   || (chapters.getCurrentChapter() == craftingChapter && SpellbookPage.getCurrent() == SpellbookPage.INVENTORY);
            }
            return chapters.getCurrentChapter() == craftingChapter;
        });
    }

    public void addPageButtons(int buttonY, int prevX, int nextX, IntConsumer pageAction) {
        addDrawableChild(new PageButton(this, x + nextX, y + buttonY, 1, pageAction));
        addDrawableChild(new PageButton(this, x + prevX, y + buttonY, -1, pageAction));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getBackgroundWidth() {
        return backgroundWidth;
    }

    public int getBackgroundHeight() {
        return backgroundHeight;
    }

    public Bounds getFrameBounds() {
        return contentBounds;
    }

    @Override
    public <T extends Drawable> T addDrawable(T drawable) {
        return super.addDrawable(drawable);
    }

    @Override
    public void init() {
        super.init();
        tabs.init();
        chapters.getCurrentChapter().content().ifPresent(content -> content.init(this));
    }

    @Override
    public void refreshRecipeBook() {
        chapters.getCurrentChapter()
            .content()
            .map(i -> i instanceof RecipesChangedListener ? (RecipesChangedListener)i : null)
            .ifPresent(RecipesChangedListener::onRecipesChanged);
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return recipeBook;
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

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight, 512, 256);

        tabs.getAllTabs().forEach(tab -> {
            Bounds bounds = tab.bounds();
            chapters.getCurrentChapter();
            boolean hover = bounds.contains(mouseX, mouseY);

            int color = tab.chapter().color() & 0xFFFFFF;

            int v = 100 + (hover ? 24 : 0);
            if (color == 0xFFFFFF || color == 0) {
                v += 48;
            } else {
                RenderSystem.setShaderColor(NativeImage.getRed(color), NativeImage.getGreen(color), NativeImage.getBlue(color), 1);
            }

            boolean isRight = tab.chapter().side() == TabSide.RIGHT;

            drawTexture(matrices, bounds.left, bounds.top, isRight ? 510 - bounds.width : 402, v, bounds.width, bounds.height, 512, 256);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            RenderSystem.setShaderTexture(0, tab.icon());
            drawTexture(matrices, isRight ? bounds.left + bounds.width - 16 - 10 : bounds.left + 10, bounds.top + (bounds.height - 16) / 2, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderTexture(0, TEXTURE);
        });

        matrices.push();
        matrices.translate(x, y, 0);
        chapters.getCurrentChapter().content().ifPresent(content -> content.draw(matrices, mouseX, mouseY, (IViewRoot)this));
        matrices.pop();
    }

    void drawSlots(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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
                    RenderSystem.setShaderTexture(0, GEM);
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
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tabs.getAllTabs().anyMatch(tab -> {

            if (tab.bounds().contains(mouseX, mouseY) && chapters.getCurrentChapter() != tab.chapter()) {
                chapters.setCurrentChapter(tab.chapter());
                GameGui.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN);
                clearAndInit();
                return true;
            }

            return false;
        }) || super.mouseClicked(mouseX, mouseY, button);
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

    private static class PageButton extends ImageButton {
        private final int increment;
        private final TextureSprite sprite = new TextureSprite()
                .setSize(25, 13)
                .setTextureSize(512, 256)
                .setTextureOffset(0, 479)
                .setTexture(TEXTURE);

        public PageButton(SpellbookScreen screen, int x, int y, int increment, IntConsumer pageAction) {
            super(x, y, 25, 20);
            this.increment = increment;
            getStyle().setIcon(sprite);
            onClick(sender -> {
                pageAction.accept(increment);
                screen.clearAndInit();
            });
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            if (!active) {
               return;
            }

            int state = hovered ? 1 : 0;

            sprite.setTextureOffset(23 * state, (int)(479 + 6.5F - (increment * 6.5F)));
            super.renderButton(matrices, mouseX, mouseY, tickDelta);
        }
    }

    static class ImageButton extends Button {

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

    public interface RecipesChangedListener {
        void onRecipesChanged();
    }
}
