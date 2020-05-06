package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class UHud extends DrawableHelper {

    public static final UHud instance = new UHud();

    public static final Identifier HUD_TEXTURE = new Identifier("unicopia", "textures/gui/hud.png");

    private Slot secondarySlot = new Slot(AbilitySlot.SECONDARY, 26, 0);
    private Slot tertiarySlot = new Slot(AbilitySlot.TERTIARY, 36, 24);

    public void render(InGameHud hud, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        int x = 104 + (scaledWidth - 50) / 2;
        int y = 20 + scaledHeight - 70;

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();

        client.getTextureManager().bindTexture(HUD_TEXTURE);

        int frameHeight = 54;
        int frameWidth = 54;

        AbilityDispatcher abilities = Pony.of(client.player).getAbilities();

        blit(x, y, 0, 0, frameWidth, frameHeight, 128, 128); // background

        float progressPercent = abilities.getStat(AbilitySlot.PRIMARY).getFillProgress();

        if (progressPercent > 0 && progressPercent < 1) {
            int progressHeight = (int)(frameHeight * progressPercent);

            blit(x, y + (frameHeight - progressHeight),
                61, frameHeight - progressHeight,
                frameWidth, progressHeight, 128, 128); // progress
        }

        blit(x, y, 0, 54, frameWidth, frameHeight, 128, 128); // frame

        secondarySlot.render(abilities, x, y, tickDelta);
        tertiarySlot.render(abilities, x, y, tickDelta);

        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }

    static class Slot {

        private final AbilitySlot slot;

        private int x;
        private int y;

        private float lastCooldown;

        public Slot(AbilitySlot slot, int x, int y) {
            this.slot = slot;
            this.x = x;
            this.y = y;
        }

        void render(AbilityDispatcher abilities, int x, int y, float tickDelta) {
            x += this.x;
            y += this.y;

            float cooldown = abilities.getStat(slot).getFillProgress();

            if (cooldown > 0 && cooldown < 1) {
                float lerpCooldown = MathHelper.lerp(tickDelta, cooldown, lastCooldown);

                lastCooldown = lerpCooldown;

                int slotPadding = 4;
                int slotSize = 15;

                int progressBottom = y + slotPadding + slotSize;
                int progressTop = progressBottom - (int)(15F * cooldown);

                fill(x + slotPadding, progressTop, x + slotPadding + slotSize, progressBottom, 0xAAFFFFFF);
            }

            blit(x, y, 105, 105, 30, 30, 128, 128);
        }

    }
}
