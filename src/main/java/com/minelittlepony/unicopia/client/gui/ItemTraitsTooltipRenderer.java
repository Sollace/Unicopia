package com.minelittlepony.unicopia.client.gui;

import java.util.Map.Entry;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.BaseText;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;

public class ItemTraitsTooltipRenderer extends BaseText implements OrderedText, TooltipComponent {

    private final SpellTraits traits;

    public ItemTraitsTooltipRenderer(SpellTraits traits) {
        this.traits = traits;
    }

    @Override
    public int getHeight() {
        return getRows() * 8 + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return getColumns() * 17 + 2;
    }

    private int getColumns() {
       return Math.max(4, (int)Math.ceil(Math.sqrt(traits.entries().size() + 1)));
    }

    private int getRows() {
       return (int)Math.ceil((traits.entries().size() + 1) / getColumns());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {

        int columns = getColumns();

        var entries = traits.stream().toList();

        for (int i = 0; i < entries.size(); i++) {
            int xx = x + (i % columns) * 17;
            int yy = y + (i / columns) * 10;
            Entry<Trait, Float> entry = entries.get(i);

            RenderSystem.setShaderTexture(0, entry.getKey().getSprite());
            DrawableHelper.drawTexture(matrices, xx, yy, 1, 0, 0, 8, 8, 8, 8);

            String string = entry.getValue() > 99 ? "99+" : Math.round(entry.getValue()) + "";
            matrices.push();

            matrices.translate(xx + 9, yy + 3, itemRenderer.zOffset + 200.0F);
            matrices.scale(0.5F, 0.5F, 1);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            textRenderer.draw(string, 0, 0, 16777215, true, matrices.peek().getPositionMatrix(), immediate, false, 0, 15728880);
            immediate.draw();
            matrices.pop();
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
    public BaseText copy() {
        return new ItemTraitsTooltipRenderer(traits);
    }
}
