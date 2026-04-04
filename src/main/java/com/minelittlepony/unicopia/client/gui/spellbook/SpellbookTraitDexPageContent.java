package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.common.client.gui.*;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.client.TextHelper;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Chapter;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen.ImageButton;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

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
        state = screen.getState().getState(SpellbookState.TRAIT_DEX_ID);
        state.setOffset(page);
        leftPage.verticalScrollbar.scrollBy(leftPage.verticalScrollbar.getScrubber().getPosition());
        rightPage.verticalScrollbar.scrollBy(rightPage.verticalScrollbar.getScrubber().getPosition());

        GameGui.playSound(USounds.Vanilla.ITEM_BOOK_PAGE_TURN);
        screen.clearAndInit();
    }

    @Override
    public void onRecipesChanged() {
        init(screen, SpellbookState.TRAIT_DEX_ID);
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
            margin.right = screen.width - screen.getBackgroundWidth() - screen.getX() + 25;
            margin.bottom = screen.height - screen.getBackgroundHeight() - screen.getY() + 40;

            if (page % 2 == 1) {
                margin.left += screen.getBackgroundWidth() / 2 - 15;
            } else {
                margin.right += screen.getBackgroundWidth() / 2 - 15;
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
            screen.getChildElements().add(this);
        }


        @Override
        public void drawOverlays(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(margin.left, margin.top, 0);
            matrices.translate(-2, -2, 200);
            int tileSize = 25;

            final int bottom = height - tileSize + 4;
            final int right = width - tileSize + 9;

            context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, 0, 0, 405, 62, tileSize, tileSize, 512, 256);
            context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, 0, bottom, 405, 72, tileSize, tileSize, 512, 256);

            for (int i = tileSize; i < right; i += tileSize) {
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, i, 0, 415, 62, tileSize, tileSize, 512, 256);
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, i, bottom, 415, 72, tileSize, tileSize, 512, 256);
            }

            for (int i = tileSize; i < bottom; i += tileSize) {
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, 0, i, 405, 67, tileSize, tileSize, 512, 256);
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, right, i, 425, 67, tileSize, tileSize, 512, 256);
            }

            context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, right, 0, 425, 62, tileSize, tileSize, 512, 256);
            context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, right, bottom, 425, 72, tileSize, tileSize, 512, 256);
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

        private final Style revealedStyle = new Style();
        private final Style hiddenStyle = new Style();

        public TraitButton(int x, int y, Trait trait) {
            super(x, y, 16, 16);
            this.trait = trait;
            revealedStyle
                .setIcon(this::renderIcon)
                .setTooltip(Tooltip.of(TextHelper.wrap(trait.getTooltip(), 200).toList()));
            hiddenStyle
                .setTooltip(Tooltip.of(TextHelper.wrap(trait.getObfuscatedTooltip(), 200).toList()));

            onClick(sender -> Pony.of(MinecraftClient.getInstance().player).getDiscoveries().markRead(trait));
        }

        private void renderIcon(DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks) {
            float amplify = Math.abs(MathHelper.sin((MinecraftClient.getInstance().player.age + partialTicks) / 80F) * 8);
            float x1 = 0;
            float x2 = 16 + amplify * 2;
            float y1 = 0;
            float y2 = 16 + amplify * 2;
            float z = 0;

            context.getMatrices().push();
            context.getMatrices().translate(x - amplify, y - amplify, 0);

            Matrix4f posMat = context.getMatrices().peek().getPositionMatrix();
            Vector3f normal = context.getMatrices().peek().transformNormal(-1, -1, -1, new Vector3f());

            context.getMatrices().pop();

            Vector4f vec = new Vector4f();

            context.draw(vertices -> {
                VertexConsumer buffer = VertexConsumers.union(
                        new OverlayVertexConsumer(vertices.getBuffer(RenderLayer.getGlint()), context.getMatrices().peek(), 0.000078125F),
                        vertices.getBuffer(RenderLayer.getEntityCutout(trait.getSprite()))
                );

                int color = 0xFFFFFFFF;
                posMat.transform(vec.set(x1, y1, z));
                buffer.vertex(vec.x(), vec.y(), vec.z(), color, 0, 0, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, normal.x(), normal.y(), normal.z());
                posMat.transform(vec.set(x1, y2, z));
                buffer.vertex(vec.x(), vec.y(), vec.z(), color, 0, 1, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, normal.x(), normal.y(), normal.z());
                posMat.transform(vec.set(x2, y2, z));
                buffer.vertex(vec.x(), vec.y(), vec.z(), color, 1, 1, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, normal.x(), normal.y(), normal.z());
                posMat.transform(vec.set(x2, y1, z));
                buffer.vertex(vec.x(), vec.y(), vec.z(), color, 1, 0, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, normal.x(), normal.y(), normal.z());
            });
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            hovered = isMouseOver(mouseX, mouseY);
            TraitDiscovery discoveries = Pony.of(MinecraftClient.getInstance().player).getDiscoveries();
            boolean known = discoveries.isKnown(trait);
            setStyle(known ? revealedStyle : hiddenStyle);

            context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, getX() - 2, getY() - 8, 204, 219, 22, 32, 512, 256);

            if (!known) {
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, getX() - 2, getY() - 1, 74, 223, 18, 18, 512, 256);
            }

            if (discoveries.isUnread(trait)) {
                context.drawTexture(RenderLayer::getGuiTextured, SpellbookScreen.TEXTURE, getX() - 8, getY() - 8, 225, 219, 35, 32, 512, 256);
            }

            super.renderWidget(context, mouseX, mouseY, tickDelta);
            hovered &= active;
        }

        @Override
        public Button setEnabled(boolean enable) {
            alpha = enable ? 1 : 0.1125F;
            return super.setEnabled(enable);
        }
    }

}
