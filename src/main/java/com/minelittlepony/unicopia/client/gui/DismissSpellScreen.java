package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDelegatingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.FlowingText;
import com.minelittlepony.unicopia.client.particle.SphereModel;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRemoveSpell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
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

    private int relativeMouseX;
    private int relativeMouseY;

    public DismissSpellScreen() {
        super(new TranslatableText("gui.unicopia.dismiss_spell"));
    }

    @Override
    protected void init() {
        double azimuth = 0;
        double ring = 2;

        for (Spell spell : pony.getSpellSlot().stream(true).toList()) {
            addDrawableChild(new Entry(spell, 75 * ring - 25, azimuth));
            ring *= 1 + 0.03 / ring;
            azimuth += Math.PI / (8 * ring);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fillGradient(matrices, 0, 0, width, height / 2, 0xF0101010, 0x80101010);
        fillGradient(matrices, 0, height / 2, width, height, 0x80101010, 0xF0101010);

        relativeMouseX = -width + mouseX * 2;
        relativeMouseY = -height + mouseY * 2;

        matrices.push();
        matrices.translate(width - mouseX, height - mouseY, 0);
        DrawableUtil.drawLine(matrices, 0, 0, relativeMouseX, relativeMouseY, 0xFFFFFF88);
        DrawableUtil.drawArc(matrices, 40, 80, 0, DrawableUtil.TAU, 0x00000010, false);
        DrawableUtil.drawArc(matrices, 160, 1600, 0, DrawableUtil.TAU, 0x00000020, false);

        super.render(matrices, mouseX, mouseY, delta);

        DrawableUtil.drawCircle(matrices, 2, 0, DrawableUtil.TAU, 0xFFAAFF99, false);
        matrices.pop();

        DrawableUtil.drawLine(matrices, mouseX, mouseY - 4, mouseX, mouseY + 4, 0xFFAAFF99);
        DrawableUtil.drawLine(matrices, mouseX - 4, mouseY, mouseX + 4, mouseY, 0xFFAAFF99);

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

    class Entry extends Vector4f implements Element, Drawable, Selectable {

        private final List<Text> tooltip = new ArrayList<>();

        private final Spell spell;
        private final Spell actualSpell;

        private boolean lastMouseOver;

        private final Vector4f copy = new Vector4f();

        public Entry(Spell spell, double radius, double azimuth) {
            this.spell = spell;
            this.actualSpell = getActualSpell();

            SphereModel.convertToCartesianCoord(this, radius, azimuth, azimuth);
            add(0,  -(float)radius / 2F, 0, 0);

            MutableText name = actualSpell.getType().getName().shallowCopy();
            int color = actualSpell.getType().getColor();
            name.setStyle(name.getStyle().withColor(color == 0 ? 0xFFAAAAAA : color));

            tooltip.add(new TranslatableText("Spell Type: %s", name));
            actualSpell.getType().getTraits().appendTooltip(tooltip);
            tooltip.add(LiteralText.EMPTY);
            tooltip.add(new TranslatableText("Affinity: %s", actualSpell.getAffinity().name()).formatted(actualSpell.getAffinity().getColor()));
            tooltip.add(LiteralText.EMPTY);
            tooltip.addAll(FlowingText.wrap(new TranslatableText(actualSpell.getType().getTranslationKey() + ".lore").formatted(actualSpell.getAffinity().getColor()), 180).toList());
            tooltip.add(LiteralText.EMPTY);
            tooltip.add(new TranslatableText("[Click to Discard]"));
        }

        private Spell getActualSpell() {
            if (spell instanceof AbstractDelegatingSpell) {
                return ((AbstractDelegatingSpell)spell).getDelegates().stream().findFirst().orElse(spell);
            }
            return spell;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(relativeMouseX, relativeMouseY)) {
                remove(this);
                pony.getSpellSlot().removeIf(spell -> spell == this.spell, true);
                Channel.REMOVE_SPELL.send(new MsgRemoveSpell(spell));
                playClickEffect();
                return true;
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return squareDistance(mouseX, mouseY, getX(), getY()) < 75;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            copy.set(getX(), getY(), getZ(), getW());
            copy.transform(matrices.peek().getPositionMatrix());

            DrawableUtil.renderItemIcon(actualSpell.getType().getDefualtStack(),
                    copy.getX() - 8 + (copy.getX() - mouseX - 5) / 60D,
                    copy.getY() - 8 + (copy.getY() - mouseY - 5) / 60D,
                    1
            );

            matrices.push();
            matrices.translate(getX(), getY(), 0);

            int color = actualSpell.getType().getColor() << 2;

            DrawableUtil.drawArc(matrices, 7, 8, 0, DrawableUtil.TAU, color | 0x00000088, false);

            if (isMouseOver(relativeMouseX, relativeMouseY)) {
                DrawableUtil.drawArc(matrices, 0, 8, 0, DrawableUtil.TAU, color | 0x000000FF, false);
                renderTooltip(matrices, tooltip, 0, 0);

                if (!lastMouseOver) {
                    lastMouseOver = true;
                    playClickEffect();
                }
            } else {
                lastMouseOver = false;
            }

            matrices.pop();
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder var1) {
        }

        @Override
        public SelectionType getType() {
            return SelectionType.HOVERED;
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
