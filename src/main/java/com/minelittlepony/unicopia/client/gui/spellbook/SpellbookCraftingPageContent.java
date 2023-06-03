package com.minelittlepony.unicopia.client.gui.spellbook;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.item.URecipes;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpellbookCraftingPageContent extends ScrollContainer implements SpellbookChapterList.Content, SpellbookScreen.RecipesChangedListener {
    public static final Text INVENTORY_TITLE = Text.translatable("gui.unicopia.spellbook.page.inventory");
    public static final Text RECIPES_TITLE = Text.translatable("gui.unicopia.spellbook.page.recipes");
    public static final int TOTAL_PAGES = 2;

    private final SpellbookScreen screen;

    private SpellbookState.PageState state = new SpellbookState.PageState();

    public SpellbookCraftingPageContent(SpellbookScreen screen) {
        this.screen = screen;
        backgroundColor = 0xFFf9efd3;
        verticalScrollbar.layoutToEnd = true;
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        state = screen.getState().getState(pageId);
        screen.addPageButtons(187, 300, 350, incr -> {
            state.swap(incr, TOTAL_PAGES);
        });
        initContents();
        screen.addDrawable(this);
        ((IViewRoot)screen).getChildElements().add(this);
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {

        int headerColor = mouseY % 255;

        DrawableUtil.drawScaledText(context, state.getOffset() == 0 ? INVENTORY_TITLE : RECIPES_TITLE, screen.getFrameBounds().left + screen.getFrameBounds().width / 2 + 20, SpellbookScreen.TITLE_Y, 1.3F, headerColor);

        Text pageText = Text.translatable("%s/%s", state.getOffset() + 1, TOTAL_PAGES);
        context.drawText(textRenderer, pageText, (int)(337 - textRenderer.getWidth(pageText) / 2F), 190, headerColor, false);
    }

    @Override
    public void onRecipesChanged() {
        initContents();
    }

    public void initContents() {
        init(this::initPageContent);
    }

    @Override
    public boolean showInventory() {
        return state.getOffset() == 0;
    }

    private void initPageContent() {
        getContentPadding().setVertical(10);
        getContentPadding().bottom = 30;

        if (state.getOffset() == 1) {
            int top = 0;
            for (SpellbookRecipe recipe : this.client.world.getRecipeManager().listAllOfType(URecipes.SPELLBOOK)) {
                if (client.player.getRecipeBook().contains(recipe)) {
                    IngredientTree tree = new IngredientTree(0, top, width - verticalScrollbar.getBounds().width + 2);
                    recipe.buildCraftingTree(tree);
                    top += tree.build(this);
                }
            }
            if (top == 0) {
                addButton(new Label(width / 2, 0).setCentered()).getStyle().setText("gui.unicopia.spellbook.page.recipes.empty");
            }
        }
    }

    @Override
    public void init(Runnable contentInitializer) {
        if (screen != null) {
            margin.left = screen.getX() + screen.getBackgroundWidth() / 2 + 10;
            margin.top = screen.getY() + 35;
            margin.right = screen.width - screen.getBackgroundWidth() - screen.getX() + 30;
            margin.bottom = screen.height - screen.getBackgroundHeight() - screen.getY() + 40;
        }
        super.init(contentInitializer);
    }

    @Override
    public void drawOverlays(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(margin.left, margin.top, 0);
        matrices.translate(-2, -2, 200);
        RenderSystem.enableBlend();
        int tileSize = 25;

        final int bottom = height - tileSize + 4;
        final int right = width - tileSize + 9;

        context.drawTexture(SpellbookScreen.TEXTURE, 0, 0, 405, 62, tileSize, tileSize, 512, 256);
        context.drawTexture(SpellbookScreen.TEXTURE, right, 0, 425, 62, tileSize, tileSize, 512, 256);

        context.drawTexture(SpellbookScreen.TEXTURE, 0, bottom, 405, 72, tileSize, tileSize, 512, 256);
        context.drawTexture(SpellbookScreen.TEXTURE, right, bottom, 425, 72, tileSize, tileSize, 512, 256);

        for (int i = tileSize; i < right; i += tileSize) {
            context.drawTexture(SpellbookScreen.TEXTURE, i, 0, 415, 62, tileSize, tileSize, 512, 256);
            context.drawTexture(SpellbookScreen.TEXTURE, i, bottom, 415, 72, tileSize, tileSize, 512, 256);
        }

        for (int i = tileSize; i < bottom; i += tileSize) {
            context.drawTexture(SpellbookScreen.TEXTURE, 0, i, 405, 67, tileSize, tileSize, 512, 256);
            context.drawTexture(SpellbookScreen.TEXTURE, right, i, 425, 67, tileSize, tileSize, 512, 256);
        }
        matrices.pop();
        screen.drawSlots(context, mouseX, mouseY, tickDelta);

        super.drawOverlays(context, mouseX, mouseY, tickDelta);
    }
}
