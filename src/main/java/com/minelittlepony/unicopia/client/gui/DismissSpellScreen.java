package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.client.FlowingText;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRemoveSpell;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.*;

public class DismissSpellScreen extends GameGui {
    private final Pony pony = Pony.of(MinecraftClient.getInstance().player);

    private int relativeMouseX;
    private int relativeMouseY;

    public DismissSpellScreen() {
        super(Text.translatable("gui.unicopia.dismiss_spell"));
    }

    @Override
    protected void init() {
        double azimuth = 0;
        double ring = 2;

        List<PlaceableSpell> placeableSpells = new ArrayList<>();

        for (Spell spell : pony.getSpellSlot().stream(true).toList()) {

            if (spell instanceof PlaceableSpell placeable) {
                if (placeable.getPosition().isPresent()) {
                    placeableSpells.add(placeable);
                    continue;
                }
            }

            addDrawableChild(new Entry(spell).ofRadial(75 * ring - 25, azimuth));
            ring *= 1 + 0.03 / ring;
            azimuth += Math.PI / (8 * ring);
        }

        double minimalDistance = 75 * (ring - 1) - 25;
        Vec3d origin = pony.getOriginVector();

        placeableSpells.forEach(placeable -> {
            placeable.getPosition().ifPresent(position -> {
                Vec3d relativePos = position.subtract(origin);
                Vec3d cartesian = relativePos
                        .normalize()
                        .multiply(minimalDistance + relativePos.length())
                        .rotateY((pony.asEntity().getYaw() - 180) * MathHelper.RADIANS_PER_DEGREE);
                addDrawableChild(new Entry(placeable).ofCartesian(cartesian));
            });
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, width, height / 2, 0xF0101010, 0x80101010);
        context.fillGradient(0, height / 2, width, height, 0x80101010, 0xF0101010);

        relativeMouseX = -width + mouseX * 2;
        relativeMouseY = -height + mouseY * 2;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(width - mouseX, height - mouseY, 0);
        DrawableUtil.drawLine(matrices, 0, 0, relativeMouseX, relativeMouseY, 0xFFFFFF88);
        DrawableUtil.drawArc(matrices, 40, 80, 0, DrawableUtil.TAU, 0x00000010, false);
        DrawableUtil.drawArc(matrices, 160, 1600, 0, DrawableUtil.TAU, 0x00000020, false);

        super.render(context, mouseX, mouseY, delta);
        DrawableUtil.renderRaceIcon(context, pony.getObservedSpecies(), 0, 0, 16);
        matrices.pop();

        DrawableUtil.drawLine(matrices, mouseX, mouseY - 4, mouseX, mouseY + 4, 0xFFAAFF99);
        DrawableUtil.drawLine(matrices, mouseX - 4, mouseY, mouseX + 4, mouseY, 0xFFAAFF99);

        matrices.push();
        matrices.translate(0, 0, 300);
        Text cancel = Text.translatable("gui.unicopia.dispell_screen.cancel");
        context.drawText(getFont(), cancel, (width - getFont().getWidth(cancel)) / 2, height - 30, 0xFFFFFFFF, true);
        matrices.pop();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    class Entry extends Vector4f implements Element, Drawable, Selectable {

        private final Spell spell;
        private final Spell actualSpell;

        private boolean lastMouseOver;

        private final Vector4f copy = new Vector4f();

        public Entry(Spell spell) {
            this.spell = spell;
            this.actualSpell = getActualSpell();
        }

        public Entry ofRadial(double radius, double azimuth) {
            SphereModel.convertToCartesianCoord(this, radius, azimuth, azimuth);
            add(0,  -(float)radius / 2F, 0, 0);
            return this;
        }

        public Entry ofCartesian(Vec3d pos) {
            add((float)pos.x, (float)pos.z, (float)pos.y, 1);
            return this;
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
                Channel.REMOVE_SPELL.sendToServer(new MsgRemoveSpell(spell));
                playClickEffect();
                return true;
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return squareDistance(mouseX, mouseY, x, y) < 75;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
            MatrixStack matrices = context.getMatrices();
            copy.set(x, y, z, w);
            copy.mul(matrices.peek().getPositionMatrix());

            var type = actualSpell.getType().withTraits(actualSpell.getTraits());

            DrawableUtil.drawLine(matrices, 0, 0, (int)x, (int)y, 0xFFAAFF99);
            DrawableUtil.renderItemIcon(context, actualSpell.isDead() ? UItems.BOTCHED_GEM.getDefaultStack() : type.getDefaultStack(),
                    copy.x - 8 + copy.z / 20F,
                    copy.y - 8 + copy.z / 20F,
                    1
            );

            int color = actualSpell.getType().getColor() << 2;

            matrices.push();
            matrices.translate(x, y, 0);

            DrawableUtil.drawArc(matrices, 7, 8, 0, DrawableUtil.TAU, color | 0x00000088, false);

            if (isMouseOver(relativeMouseX, relativeMouseY)) {
                DrawableUtil.drawArc(matrices, 0, 8, 0, DrawableUtil.TAU, color | 0x000000FF, false);

                List<Text> tooltip = new ArrayList<>();

                MutableText name = actualSpell.getType().getName().copy();
                color = actualSpell.getType().getColor();
                name.setStyle(name.getStyle().withColor(color == 0 ? 0xFFAAAAAA : color));
                tooltip.add(Text.translatable("gui.unicopia.dispell_screen.spell_type", name));
                actualSpell.getType().getTraits().appendTooltip(tooltip);
                tooltip.add(ScreenTexts.EMPTY);
                tooltip.add(Text.translatable("gui.unicopia.dispell_screen.affinity", actualSpell.getAffinity().name()).formatted(actualSpell.getAffinity().getColor()));
                tooltip.add(ScreenTexts.EMPTY);
                tooltip.addAll(FlowingText.wrap(Text.translatable(actualSpell.getType().getTranslationKey() + ".lore").formatted(actualSpell.getAffinity().getColor()), 180).toList());
                if (spell instanceof TimedSpell timed) {
                    tooltip.add(ScreenTexts.EMPTY);
                    tooltip.add(Text.translatable("gui.unicopia.dispell_screen.time_left", StringHelper.formatTicks(timed.getTimer().getTicksRemaining())));
                }
                tooltip.add(ScreenTexts.EMPTY);
                tooltip.add(Text.translatable("gui.unicopia.dispell_screen.discard"));
                context.drawTooltip(getFont(), tooltip, 0, 0);

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
        public void appendNarrations(NarrationMessageBuilder builder) {
        }

        @Override
        public SelectionType getType() {
            return SelectionType.HOVERED;
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }

    static void playClickEffect() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 6, 0.3F));
    }

    static double squareDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx * dx + dy * dy);
    }
}
