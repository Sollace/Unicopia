package com.minelittlepony.unicopia.client.render.shader;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.Pool;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ViewportShader {
    public static final ViewportShader INSTANCE = new ViewportShader();

    public static final Identifier CREEPER_SHADER = Identifier.ofVanilla("shaders/post/creeper.json");
    public static final Identifier DESATURATION_SHADER = Unicopia.id("shaders/post/desaturate.json");

    private final MinecraftClient client = MinecraftClient.getInstance();

    @SuppressWarnings("deprecation") // processor.render()
    public void render(Pool pool, float tickDelta) {
        if (Unicopia.getConfig().disableShaders.get()) {
            return;
        }

        Pony pony = Pony.of(client.player);

        if (pony != null) {
            float corruption = pony.getInterpolator().interpolate("corruption", pony.getCorruption().getScaled(0.9F), 10);
            if (!MathHelper.approximatelyEquals(corruption, 0)) {
                PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(DESATURATION_SHADER, DefaultFramebufferSet.MAIN_ONLY);
                if (processor == null) {
                    return;
                }

                processor.render(client.getFramebuffer(), pool, pass -> {
                    pass.setUniform("Saturation", 1 - corruption + 0.05F);
                });
            }
        }
    }
}
