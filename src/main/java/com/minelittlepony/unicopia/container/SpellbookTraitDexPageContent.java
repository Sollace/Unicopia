package com.minelittlepony.unicopia.container;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SpellbookTraitDexPageContent extends DrawableHelper implements SpellbookChapterList.Content, SpellbookScreen.RecipesChangedListener {

    private final Trait[] traits = Trait.all().toArray(Trait[]::new);
    private int offset;

    private final DexPage leftPage = new DexPage();
    private final DexPage rightPage = new DexPage();

    private final SpellbookScreen screen;

    public SpellbookTraitDexPageContent(SpellbookScreen screen) {
        this.screen = screen;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {

    }

    @Override
    public void init(SpellbookScreen screen) {
        int page = offset * 2;
        leftPage.init(screen, page);
        rightPage.init(screen, page + 1);
        screen.addPageButtons(187, 30, 350, incr -> {
            offset = MathHelper.clamp(offset + incr, 0, (int)Math.ceil(traits.length / 2F) - 1);
        });
    }

    @Override
    public void onRecipesChanged() {
        init(screen);
    }

    private final class DexPage extends ScrollContainer {
        public DexPage() {
            scrollbar.layoutToEnd = true;
            backgroundColor = 0xFFf9efd3;
        }

        public void init(SpellbookScreen screen, int page) {
            if (page < 0 || page >= traits.length) {
                return;
            }

            margin.left = screen.getX() + 20;
            margin.top = screen.getY() + 15;
            margin.right = screen.width - screen.getBackgroundWidth() - screen.getX() + 20;
            margin.bottom = screen.height - screen.getBackgroundHeight() - screen.getY() + 40;

            if (page % 2 == 1) {
                margin.left += screen.getBackgroundWidth() / 2;
            } else {
                margin.right += screen.getBackgroundWidth() / 2 - 5;
            }

            init(() -> {
                Trait trait = traits[page];

                boolean known = Pony.of(MinecraftClient.getInstance().player).getDiscoveries().isKnown(trait);

                addButton(new SpellbookScreen.TraitButton(width / 2 - 8, 8, trait));
                addButton(new Label(width / 2, 26).setCentered())
                    .getStyle()
                        .setText(known ? Text.translatable("gui.unicopia.trait.label",
                                Text.translatable("trait." + trait.getId().getNamespace() + "." + trait.getId().getPath() + ".name")
                ) : Text.literal("???"));
                IngredientTree tree = new IngredientTree(0, 50, width).noLabels();
                Pony.of(MinecraftClient.getInstance().player).getDiscoveries().getKnownItems(trait).forEach(i -> tree.input(i.getDefaultStack()));
                tree.build(this);
            });
            screen.addDrawable(this);
            ((IViewRoot)screen).getChildElements().add(this);
        }


        @Override
        public void drawOverlays(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            matrices.push();
            matrices.translate(margin.left, margin.top, 0);
            matrices.translate(-2, -2, 0);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
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

            if (this == rightPage) {
                leftPage.drawDelayed(matrices, mouseX, mouseY, 0);
                rightPage.drawDelayed(matrices, mouseX, mouseY, 0);
            }
        }

        public void drawDelayed(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            super.drawOverlays(matrices, mouseX, mouseY, tickDelta);
        }
    }
}
