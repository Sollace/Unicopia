package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.Comparator;
import java.util.List;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen.ImageButton;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

public class SpellbookTraitDexPageContent extends DrawableHelper implements SpellbookChapterList.Content, SpellbookScreen.RecipesChangedListener {

    private final Trait[] traits = Trait.values();
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
            leftPage.scrollbar.scrollBy(leftPage.scrollbar.getVerticalScrollAmount());
            rightPage.scrollbar.scrollBy(rightPage.scrollbar.getVerticalScrollAmount());
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
            getContentPadding().setVertical(10);
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

                addButton(new TraitButton(width / 2 - 8, 8, trait));
                addButton(new Label(width / 2, 26).setCentered())
                    .getStyle()
                        .setText(known ? Text.translatable("gui.unicopia.trait.label",
                                Text.translatable("trait." + trait.getId().getNamespace() + "." + trait.getId().getPath() + ".name")
                ) : Text.literal("???"));
                IngredientTree tree = new IngredientTree(0, 50, width + 18).noLabels();

                List<Item> knownItems = Pony.of(MinecraftClient.getInstance().player).getDiscoveries().getKnownItems(trait).toList();
                SpellTraits.getItems(trait)
                    .sorted(Comparator.comparing(u -> knownItems.contains(u) ? 0 : 1))
                    .forEach(i -> {
                        DefaultedList<ItemStack> stacks = DefaultedList.of();
                        i.appendStacks(ItemGroup.SEARCH, stacks);
                        if (knownItems.contains(i)) {
                            tree.input(stacks);
                        } else {
                            tree.mystery(stacks);
                        }
                    });
                tree.build(this);
            });
            screen.addDrawable(this);
            ((IViewRoot)screen).getChildElements().add(this);
        }


        @Override
        public void drawOverlays(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            matrices.push();
            matrices.translate(margin.left, margin.top, 0);
            matrices.translate(-2, -2, 200);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
            int tileSize = 25;

            final int bottom = height - tileSize + 4;
            final int right = width - tileSize + 9;

            drawTexture(matrices, 0, 0, 405, 62, tileSize, tileSize, 512, 256);
            drawTexture(matrices, 0, bottom, 405, 72, tileSize, tileSize, 512, 256);

            for (int i = tileSize; i < right; i += tileSize) {
                drawTexture(matrices, i, 0, 415, 62, tileSize, tileSize, 512, 256);
                drawTexture(matrices, i, bottom, 415, 72, tileSize, tileSize, 512, 256);
            }

            for (int i = tileSize; i < bottom; i += tileSize) {
                drawTexture(matrices, 0, i, 405, 67, tileSize, tileSize, 512, 256);
                drawTexture(matrices, right, i, 425, 67, tileSize, tileSize, 512, 256);
            }

            drawTexture(matrices, right, 0, 425, 62, tileSize, tileSize, 512, 256);
            drawTexture(matrices, right, bottom, 425, 72, tileSize, tileSize, 512, 256);
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

    static class TraitButton extends ImageButton {
        private final Trait trait;

        public TraitButton(int x, int y, Trait trait) {
            super(x, y, 16, 16);
            this.trait = trait;
            getStyle().setIcon(new TextureSprite()
                    .setTextureSize(16, 16)
                    .setSize(16, 16)
                    .setTexture(trait.getSprite()));
            getStyle().setTooltip(trait.getTooltip());

            onClick(sender -> Pony.of(MinecraftClient.getInstance().player).getDiscoveries().markRead(trait));
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            TraitDiscovery discoveries = Pony.of(MinecraftClient.getInstance().player).getDiscoveries();
            setEnabled(discoveries.isKnown(trait));

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
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

}
