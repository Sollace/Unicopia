package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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
        slots.forEach(slot -> slot.renderForeground(matrices, abilities, tickDelta));

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
}
