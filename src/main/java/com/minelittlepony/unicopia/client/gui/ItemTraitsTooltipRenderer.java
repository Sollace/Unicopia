package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;

public class ItemTraitsTooltipRenderer implements Text, OrderedText, TooltipComponent {

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
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        int columns = getColumns();
        int i = 0;

        for (var entry : traits) {
            renderTraitIcon(entry.getKey(), entry.getValue(), matrices,
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

    public static void renderStackTraits(ItemStack stack, MatrixStack matrices, float x, float y, float weight, float delta, int seed) {
        renderStackTraits(SpellTraits.of(stack), matrices, x, y, weight, delta, seed);
    }

    public static void renderStackTraits(SpellTraits traits, MatrixStack matrices, float x, float y, float weight, float delta, int seed) {
        float time = MathHelper.cos((MinecraftClient.getInstance().player.age + delta + seed) / 2F) * 0.7F;

        float angle = 0.7F + (time / 30F) % MathHelper.TAU;
        float angleIncrement = MathHelper.TAU / traits.entries().size();
        float r = 9 + 2 * MathHelper.sin(delta / 20F);

        for (var entry : traits) {
            if (isKnown(entry.getKey())) {
                ItemTraitsTooltipRenderer.renderTraitIcon(entry.getKey(), entry.getValue() * weight, matrices,
                        x + r * MathHelper.sin(angle),
                        y + r * MathHelper.cos(angle)
                );
                angle += angleIncrement;
            }
        }
    }

    private static boolean isKnown(Trait trait) {
        return MinecraftClient.getInstance().player == null
            || Pony.of(MinecraftClient.getInstance().player).getDiscoveries().isKnown(trait);
    }

    public static void renderTraitIcon(Trait trait, float value, MatrixStack matrices, float xx, float yy) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        int size = 12;

        if (!isKnown(trait)) {
            trait = Trait.values()[MinecraftClient.getInstance().player.getRandom().nextInt(Trait.all().size())];
        }

        RenderSystem.setShaderTexture(0, trait.getSprite());

        matrices.push();
        matrices.translate(xx, yy, itemRenderer.zOffset + 300.0F);

        DrawableHelper.drawTexture(matrices, 2, 1, 0, 0, 0, size, size, size, size);

        matrices.translate(9, 3 + size / 2, 0);
        matrices.scale(0.5F, 0.5F, 1);

        String count = value > 99 ? "99+" : Math.round(value) == value ? (int)value + "" : ((Math.round(value * 10) / 10F) + "");

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(count, 0, 0, 16777215, true, matrices.peek().getPositionMatrix(), immediate, false, 0, 15728880);
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
