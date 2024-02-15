package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import java.util.function.Function;

import com.minelittlepony.common.client.gui.*;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.client.TextHelper;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Chapter;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen.ImageButton;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SpellbookTraitDexPageContent implements SpellbookChapterList.Content, SpellbookScreen.RecipesChangedListener {
    private final Trait[] traits = Trait.values();
    private SpellbookState.PageState state = new SpellbookState.PageState();

    private final DexPage leftPage = new DexPage();
    private final DexPage rightPage = new DexPage();

    private final SpellbookScreen screen;

    private final Function<Identifier, Identifier> unreadIcon = Util.memoize(id -> Chapter.createIcon(id, "_unread"));

    public SpellbookTraitDexPageContent(SpellbookScreen screen) {
        this.screen = screen;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {

    }

    @Override
    public Identifier getIcon(Chapter chapter, Identifier icon) {
        if (Pony.of(MinecraftClient.getInstance().player).getDiscoveries().isUnread()) {
            return unreadIcon.apply(chapter.id());
        }
        return icon;
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        state = screen.getState().getState(pageId);

        int page = state.getOffset() * 2;
        leftPage.init(screen, page);
        rightPage.init(screen, page + 1);
        screen.addPageButtons(187, 30, 350, incr -> {
            state.swap(incr, (int)Math.ceil(traits.length / 2F));
            leftPage.verticalScrollbar.scrollBy(leftPage.verticalScrollbar.getScrubber().getPosition());
            rightPage.verticalScrollbar.scrollBy(rightPage.verticalScrollbar.getScrubber().getPosition());
        });
    }

    public void pageTo(SpellbookScreen screen, Trait trait) {
        int page = Arrays.binarySearch(traits, trait);
        if (page < 0) {
            return;
        }
        page /= 2;
        state = screen.getState().getState(SpellbookChapterList.TRAIT_DEX_ID);
        state.setOffset(page);
        leftPage.verticalScrollbar.scrollBy(leftPage.verticalScrollbar.getScrubber().getPosition());
        rightPage.verticalScrollbar.scrollBy(rightPage.verticalScrollbar.getScrubber().getPosition());

        GameGui.playSound(USounds.Vanilla.ITEM_BOOK_PAGE_TURN);
        screen.clearAndInit();
    }

    @Override
    public void onRecipesChanged() {
        init(screen, SpellbookChapterList.TRAIT_DEX_ID);
    }

    private final class DexPage extends ScrollContainer {
        public DexPage() {
            verticalScrollbar.layoutToEnd = true;
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
                        List<ItemStack> stacks = ItemGroupRegistry.getVariations(i);
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
            context.drawTexture(SpellbookScreen.TEXTURE, 0, bottom, 405, 72, tileSize, tileSize, 512, 256);

            for (int i = tileSize; i < right; i += tileSize) {
                context.drawTexture(SpellbookScreen.TEXTURE, i, 0, 415, 62, tileSize, tileSize, 512, 256);
                context.drawTexture(SpellbookScreen.TEXTURE, i, bottom, 415, 72, tileSize, tileSize, 512, 256);
            }

            for (int i = tileSize; i < bottom; i += tileSize) {
                context.drawTexture(SpellbookScreen.TEXTURE, 0, i, 405, 67, tileSize, tileSize, 512, 256);
                context.drawTexture(SpellbookScreen.TEXTURE, right, i, 425, 67, tileSize, tileSize, 512, 256);
            }

            context.drawTexture(SpellbookScreen.TEXTURE, right, 0, 425, 62, tileSize, tileSize, 512, 256);
            context.drawTexture(SpellbookScreen.TEXTURE, right, bottom, 425, 72, tileSize, tileSize, 512, 256);
            matrices.pop();

            if (this == rightPage) {
                leftPage.drawDelayed(context, mouseX, mouseY, 0);
                rightPage.drawDelayed(context, mouseX, mouseY, 0);
            }
        }

        public void drawDelayed(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            super.drawOverlays(context, mouseX, mouseY, tickDelta);
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
            getStyle().setTooltip(Tooltip.of(TextHelper.wrap(trait.getTooltip(), 200).toList()));

            onClick(sender -> Pony.of(MinecraftClient.getInstance().player).getDiscoveries().markRead(trait));
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            TraitDiscovery discoveries = Pony.of(MinecraftClient.getInstance().player).getDiscoveries();
            setEnabled(discoveries.isKnown(trait));

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            context.drawTexture(SpellbookScreen.TEXTURE, getX() - 2, getY() - 8, 204, 219, 22, 32, 512, 256);

            if (!active) {
                context.drawTexture(SpellbookScreen.TEXTURE, getX() - 2, getY() - 1, 74, 223, 18, 18, 512, 256);
            }

            if (discoveries.isUnread(trait)) {
                context.drawTexture(SpellbookScreen.TEXTURE, getX() - 8, getY() - 8, 225, 219, 35, 32, 512, 256);
            }

            super.renderButton(context, mouseX, mouseY, tickDelta);
            hovered &= active;
        }

        @Override
        public Button setEnabled(boolean enable) {
            alpha = enable ? 1 : 0.1125F;
            return super.setEnabled(enable);
        }
    }

}
