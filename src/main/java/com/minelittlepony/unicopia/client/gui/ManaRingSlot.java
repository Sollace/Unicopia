package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

class ManaRingSlot extends Slot {

    public ManaRingSlot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y) {
        super(uHud, normalSlot, backupSlot, x, y, 8, UHud.PRIMARY_SLOT_SIZE, 33, 43, 30);
        background(0, 5);
        foreground(0, 59);
    }

    @Override
    protected void renderContents(DrawContext context, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        MatrixStack matrices = context.getMatrices();



        matrices.push();
        matrices.translate(24.125, 24.75, 0);

        Pony pony = Pony.of(uHud.client.player);
        MagicReserves mana = pony.getMagicalReserves();

        boolean canUseSuper = Abilities.RAGE.canUse(pony.getCompositeRace()) || Abilities.RAINBOOM.canUse(pony.getCompositeRace());

        double maxManaBarSize = canUseSuper ? DrawableUtil.PI : DrawableUtil.TAU;
        double arcBegin = renderRing(matrices, 17, 13, 0, maxManaBarSize, mana.getMana(), 0xFF88FF99, tickDelta);
        renderRing(matrices, 17, 13, 0, maxManaBarSize, mana.getExhaustion(), 0xFF002299, tickDelta);

        if (!uHud.client.player.isCreative()) {

            int level = pony.getLevel().get();
            int seconds = level % 16;
            int minutes = (level / 16) % 16;
            int hours = level / 32;

            DrawableUtil.drawNotchedArc(matrices, 10, 13, 0, hours * 0.2, 0.1, 0.1, 0x00AA88FF);
            DrawableUtil.drawNotchedArc(matrices, 10, 13, hours * 0.2, minutes * 0.2, 0.1, 0.1, 0x008888AA);
            DrawableUtil.drawNotchedArc(matrices, 10, 13, (hours + minutes) * 0.2, seconds * 0.2, 0.1, 0.1, 0x88880099);

            level = pony.getCorruption().get();
            seconds = level % 16;
            minutes = (level / 16) % 16;
            hours = level / 32;

            DrawableUtil.drawNotchedArc(matrices, 7, 10,  DrawableUtil.PI, hours * 0.2, 0.1, 0.1, 0x000088FF);
            DrawableUtil.drawNotchedArc(matrices, 7, 10, hours * 0.2 + DrawableUtil.PI, minutes * 0.2, 0.1, 0.1, 0x000088AA);
            DrawableUtil.drawNotchedArc(matrices, 7, 10, (hours + minutes) * 0.2 + DrawableUtil.PI, seconds * 0.2, 0.1, 0.1, 0x00008899);

            if (canUseSuper) {
                renderRing(matrices, 17, 13, Math.min(arcBegin, DrawableUtil.PI), Math.max(DrawableUtil.PI, DrawableUtil.TAU - arcBegin), mana.getCharge(), 0x88FF9999, tickDelta);
            }

            double cost = abilities.getStats().stream()
                    .mapToDouble(s -> s.getCost(Unicopia.getConfig().hudPage.get()))
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

                DrawableUtil.drawArc(matrices, 13, 17, arcBegin - angle, angle, color);
            }
        }

        matrices.pop();
        super.renderContents(context, abilities, bSwap, tickDelta);
    }

    private double renderRing(MatrixStack matrices, double outerRadius, double innerRadius, double offsetAngle, double maxAngle, Bar bar, int color, float tickDelta) {
        double fill = bar.getPercentFill(tickDelta) * maxAngle;
        double shadow = bar.getShadowFill(tickDelta) * maxAngle;

        DrawableUtil.drawArc(matrices, innerRadius, outerRadius, offsetAngle, fill, color);

        if (shadow > fill) {
            color = (color & 0xFFFFFF00)
                 | ((color & 0x000000FF) / 2);

            DrawableUtil.drawArc(matrices, innerRadius, outerRadius, offsetAngle + fill, shadow - fill, color);
        }
        return offsetAngle + fill;
    }
}
