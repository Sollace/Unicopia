package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ItemTraitsTooltipRenderer implements Text, OrderedText, TooltipComponent {
    private static final Identifier UNKNOWN = Unicopia.id("textures/gui/trait/unknown.png");

    private final SpellTraits traits;

    public ItemTraitsTooltipRenderer(SpellTraits traits) {
        this.traits = traits;
    }

    @Override
    public int getHeight() {
        return getRows() * 17 + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return getColumns() * 17 + 2;
    }

    private int getColumns() {
        return Math.min(traits.entries().size(), Math.max(6, (int)Math.ceil(Math.sqrt(traits.entries().size() + 1))));
    }

    private int getRows() {
        int columns = getColumns();
        if (columns == traits.entries().size()) {
            return 1;
        }
        return Math.max(1, (int)Math.ceil((float)(traits.entries().size() + 1) / getColumns()));
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int columns = getColumns();
        int i = 0;

        for (var entry : traits) {
            renderTraitIcon(entry.getKey(), entry.getValue(), context,
                    x + (i % columns) * 17,
                    y + (i / columns) * 17
            );
            i++;
        }
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return false;
    }

    @Override
    public OrderedText asOrderedText() {
        return this;
    }

    @Override
    public MutableText copy() {
        return Text.empty();
    }

    public static void renderStackTraits(ItemStack stack, DrawContext context, float x, float y, float weight, float delta, int seed) {
        renderStackTraits(SpellTraits.of(stack), context, x, y, weight, delta, seed, false);
    }

    public static void renderStackTraits(SpellTraits traits, DrawContext context, float x, float y, float weight, float delta, int seed, boolean revealAll) {
        float time = MathHelper.cos((MinecraftClient.getInstance().player.age + delta + seed) / 2F) * 0.7F;

        float angle = 0.7F + (time / 30F) % MathHelper.TAU;
        float angleIncrement = MathHelper.TAU / traits.entries().size();
        float r = 9 + 2 * MathHelper.sin(delta / 20F);

        for (var entry : traits) {
            if (revealAll || isKnown(entry.getKey())) {
                ItemTraitsTooltipRenderer.renderTraitIcon(entry.getKey(), entry.getValue() * weight, context,
                        x + r * MathHelper.sin(angle),
                        y + r * MathHelper.cos(angle),
                        revealAll || isKnown(entry.getKey())
                );
                angle += angleIncrement;
            }
        }
    }

    public static boolean isKnown(Trait trait) {
        return MinecraftClient.getInstance().player == null
            || Pony.of(MinecraftClient.getInstance().player).getDiscoveries().isKnown(trait);
    }

    public static void renderTraitIcon(Trait trait, float value, DrawContext context, float xx, float yy) {
        renderTraitIcon(trait, value, context, xx, yy, isKnown(trait));
    }

    public static void renderTraitIcon(Trait trait, float value, DrawContext context, float xx, float yy, boolean reveal) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int size = 12;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(xx, yy, 300F);

        context.drawTexture(reveal ? trait.getSprite() : UNKNOWN, 2, 1, 0, 0, 0, size, size, size, size);

        matrices.translate(9, 3 + size / 2, 0);
        matrices.scale(0.5F, 0.5F, 1);

        String count = value > 99 ? "99+" : Math.round(value) == value ? (int)value + "" : ((Math.round(value * 10) / 10F) + "");

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
                // TODO: Before 1.21 was using tessellator's buffer but we can't get it without calling begin()
                //VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(count, 0, 0, 16777215, true, matrices.peek().getPositionMatrix(), immediate, TextLayerType.SEE_THROUGH, 0, 15728880);
        immediate.draw();
        matrices.pop();
    }

    @Override
    public Style getStyle() {
        return Text.empty().getStyle();
    }

    @Override
    public TextContent getContent() {
        return Text.empty().getContent();
    }

    @Override
    public List<Text> getSiblings() {
        return new ArrayList<>();
    }
}
