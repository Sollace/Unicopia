package com.minelittlepony.unicopia.world.client.gui;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class UHud extends DrawableHelper {

    public static final UHud instance = new UHud();

    public static final Identifier HUD_TEXTURE = new Identifier("unicopia", "textures/gui/hud.png");

    private Slot secondarySlot = new Slot(AbilitySlot.SECONDARY, 26, 0);
    private Slot tertiarySlot = new Slot(AbilitySlot.TERTIARY, 36, 24);

    private final MinecraftClient client = MinecraftClient.getInstance();

    public void render(InGameHud hud, float tickDelta) {

        if (client.player == null || client.player.isSpectator()) {
            return;
        }

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        int x = 104 + (scaledWidth - 50) / 2;
        int y = 20 + scaledHeight - 70;

        MatrixStack matrices = new MatrixStack();

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();

        client.getTextureManager().bindTexture(HUD_TEXTURE);

        int frameHeight = 54;
        int frameWidth = 54;

        AbilityDispatcher abilities = Pony.of(client.player).getAbilities();

        drawTexture(matrices, x, y, 0, 0, frameWidth, frameHeight, 128, 128); // background

        AbilityDispatcher.Stat stat = abilities.getStat(AbilitySlot.PRIMARY);

        float progressPercent = stat.getFillProgress();

        if (progressPercent > 0 && progressPercent < 1) {
            int progressHeight = (int)(frameHeight * progressPercent);

            drawTexture(matrices, x, y + (frameHeight - progressHeight),
                61, frameHeight - progressHeight,
                frameWidth, progressHeight, 128, 128); // progress
        }

        renderAbilityIcon(matrices, stat, x + 9, y + 15, 32, 32, 32, 32);
        drawTexture(matrices, x, y, 0, 54, frameWidth, frameHeight, 128, 128); // frame

        secondarySlot.render(matrices, abilities, x, y, tickDelta);
        tertiarySlot.render(matrices, abilities, x, y, tickDelta);

        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }

    void renderAbilityIcon(MatrixStack matrices, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility().ifPresent(ability -> {
            Identifier id = Abilities.REGISTRY.getId(ability);
            client.getTextureManager().bindTexture(new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + ".png"));

            drawTexture(matrices, x, y, 0, 0, frameWidth, frameHeight, u, v);

            client.getTextureManager().bindTexture(HUD_TEXTURE);
        });
    }

    class Slot {
        private final AbilitySlot slot;

        private int x;
        private int y;

        private float lastCooldown;

        public Slot(AbilitySlot slot, int x, int y) {
            this.slot = slot;
            this.x = x;
            this.y = y;
        }

        void render(MatrixStack matrices, AbilityDispatcher abilities, int x, int y, float tickDelta) {
            x += this.x;
            y += this.y;

            AbilityDispatcher.Stat stat = abilities.getStat(slot);
            float cooldown = stat.getFillProgress();

            drawTexture(matrices, x, y, 80, 105, 25, 25, 128, 128);

            if (cooldown > 0 && cooldown < 1) {
                float lerpCooldown = MathHelper.lerp(tickDelta, cooldown, lastCooldown);

                lastCooldown = lerpCooldown;

                int slotPadding = 4;
                int slotSize = 15;

                int progressBottom = y + slotPadding + slotSize;
                int progressTop = progressBottom - (int)(15F * cooldown);

                fill(matrices, x + slotPadding, progressTop, x + slotPadding + slotSize, progressBottom, 0xFFFFFFFF);
                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();
            }

            renderAbilityIcon(matrices, stat, x + 2, y + 2, 20, 20, 20, 20);
            drawTexture(matrices, x, y, 105, 105, 25, 25, 128, 128);
        }

    }
}
