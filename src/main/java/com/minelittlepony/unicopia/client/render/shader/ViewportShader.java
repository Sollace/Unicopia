package com.minelittlepony.unicopia.client.render.shader;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.*;
import com.google.gson.JsonSyntaxException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ViewportShader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ID = Unicopia.id("viewport_shader");

    public static final ViewportShader INSTANCE = new ViewportShader();

    public static final Identifier CREEPER_SHADER = Identifier.ofVanilla("shaders/post/creeper.json");
    public static final Identifier DESATURATION_SHADER = Unicopia.id("shaders/post/desaturate.json");

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Nullable
    private LoadedShader shader;

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public void loadShader(@Nullable Identifier shaderId) {

        if (shader != null) {
            try {
                shader.close();
            } catch (Throwable ignored) {
            } finally {
                shader = null;
            }
        }

        if (shaderId == null || Unicopia.getConfig().disableShaders.get()) {
            return;
        }

        try {
            shader = new LoadedShader(client, shaderId);
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", shaderId, e);
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse shader: {}", shaderId, e);
        }
    }

    public void onResized(int width, int height) {
        if (shader != null) {
            shader.setupDimensions(width, height);
        }
    }

    public void render(RenderTickCounter tickCounter) {
        if (Unicopia.getConfig().disableShaders.get()) {
            return;
        }

        if (shader != null && client.player != null) {
            Pony pony = Pony.of(client.player);

            float corruption = pony.getCorruption().getScaled(0.9F);
            if (!MathHelper.approximatelyEquals(corruption, 0)) {
                corruption = pony.getInterpolator().interpolate("corruption", corruption, 10);

                corruption = 1 - corruption + 0.05F;

                shader.setUniformValue("color_convolve", "Saturation", corruption);
                shader.render(tickCounter.getLastFrameDuration());
            }
        }
    }

    @Override
    public void reload(ResourceManager var1) {
        loadShader(shader != null ? shader.id : DESATURATION_SHADER);
    }

    static class LoadedShader extends PostEffectProcessor {
        private final Object2FloatMap<String> uniformValues = new Object2FloatOpenHashMap<>();

        private Multimap<String, JsonEffectShaderProgram> programs;

        private final Identifier id;

        public LoadedShader(MinecraftClient client, Identifier id) throws IOException, JsonSyntaxException {
            super(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), id);
            this.id = id;
            setupDimensions(
                client.getWindow().getFramebufferWidth(),
                client.getWindow().getFramebufferHeight()
            );
        }

        @Override
        public PostEffectPass addPass(String programName, Framebuffer source, Framebuffer dest, boolean linear) throws IOException {
            PostEffectPass pass = super.addPass(programName, source, dest, linear);
            if (programs == null) {
               programs = LinkedListMultimap.create();
            }
            programs.put(pass.getProgram().getName(), pass.getProgram());
            return pass;
        }

        public void setUniformValue(String programName, String uniformName, float value) {
            float currentValue = uniformValues.containsKey(uniformName) ? 0F : uniformValues.getFloat(uniformName);
            if (!MathHelper.approximatelyEquals(value, currentValue)) {
                uniformValues.put(uniformName, value);
                programs.get(programName).forEach(program -> {
                    GlUniform uniform = program.getUniformByName(uniformName);
                    if (uniform != null) {
                        uniform.set(value);
                    }
                });
            }
        }
    }
}
