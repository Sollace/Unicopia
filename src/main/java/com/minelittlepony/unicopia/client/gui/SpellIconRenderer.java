package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public interface SpellIconRenderer {
    static void renderSpell(DrawContext context, CustomisedSpellType<?> spell, double x, double y) {
        renderSpell(context, spell, x, y, 1);
    }

    static void renderSpell(DrawContext context, CustomisedSpellType<?> spell, double x, double y, float scale) {
        if (spell.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        Pony pony = Pony.of(client.player);

        if (pony.getSpellSlot().contains(spell.and(SpellPredicate.IS_VISIBLE))) {
            MatrixStack modelStack = context.getMatrices();

            float ringScale = (0.7F + scale);

            modelStack.push();
            modelStack.translate(x + 8 * scale, y + 8 * scale, 0);

            int color = spell.type().getColor() | 0x000000FF;
            double radius = (1.5F + Math.sin(client.player.age / 9D) / 4) * ringScale;

            DrawableUtil.drawArc(modelStack, radius, radius + 3, 0, DrawableUtil.TAU, color & 0xFFFFFF2F);
            DrawableUtil.drawArc(modelStack, radius + 3, radius + 4, 0, DrawableUtil.TAU, color & 0xFFFFFFAF);
            pony.getSpellSlot().get(spell.and(SpellPredicate.IS_TIMED)).map(TimedSpell::getTimer).ifPresent(timer -> {
                DrawableUtil.drawArc(modelStack, radius, radius + 3, 0, DrawableUtil.TAU * timer.getPercentTimeRemaining(client.getRenderTickCounter().getTickDelta(false)), 0xFFFFFFFF);
            });

            long count = pony.getSpellSlot().stream(spell).count();
            if (count > 1) {
                modelStack.push();
                modelStack.translate(1, 1, 900);
                modelStack.scale(0.8F, 0.8F, 0.8F);
                context.drawText(client.textRenderer, count > 64 ? "64+" : String.valueOf(count), 0, 0, 0xFFFFFFFF, true);
                modelStack.pop();
            }

            modelStack.pop();
        }

        DrawableUtil.renderItemIcon(context, spell.getDefaultStack(), x, y, scale);
    }
}
