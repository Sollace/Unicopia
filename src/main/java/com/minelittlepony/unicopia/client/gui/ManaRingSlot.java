package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;

import net.minecraft.client.util.math.MatrixStack;

class ManaRingSlot extends Slot {

    public ManaRingSlot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y) {
        super(uHud, normalSlot, backupSlot, x, y, 8, UHud.PRIMARY_SLOT_SIZE, 33, 43, 42);
        background(0, 5);
        foreground(0, 59);
    }

    @Override
    protected void renderContents(MatrixStack matrices, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        matrices.push();
        matrices.translate(24.5, 25.5, 0);

        Pony pony = Pony.of(uHud.client.player);
        MagicReserves mana = pony.getMagicalReserves();

        double arcBegin = 0;

        arcBegin = renderRing(matrices, 17, 13, 0, mana.getMana(), 0xFF88FF99, tickDelta);

        if (!uHud.client.player.isCreative()) {
            renderRing(matrices, 13, 11, 0, mana.getXp(), 0x88880099, tickDelta);

            double cost = abilities.getStats().stream()
                    .mapToDouble(s -> s.getCost(KeyBindingsHandler.INSTANCE.page))
                    .reduce(Double::sum)
                    .getAsDouble();

            if (cost > 0) {
                float percent = mana.getMana().getPercentFill();
                float max = mana.getMana().getMax();

                cost *= 10;
                cost /= 1 + pony.getLevel().getScaled(3);

                int color = cost / max > percent ? 0xFF000099 : 0xFFFF0099;

                cost = Math.min(percent, Math.min(max, cost) / max);

                double angle = cost * Math.PI * 2;

                DrawableUtil.drawArc(matrices, 13, 17, arcBegin - angle, angle, color, false);
            }
        }

        arcBegin = renderRing(matrices, 17, 13, arcBegin, mana.getExhaustion(), 0xFF002299, tickDelta);

        matrices.pop();

        super.renderContents(matrices, abilities, bSwap, tickDelta);
    }

    private double renderRing(MatrixStack matrices, double outerRadius, double innerRadius, double offsetAngle, Bar bar, int color, float tickDelta) {
        double fill = bar.getPercentFill() * DrawableUtil.TAU;
        double shadow = bar.getShadowFill() * DrawableUtil.TAU;

        DrawableUtil.drawArc(matrices, innerRadius, outerRadius, offsetAngle, fill, color, true);

        if (shadow > fill) {
            color = (color & 0xFFFFFF00)
                 | ((color & 0x000000FF) / 2);

            DrawableUtil.drawArc(matrices, innerRadius, outerRadius, offsetAngle + fill, shadow - fill, color, false);
        }
        return offsetAngle + fill;
    }
}
