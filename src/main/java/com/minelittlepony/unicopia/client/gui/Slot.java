package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

class Slot {
    protected final UHud uHud;

    private final AbilitySlot aSlot;
    private final AbilitySlot bSlot;

    protected int x;
    protected int y;

    private float lastCooldown;

    private final int slotPadding;
    private final int labelOffset;

    private final int size;
    private final int iconSize;

    private int textureU;
    private int textureV;

    private int backgroundU = 105;
    private int backgroundV = 105;

    public Slot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y, int padding, int size, int labelOffset, int iconSize) {
        this.uHud = uHud;
        this.aSlot = normalSlot;
        this.bSlot = backupSlot;
        this.x = x;
        this.y = y;
        this.slotPadding = padding;
        this.labelOffset = labelOffset;
        this.size = size;
        this.iconSize = iconSize;
    }

    Slot background(int u, int v) {
        textureU = u;
        textureV = v;
        return this;
    }
    Slot foreground(int u, int v) {
        backgroundU = u;
        backgroundV = v;
        return this;
    }

    void renderBackground(MatrixStack matrices, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        matrices.push();
        matrices.translate(x, y, 0);

        float cooldown = abilities.getStat(bSwap ? bSlot : aSlot).getFillProgress();

        // background
        UHud.drawTexture(matrices, 0, 0, textureU, textureV, size, size, 128, 128);

        uHud.renderAbilityIcon(matrices, abilities.getStat(bSwap ? bSlot : aSlot), slotPadding / 2, slotPadding / 2, iconSize, iconSize, iconSize, iconSize);


        if (cooldown > 0 && cooldown <= 1) {
            float lerpCooldown = MathHelper.lerp(tickDelta, cooldown, lastCooldown);

            lastCooldown = lerpCooldown;

            int progressBottom = size - slotPadding;
            int progressMax = size - slotPadding * 2;
            int progressTop = progressBottom - (int)(progressMax * cooldown);

            // progress
            UHud.fill(matrices, slotPadding, progressTop, size - slotPadding, progressBottom, 0xCFFFFFFF);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableBlend();
        }

        renderContents(matrices, abilities, bSwap, tickDelta);
        matrices.pop();
    }

    protected void renderContents(MatrixStack matrices, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        // contents

        UHud.drawTexture(matrices, 0, 0, backgroundU, backgroundV, size, size, 128, 128);
    }

    void renderLabel(MatrixStack matrices, AbilityDispatcher abilities, float tickDelta) {
        Text label = KeyBindingsHandler.INSTANCE.getBinding(aSlot).getBoundKeyLocalizedText();

        matrices.push();
        matrices.translate(x + labelOffset, y + labelOffset, 0);
        matrices.scale(0.5F, 0.5F, 0.5F);

        UHud.drawTextWithShadow(matrices, uHud.font, label, 0, 0, 0xFFFFFF);

        matrices.pop();
    }
}