package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.model.TexturedSphereModel;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.mixin.client.MixinMinecraftClient;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.Vec3d;

class PortalFrameBuffer implements AutoCloseable {
    private static final LoadingCache<UUID, PortalFrameBuffer> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .<UUID, PortalFrameBuffer>removalListener(n -> n.getValue().close())
            .build(CacheLoader.from(PortalFrameBuffer::new));

    private static int recursionCount;

    @Nullable
    public static PortalFrameBuffer unpool(UUID id) {
        try {
            return CACHE.get(id);
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Nullable
    private SimpleFramebuffer framebuffer;
    @Nullable
    private SimpleFramebuffer backgroundBuffer;
    @Nullable
    private WorldRenderer renderer;
    @Nullable
    private ClientWorld world;

    private boolean closed;

    private final MinecraftClient client = MinecraftClient.getInstance();

    private boolean pendingDraw;

    @Nullable
    private Frustum frustum;

    PortalFrameBuffer(UUID id) { }

    public void draw(MatrixStack matrices, VertexConsumerProvider vertices) {
        matrices.translate(0, -0.001, 0);

        if (!(closed || framebuffer == null)) {
            RenderSystem.assertOnRenderThread();
            GlStateManager._colorMask(true, true, true, false);
            GlStateManager._enableDepthTest();
            GlStateManager._disableCull();
            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            BufferBuilder buffer = tessellator.getBuffer();
            float uScale = (float)framebuffer.viewportWidth / (float)framebuffer.textureWidth;
            float vScale = (float)framebuffer.viewportHeight / (float)framebuffer.textureHeight;
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem._setShaderTexture(0, framebuffer.getColorAttachment());
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            TexturedSphereModel.DISK.render(matrices, buffer, 2F, 1, 1, 1, 1, uScale, vScale);
            tessellator.draw();

            client.getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            GlStateManager._enableCull();
            GlStateManager._colorMask(true, true, true, true);
            GlStateManager._depthMask(true);
        } else {
            Vec3d skyColor = client.world.getSkyColor(client.gameRenderer.getCamera().getPos(), client.getTickDelta());
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicShield()), 0, 0, 2, (float)skyColor.x, (float)skyColor.y, (float)skyColor.z, 1);
        }
    }

    public void build(PortalSpell spell, Caster<?> caster, EntityReference.EntityValues<Entity> target) {

        if (framebuffer != null && System.currentTimeMillis() % 100 != 0) {
            return;
        }

        if (pendingDraw && recursionCount > 0) {
            innerBuild(spell, caster, target);
            return;
        }

        if (pendingDraw) {
            return;
        }
        pendingDraw = true;
        if (recursionCount > 0) {
            innerBuild(spell, caster, target);
        } else {
            ((MixinMinecraftClient)client).getRenderTaskQueue().add(() -> innerBuild(spell, caster, target));
        }
    }

    private void innerBuild(PortalSpell spell, Caster<?> caster, EntityReference.EntityValues<Entity> target) {
        synchronized (client) {
            pendingDraw = false;

            if (recursionCount > 2) {
                return;
            }
            recursionCount++;

            try {
                if (closed || client.interactionManager == null) {
                    close();
                    return;
                }

                var fov = client.options.getFov();
                int originalFov = fov.getValue();
                fov.setValue(110);

                Entity cameraEntity = UEntities.CAST_SPELL.create(caster.asWorld());
                cameraEntity.setPosition(target.pos());
                cameraEntity.setPitch(spell.getTargetPitch());
                cameraEntity.setYaw(spell.getTargetYaw() + 180);

                drawWorld(cameraEntity, 400, 400);

                fov.setValue(originalFov);
            } finally {
                recursionCount--;
            }
        }
    }

    private void drawWorld(Entity cameraEntity, int width, int height) {
        Entity oldCameraEntity = client.cameraEntity;
        Window window = client.getWindow();

        int i = window.getFramebufferWidth();
        int j = window.getFramebufferHeight();

        width = i;
        height = j;

        Perspective perspective = client.options.getPerspective();
        MatrixStack view = RenderSystem.getModelViewStack();

        Matrix4f proj = RenderSystem.getProjectionMatrix();
        Matrix3f invView = RenderSystem.getInverseViewRotationMatrix();

        int fbo = client.getFramebuffer().fbo;
        Camera camera = client.gameRenderer.getCamera();

        WorldRenderer globalRenderer = client.worldRenderer;
        try {
            client.cameraEntity = cameraEntity;
            client.getFramebuffer().endWrite();

            if (framebuffer == null) {
                framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                framebuffer.setClearColor(0, 0, 0, 0);
                framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }

            view.push();
            view.loadIdentity();
            RenderSystem.applyModelViewMatrix();

            window.setFramebufferWidth(width);
            window.setFramebufferHeight(height);
            client.getFramebuffer().fbo = framebuffer.fbo;

            client.options.setPerspective(Perspective.FIRST_PERSON);

            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            framebuffer.beginWrite(true);
            BackgroundRenderer.clearFog();
            RenderSystem.enableCull();

            if (renderer == null) {
                renderer = new WorldRenderer(client, client.getEntityRenderDispatcher(), client.getBlockEntityRenderDispatcher(), client.getBufferBuilders());
            }
            if (client.world != world) {
                world = client.world;
                renderer.setWorld(client.world);
            }
            ((MixinMinecraftClient)client).setWorldRenderer(renderer);

            renderer.scheduleBlockRenders((int)cameraEntity.getX() / 16, (int)cameraEntity.getY() / 16, (int)cameraEntity.getZ() / 16);

            client.gameRenderer.setRenderHand(false);
            MatrixStack matrices = new MatrixStack();

            matrices.scale((float)width / height, 1, 1);

            client.gameRenderer.renderWorld(1, 0, matrices);

            // Strip transparency
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(1, 1, 1, 1);
            RenderSystem.clear(GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            RenderSystem.colorMask(true, true, true, true);

            framebuffer.endWrite();
        } finally {
            ((MixinMinecraftClient)client).setWorldRenderer(globalRenderer);

            client.getFramebuffer().fbo = fbo;
            client.getFramebuffer().beginWrite(true);

            view.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(proj, VertexSorter.BY_Z);
            RenderSystem.setInverseViewRotationMatrix(invView);

            window.setFramebufferWidth(i);
            window.setFramebufferHeight(j);

            client.options.setPerspective(perspective);
            client.cameraEntity = oldCameraEntity;

            if (recursionCount <= 1) {
                client.gameRenderer.setRenderHand(true);
                camera.update(client.world,
                        client.getCameraEntity() == null ? client.player : client.getCameraEntity(),
                        perspective.isFirstPerson(),
                        perspective.isFrontView(),
                        1
                );
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        if (framebuffer != null) {
            SimpleFramebuffer fb = framebuffer;
            framebuffer = null;
            fb.delete();
        }
        if (backgroundBuffer != null) {
            SimpleFramebuffer fb = backgroundBuffer;
            backgroundBuffer = null;
            fb.delete();
        }
        if (renderer != null) {
            renderer.close();
        }
    }
}