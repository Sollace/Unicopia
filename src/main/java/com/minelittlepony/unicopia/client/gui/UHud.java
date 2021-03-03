package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class UHud extends DrawableHelper {

    public static final UHud INSTANCE = new UHud();

    public static final Identifier HUD_TEXTURE = new Identifier("unicopia", "textures/gui/hud.png");

    public static final int PRIMARY_SLOT_SIZE = 49;

    public TextRenderer font;

    final MinecraftClient client = MinecraftClient.getInstance();

    private final List<Slot> slots = Util.make(new ArrayList<>(), slots -> {
        slots.add(new ManaRingSlot(this, AbilitySlot.PRIMARY, AbilitySlot.PASSIVE, 0, 0));
        slots.add(new Slot(this, AbilitySlot.SECONDARY, AbilitySlot.SECONDARY, 26, -5));
        slots.add(new Slot(this, AbilitySlot.TERTIARY, AbilitySlot.TERTIARY, 36, 19));
    });

    @Nullable
    private Text message;
    private int messageTime;

    int xDirection;

    public void render(InGameHud hud, MatrixStack matrices, float tickDelta) {

        if (client.player == null || client.player.isSpectator() || client.options.hudHidden) {
            return;
        }

        font = client.textRenderer;

        xDirection = client.player.getMainArm() == Arm.LEFT ? -1 : 1;

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        matrices.push();
        matrices.translate(((scaledWidth - 50) / 2) + (104 * xDirection), scaledHeight - 50, 0);

        Pony pony = Pony.of(client.player);
        AbilityDispatcher abilities = pony.getAbilities();

        if (message != null && messageTime > 0) {
            renderMessage(matrices, tickDelta);
        }

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();

        client.getTextureManager().bindTexture(HUD_TEXTURE);

        boolean swap = client.options.keySneak.isPressed();

        slots.forEach(slot -> slot.renderBackground(matrices, abilities, swap, tickDelta));
        slots.forEach(slot -> slot.renderLabel(matrices, abilities, tickDelta));

        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();

        matrices.pop();

        if (pony.getSpecies() == Race.CHANGELING && !client.player.isSneaking()) {
            pony.getSpellSlot().get(SpellType.DISGUISE, false).map(DisguiseSpell::getDisguise)
                .map(Disguise::getAppearance)
                .ifPresent(appearance -> {

                    float baseHeight = 14;
                    float entityHeight = appearance.getDimensions(appearance.getPose()).height;
                    int scale = (int)(baseHeight / entityHeight);

                    int x = scaledWidth / 2 + 67;
                    int y = scaledHeight - 25;

                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(x, y, 0);
                    RenderSystem.rotatef(45, -0.2F, 1, 0);
                    InventoryScreen.drawEntity(0, 0, scale, 0, -20, client.player);
                    RenderSystem.popMatrix();
                });
        }
    }

    private void renderMessage(MatrixStack matrices, float tickDelta) {

        float time = messageTime - tickDelta;
        int progress = Math.min(255, (int)(time * 255F / 20F));

        if (progress > 8) {

            int color = 0xFFFFFF;

            int alpha = progress << 24 & -16777216;

            color |= alpha;

            drawCenteredText(matrices, client.textRenderer, message, 25, -15, color);
        }
    }

    public void setMessage(Text message) {
        this.message = message;
        this.messageTime = 60;
    }

    public void tick() {
        if (messageTime > 0) {
            messageTime--;
        }
    }

    void renderAbilityIcon(MatrixStack matrices, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility(KeyBindingsHandler.INSTANCE.page).ifPresent(ability -> {
            client.getTextureManager().bindTexture(ability.getIcon(Pony.of(client.player), client.options.keySneak.isPressed()));
            drawTexture(matrices, x, y, 0, 0, frameWidth, frameHeight, u, v);
            client.getTextureManager().bindTexture(HUD_TEXTURE);
        });
    }

}
