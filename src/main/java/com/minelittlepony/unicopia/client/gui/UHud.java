package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class UHud extends DrawableHelper {

    public static final UHud instance = new UHud();

    public static final Identifier HUD_TEXTURE = new Identifier("unicopia", "textures/gui/hud.png");

    public TextRenderer font;

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final List<Slot> slots = Util.make(new ArrayList<>(), slots -> {
        slots.add(new Slot(this, AbilitySlot.PRIMARY, AbilitySlot.PASSIVE, 0, 0, 8, 49, 38, 42).background(0, 5).foreground(0, 59));
        slots.add(new Slot(this, AbilitySlot.SECONDARY, AbilitySlot.SECONDARY, 26, -5, 3, 22, 17, 19).background(80, 105));
        slots.add(new Slot(this, AbilitySlot.TERTIARY, AbilitySlot.TERTIARY, 36, 19, 3, 22, 17, 19).background(80, 105));
    });

    public void render(InGameHud hud, MatrixStack matrices, float tickDelta) {

        if (client.player == null || client.player.isSpectator()) {
            return;
        }

        font = client.textRenderer;

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        matrices.push();
        matrices.translate(104 + (scaledWidth - 50) / 2, 20 + scaledHeight - 70, 0);

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();

        client.getTextureManager().bindTexture(HUD_TEXTURE);

        AbilityDispatcher abilities = Pony.of(client.player).getAbilities();

        boolean swap = client.options.keySneak.isPressed();

        slots.forEach(slot -> slot.renderBackground(matrices, abilities, swap, tickDelta));

        renderManaRings(matrices);

        slots.forEach(slot -> slot.renderForeground(matrices, abilities, tickDelta));

        slots.forEach(slot -> slot.renderLabel(matrices, abilities, tickDelta));

        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();

        matrices.pop();
    }

    void renderAbilityIcon(MatrixStack matrices, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility().ifPresent(ability -> {
            Identifier id = Abilities.REGISTRY.getId(ability);
            client.getTextureManager().bindTexture(new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + ".png"));

            drawTexture(matrices, x, y, 0, 0, frameWidth, frameHeight, u, v);

            client.getTextureManager().bindTexture(HUD_TEXTURE);
        });
    }

    void renderManaRings(MatrixStack matrices) {
        matrices.push();
        matrices.translate(24.5, 25.5, 0);

        MagicReserves mana = Pony.of(client.player).getMagicalReserves();

        double arcBegin = 0;

        arcBegin = renderRing(matrices, 17, 13, 0, mana.getMana(), 0xFF88FF99);
        arcBegin = renderRing(matrices, 17, 13, arcBegin, mana.getEnergy(), 0xFF002299);

        matrices.pop();
    }

    private double renderRing(MatrixStack matrices, double outerRadius, double innerRadius, double offsetAngle, Bar bar, int color) {
        double fill = bar.getPercentFill() * Math.PI * 2;

        renderArc(matrices, innerRadius, outerRadius, offsetAngle, fill, color, false);
        return offsetAngle + fill;
    }

    /**
     * Renders a colored arc.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void renderArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        float f = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float h = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;

        final double num_rings = 300;
        final double twoPi = Math.PI * 2;
        final double increment = twoPi / num_rings;

        if (arcAngle < increment) {
            return;
        }

        final double maxAngle = MathHelper.clamp(startAngle + arcAngle, 0, twoPi - increment);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

        Matrix4f model = matrices.peek().getModel();

        if (!mirrorHorizontally) {
            startAngle = -startAngle;
        }

        for (double angle = startAngle; angle >= -maxAngle; angle -= increment) {
            // center
            bufferBuilder.vertex(model,
                    (float)(innerRadius * Math.sin(angle)),
                    (float)(innerRadius * Math.cos(angle)), 0).color(f, g, h, k).next();

            // point one
            bufferBuilder.vertex(model,
                    (float)(outerRadius * Math.sin(angle)),
                    (float)(outerRadius * Math.cos(angle)), 0).color(f, g, h, k).next();

            // point two
            bufferBuilder.vertex(model,
                    (float)(outerRadius * Math.sin(angle + increment)),
                    (float)(outerRadius * Math.cos(angle + increment)), 0).color(f, g, h, k).next();

            // back to center
            bufferBuilder.vertex(model,
                    (float)(innerRadius * Math.sin(angle + increment)),
                    (float)(innerRadius * Math.cos(angle + increment)), 0).color(f, g, h, k).next();

        }

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        RenderSystem.enableTexture();
    }
}
