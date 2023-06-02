package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.Optional;
import java.util.function.IntConsumer;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.gui.*;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.container.inventory.*;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpellbookStateChanged;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class SpellbookScreen extends HandledScreen<SpellbookScreenHandler> implements RecipeBookProvider {
    public static final Identifier TEXTURE = Unicopia.id("textures/gui/container/book.png");
    public static final Identifier SLOT = Unicopia.id("textures/gui/container/slot.png");
    public static final Identifier GEM = Unicopia.id("textures/item/gemstone.png");

    private static final int CONTENT_PADDING = 30;

    public static final int TITLE_X = 30;
    public static final int TITLE_Y = 20;
    public static final int TITLE_COLOR = 0xFF404040;

    private final RecipeBookWidget recipeBook = new RecipeBookWidget();

    private final Chapter craftingChapter;
    private final SpellbookTraitDexPageContent traitDex = new SpellbookTraitDexPageContent(this);
    private final SpellbookChapterList chapters = new SpellbookChapterList(this,
        craftingChapter = new Chapter(SpellbookChapterList.CRAFTING_ID, TabSide.LEFT, 0, 0, Optional.of(new SpellbookCraftingPageContent(this))),
        new Chapter(SpellbookChapterList.PROFILE_ID, TabSide.LEFT, 1, 0, Optional.of(new SpellbookProfilePageContent(this))),
        new Chapter(SpellbookChapterList.TRAIT_DEX_ID, TabSide.LEFT, 3, 0, Optional.of(traitDex))
    );
    private final SpellbookTabBar tabs = new SpellbookTabBar(this, chapters);

    private Bounds contentBounds = Bounds.empty();

    public SpellbookScreen(SpellbookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 405;
        backgroundHeight = 219;
        contentBounds = new Bounds(CONTENT_PADDING, CONTENT_PADDING, backgroundWidth - CONTENT_PADDING * 2, backgroundHeight - CONTENT_PADDING * 3 - 2);

        handler.addSlotShowingCondition(slotType -> {
            if (slotType == SlotType.INVENTORY) {
                return chapters.getCurrentChapter().content().filter(Content::showInventory).isPresent();
            }
            return chapters.getCurrentChapter() == craftingChapter;
        });
        handler.getSpellbookState().setSynchronizer(state -> {
            Channel.CLIENT_SPELLBOOK_UPDATE.sendToServer(new MsgSpellbookStateChanged<ServerPlayerEntity>(handler.syncId, state));
        });
    }

    public SpellbookState getState() {
        return handler.getSpellbookState();
    }

    public SpellbookTraitDexPageContent getTraitDex() {
        return traitDex;
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
        chapters.getCurrentChapter().content().ifPresent(content -> content.init(this, chapters.getCurrentChapter().id()));
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
        if (getState().isDirty()) {
            clearAndInit();
        }
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        renderBackground(matrices);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight, 512, 256);

        if (Debug.DEBUG_SPELLBOOK_CHAPTERS) {
            clearAndInit();
        }

        tabs.getAllTabs().forEach(tab -> {
            Bounds bounds = tab.bounds();
            chapters.getCurrentChapter();
            boolean hover = bounds.contains(mouseX, mouseY);

            int color = tab.chapter().color() & 0xFFFFFF;

            int v = 100 + (hover ? 24 : 0);
            if (color == 0xFFFFFF || color == 0) {
                v += 48;
            } else {
                RenderSystem.setShaderColor(ColorHelper.Abgr.getRed(color) / 255F, ColorHelper.Abgr.getGreen(color) / 255F, ColorHelper.Abgr.getBlue(color) / 255F, 1);
            }

            boolean isRight = tab.chapter().side() == TabSide.RIGHT;

            drawTexture(matrices, bounds.left, bounds.top, isRight ? 510 - bounds.width : 402, v, bounds.width, bounds.height, 512, 256);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            RenderSystem.setShaderTexture(0, tab.icon().get());
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
            if (slot.isEnabled() && slot instanceof SpellbookSlot p) {
                drawTexture(matrices, slot.x - 8, slot.y - 8, 0, 0, 32, 32, 32, 32);

                if (slot instanceof InputSlot) {
                    RenderSystem.setShaderColor(1, 1, 1, 0.3F);
                    RenderSystem.setShaderTexture(0, GEM);
                    drawTexture(matrices, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    RenderSystem.setShaderTexture(0, SLOT);
                }

                if (!(p instanceof InventorySlot)) {
                    float weight = p.getWeight();
                    ItemTraitsTooltipRenderer.renderStackTraits(slot.getStack(), matrices, slot.x, slot.y, weight == 0 ? 1 : weight, delta, slot.id);
                    RenderSystem.setShaderTexture(0, SLOT);
                    RenderSystem.enableBlend();
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
                getState().setCurrentPageId(tab.chapter().id());
                GameGui.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN);
                clearAndInit();
                return true;
            }

            return false;
        }) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void clearAndInit() {
        super.clearAndInit();
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
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);

            RenderSystem.setShaderColor(1, 1, 1, alpha);
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(
                    GlStateManager.SrcFactor.SRC_ALPHA,
                    GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

            if (getStyle().hasIcon()) {
                getStyle().getIcon().render(matrices, getX(), getY(), mouseX, mouseY, tickDelta);
            }

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    public interface RecipesChangedListener {
        void onRecipesChanged();
    }
}
