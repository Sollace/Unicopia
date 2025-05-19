package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.shader.UShaders;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.mixin.client.MixinMinecraftClient;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
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
import net.minecraft.util.Colors;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

class PortalFrameBuffer implements AutoCloseable {
    private static final LoadingCache<UUID, PortalFrameBuffer> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
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
    private WorldRenderer renderer;
    @Nullable
    private ClientWorld world;

    private final Camera camera = new Camera();

    private boolean closed;

    private final MinecraftClient client = MinecraftClient.getInstance();

    private boolean pendingDraw;

    @Nullable
    private Frustum frustum;

    PortalFrameBuffer(UUID id) { }

    public void draw(MatrixStack matrices, VertexConsumerProvider vertices) {
        matrices.translate(0, -0.001, 0);

        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._enableDepthTest();
        GlStateManager._disableCull();

        if (!(closed || framebuffer == null)) {
            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            RenderSystem.setShader(UShaders.RENDER_TYPE_PORTAL_SURFACE);
            RenderSystem._setShaderTexture(0, framebuffer.getColorAttachment());
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            SphereModel.DISK.render(matrices, buffer, 1, 2F, Colors.WHITE);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            client.getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        } else {
            Vec3d skyColor = client.world.getSkyColor(client.gameRenderer.getCamera().getPos(), client.getRenderTickCounter().getTickDelta(false));
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicShield()), 0, 0, 2, Color.argbToHex(1, (float)skyColor.x, (float)skyColor.y, (float)skyColor.z));
        }

        GlStateManager._enableCull();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);
    }

    public void build(PortalSpell spell, Caster<?> caster, EntityReference.EntityValues<Entity> target) {
        closed = false;

        long refreshRate = Unicopia.getConfig().fancyPortalRefreshRate.get();
        if (refreshRate > 0 && framebuffer != null && System.currentTimeMillis() % refreshRate != 0) {
            return;
        }

        if (pendingDraw && recursionCount > Math.max(0, Unicopia.getConfig().maxPortalRecursion.get())) {
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

            if (recursionCount > 0) {
                return;
            }
            recursionCount++;

            Entity globalCameraEntity = client.cameraEntity;

            try {
                if (closed || client.interactionManager == null) {
                    close();
                    return;
                }

                Camera camera = client.gameRenderer.getCamera();

                Entity cameraEntity = UEntities.CAST_SPELL.create(caster.asWorld());

                Vec3d pos = target.pos();

                Quaternionf orientationChange = spell.getOrientationChange();
                Matrix4f positionMatrix = spell.getPositionMatrix(caster, target.pos(), orientationChange, new Matrix4f());

                Vector4f transformedPos = positionMatrix.transform(new Vector4f(pos.toVector3f(), 1));
                cameraEntity.setPosition(transformedPos.x, transformedPos.y + 0.5F, transformedPos.z);
                cameraEntity.setPitch(MathHelper.clamp(camera.getPitch() - spell.getTargetPitch() + spell.getPitch(), -90, 90));
                cameraEntity.setYaw(MathHelper.wrapDegrees(camera.getYaw() + spell.getYawDifference()));

                client.cameraEntity = cameraEntity;
                drawWorld(cameraEntity, 400, 400);
            } finally {
                client.cameraEntity = globalCameraEntity;
                recursionCount--;
            }
        }
    }

    private void drawWorld(Entity cameraEntity, int width, int height) {
        Window window = client.getWindow();

        int globalFramebufferWidth = window.getFramebufferWidth();
        int globalFramebufferHeight = window.getFramebufferHeight();

        width = globalFramebufferWidth;
        height = globalFramebufferHeight;

        Matrix4f proj = RenderSystem.getProjectionMatrix();
        try {
            client.getFramebuffer().endWrite();

            if (framebuffer == null) {
                framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                framebuffer.setClearColor(0, 0, 0, 0);
                framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }

            window.setFramebufferWidth(width);
            window.setFramebufferHeight(height);

            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            framebuffer.beginWrite(true);
            BackgroundRenderer.clearFog();
            RenderSystem.enableCull();

            if (cameraEntity.getWorld() != world) {
                world = (ClientWorld)cameraEntity.getWorld();
            }

            if (renderer == null) {
                renderer = new WorldRenderer(client, client.getEntityRenderDispatcher(), client.getBlockEntityRenderDispatcher(), client.getBufferBuilders());
                renderer.setWorld(world);
                renderer.scheduleBlockRenders(
                        ChunkSectionPos.getSectionCoord((int)cameraEntity.getX()),
                        ChunkSectionPos.getSectionCoord((int)cameraEntity.getY()),
                        ChunkSectionPos.getSectionCoord((int)cameraEntity.getZ())
                );
            }

            camera.update(world, cameraEntity, false, false, 1);

            double fov = 120;
            Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fov);
            Matrix4f cameraTransform = new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf()));

            client.gameRenderer.loadProjectionMatrix(projectionMatrix);

            renderer.setupFrustum(
                    camera.getPos(),
                    cameraTransform,
                    client.gameRenderer.getBasicProjectionMatrix(Math.max(fov, client.options.getFov().getValue().intValue()))
            );
            renderer.render(client.getRenderTickCounter(), false, camera, client.gameRenderer,
                    client.gameRenderer.getLightmapTextureManager(),
                    cameraTransform,
                    projectionMatrix
            );
            // Strip transparency
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(1, 1, 1, 1);
            RenderSystem.clear(GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            RenderSystem.colorMask(true, true, true, true);

            framebuffer.endWrite();
        } finally {
            client.getFramebuffer().beginWrite(true);
            client.gameRenderer.loadProjectionMatrix(proj);
            client.getBlockEntityRenderDispatcher().setWorld(client.world);

            window.setFramebufferWidth(globalFramebufferWidth);
            window.setFramebufferHeight(globalFramebufferHeight);
        }
    }

    @Override
    public void close() {
        synchronized (client) {
            closed = true;
            if (framebuffer != null) {
                SimpleFramebuffer fb = framebuffer;
                framebuffer = null;
                fb.delete();
            }
            if (renderer != null) {
                renderer.getChunkBuilder().stop();
                renderer.close();
                renderer = null;
            }
        }
    }
}