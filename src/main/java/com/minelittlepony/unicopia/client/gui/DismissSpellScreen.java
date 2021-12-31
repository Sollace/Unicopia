package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.FlowingText;
import com.minelittlepony.unicopia.client.particle.SphereModel;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vector4f;

public class DismissSpellScreen extends GameGui {
    private final Pony pony = Pony.of(MinecraftClient.getInstance().player);

    private final List<Spell> spells;

    private final List<Entry> entries = new ArrayList<>();

    private double lastMouseX;
    private double lastMouseY;

    public DismissSpellScreen() {
        super(new TranslatableText("gui.unicopia.dismiss_spell"));

        spells = pony.getSpellSlot().stream(true).toList();
    }

    @Override
    protected void init() {
        entries.clear();

        double azimuth = 0;
        int ring = 1;

        for (Spell spell : spells) {

            double increment = Math.PI / (8 * ring);

            azimuth += increment;
            if (azimuth > Math.PI) {
                azimuth = 0;
                ring++;
            }

            double radius = 75 * ring - 25;

            Vector4f pos = SphereModel.convertToCartesianCoord(radius, azimuth, azimuth);
            pos.add(0,  -(float)radius / 2F, 0, 0);

            entries.add(new Entry(spell, pos));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fillGradient(matrices, 0, 0, width, height / 2, 0xF0101010, 0x80101010);
        fillGradient(matrices, 0, height / 2, width, height, 0x80101010, 0xF0101010);

        super.render(matrices, mouseX, mouseY, delta);

        matrices.push();
        matrices.translate(width - mouseX, height - mouseY, 0);

        mouseX = -width + mouseX * 2;
        mouseY = -height + mouseY * 2;

        DrawableUtil.drawLine(matrices, 0, 0, mouseX, mouseY, 0xFFFFFF88);
        DrawableUtil.drawArc(matrices, 40, 80, 0, Math.PI * 2, 0x00000010, false);
        DrawableUtil.drawArc(matrices, 160, 1600, 0, Math.PI * 2, 0x00000020, false);

        for (Entry entry : entries) {
            entry.render(this, matrices, mouseX, mouseY);
        }

        matrices.push();
        DrawableUtil.drawCircle(matrices, 2, 0, Math.PI * 2, 0xFFAAFF99, false);
        matrices.translate(mouseX, mouseY, 0);
        DrawableUtil.drawLine(matrices, 0, -4, 0, 4, 0xFFAAFF99);
        DrawableUtil.drawLine(matrices, -4, 0, 4, 0, 0xFFAAFF99);
        matrices.pop();

        matrices.pop();

        matrices.push();
        matrices.translate(0, 0, 300);
        Text cancel = new LiteralText("Press ESC to cancel");
        getFont().drawWithShadow(matrices, cancel, (width - getFont().getWidth(cancel)) / 2, height - 30, 0xFFFFFFFF);
        matrices.pop();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    class Entry {

        private final List<Text> tooltip = new ArrayList<>();

        private final Spell spell;
        private final Vector4f pos;

        private boolean lastMouseOver;

        public Entry(Spell spell, Vector4f pos) {
            this.spell = spell;
            this.pos = pos;

            MutableText name = spell.getType().getName().shallowCopy();
            name.setStyle(name.getStyle().withColor(spell.getType().getColor()));

            tooltip.add(new TranslatableText("Spell Type: %s", name));
            spell.getType().getTraits().appendTooltip(tooltip);
            tooltip.add(LiteralText.EMPTY);
            tooltip.add(new TranslatableText("Affinity: %s", spell.getAffinity().name()).formatted(spell.getAffinity().getColor()));
            tooltip.add(LiteralText.EMPTY);
            tooltip.addAll(FlowingText.wrap(new TranslatableText(spell.getType().getTranslationKey() + ".lore").formatted(spell.getAffinity().getColor()), 180).toList());
            tooltip.add(LiteralText.EMPTY);
            tooltip.add(new TranslatableText("[Click to Discard]"));
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return squareDistance(mouseX, mouseY, pos.getX(), pos.getY()) < 75;
        }

        public void render(DismissSpellScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
            Vector4f copy = new Vector4f(pos.getX(), pos.getY(), pos.getZ(), pos.getW());
            copy.transform(matrices.peek().getPositionMatrix());

            boolean hovered = isMouseOver(mouseX, mouseY);

            MatrixStack modelStack = RenderSystem.getModelViewStack();
            modelStack.push();
            float scale = 1.1F;
            modelStack.translate(-21, -14, 0);
            modelStack.scale(scale, scale, 1);
            RenderSystem.applyModelViewMatrix();

            screen.client.getItemRenderer().renderGuiItemIcon(spell.getType().getDefualtStack(), (int)copy.getX() - 8, (int)copy.getY() - 8);

            modelStack.pop();
            RenderSystem.applyModelViewMatrix();

            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), 0);

            int color = spell.getType().getColor() << 2;

            DrawableUtil.drawArc(matrices, 7, 8, 0, Math.PI * 2, color | 0x00000088, false);

            if (hovered) {
                DrawableUtil.drawArc(matrices, 0, 8, 0, Math.PI * 2, color | 0x000000FF, false);
                screen.renderTooltip(matrices, tooltip, 0, 0);

                if (!lastMouseOver) {
                    lastMouseOver = true;
                    playClickEffect();
                }
            } else {
                lastMouseOver = false;
            }

            matrices.pop();
        }
    }

    static void playClickEffect() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 6, 0.3F));
    }

    static double squareDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx * dx + dy * dy);
    }
}
