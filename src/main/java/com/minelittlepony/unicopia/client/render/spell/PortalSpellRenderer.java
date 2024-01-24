package com.minelittlepony.unicopia.client.render.spell;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minelittlepony.common.util.Color;
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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class PortalSpellRenderer extends SpellRenderer<PortalSpell> {
    static boolean FANCY_PORTAlS = false;

    private static final LoadingCache<UUID, PortalFrameBuffer> FRAME_BUFFERS = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .<UUID, PortalFrameBuffer>removalListener(n -> n.getValue().close())
            .build(CacheLoader.from(uuid -> new PortalFrameBuffer()));

    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass == 0;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, PortalSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        if (!spell.isLinked()) {
            return;
        }

        int color = spell.getType().getColor();

        float red = Color.r(color);
        float green = Color.g(color);
        float blue = Color.b(color);

        VertexConsumer buff = vertices.getBuffer(RenderLayers.getEndGateway());

        matrices.push();
        matrices.translate(0, 0.02, 0);
        SphereModel.DISK.render(matrices, buff, light, 0, 2F, red, green, blue, 1);
        matrices.pop();

        if (!FANCY_PORTAlS) {
            matrices.push();
            matrices.translate(0, -0.02, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            SphereModel.DISK.render(matrices, buff, light, 0, 2F, red, green, blue, 1);
            matrices.pop();
            return;
        }

        // Fancy portal rendering is disabled for now
        // Need to fix:
        // 1. Transparent parts of the sky (because the game sets the clear to (0,0,0,0)
        // 2. Chunk flickering at long distances between portals

        if (caster.asEntity().distanceTo(client.cameraEntity) > 50) {
            return; // don't bother rendering if too far away
        }

        spell.getTarget().ifPresent(target -> {
            try {
                float grown = Math.min(caster.asEntity().age, 20) / 20F;
                matrices.push();
                matrices.translate(0, -0.01, 0);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-spell.getYaw()));
                matrices.scale(grown, 1, grown);
                PortalFrameBuffer buffer = FRAME_BUFFERS.get(target.uuid());
                buffer.build(spell, caster, target);
                buffer.draw(matrices, vertices);
                matrices.pop();
            } catch (ExecutionException e) { }
        });
    }

    static class PortalFrameBuffer implements AutoCloseable {
        @Nullable
        private SimpleFramebuffer framebuffer;
        @Nullable
        private SimpleFramebuffer backgroundBuffer;
        private boolean closed;

        private final MinecraftClient client = MinecraftClient.getInstance();

        private boolean pendingDraw;

        private static int recursionCount;

        public void draw(MatrixStack matrices, VertexConsumerProvider vertices) {
            Vec3d skyColor = client.world.getSkyColor(client.gameRenderer.getCamera().getPos(), client.getTickDelta());
            BackgroundRenderer.setFogBlack();
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicShield()), 0, 0, 2, (float)skyColor.x, (float)skyColor.y, (float)skyColor.z, 1);
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
            }
        }

        public void build(PortalSpell spell, Caster<?> caster, EntityReference.EntityValues<Entity> target) {

            if (System.currentTimeMillis() % 100 < 50) {
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

                    Vec3d offset = new Vec3d(0, 1.8, 0);
                    float yaw = spell.getYawDifference();

                    offset = offset.rotateY(yaw * MathHelper.RADIANS_PER_DEGREE);

                    Entity cameraEntity = UEntities.CAST_SPELL.create(caster.asWorld());
                    cameraEntity.setPosition(target.pos().add(offset));
                    cameraEntity.setPitch(spell.getTargetPitch());
                    cameraEntity.setYaw(spell.getTargetYaw() + 180);

                    drawWorld(cameraEntity, 400, 400);

                    fov.setValue(originalFov);
                } finally {
                    recursionCount--;
                }
            }
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private void drawWorld(Entity cameraEntity, int width, int height) {
            Entity oldCameraEntity = client.cameraEntity;
            client.cameraEntity = cameraEntity;

            Window window = client.getWindow();

            Perspective perspective = client.options.getPerspective();
            client.options.setPerspective(Perspective.FIRST_PERSON);

            int i = window.getFramebufferWidth();
            int j = window.getFramebufferHeight();

            width = i;
            height = j;

            client.getFramebuffer().endWrite();

            if (framebuffer == null) {
                framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                framebuffer.setClearColor(0, 0, 0, 0);
                framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }

            window.setFramebufferWidth(width);
            window.setFramebufferHeight(height);

            MatrixStack view = RenderSystem.getModelViewStack();
            view.push();
            view.loadIdentity();
            RenderSystem.applyModelViewMatrix();
            Matrix4f proj = RenderSystem.getProjectionMatrix();
            Matrix3f invView = RenderSystem.getInverseViewRotationMatrix();

            int fbo = client.getFramebuffer().fbo;
            client.getFramebuffer().fbo = framebuffer.fbo;

            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            framebuffer.beginWrite(true);
            BackgroundRenderer.clearFog();
            RenderSystem.enableCull();

            Camera camera = client.gameRenderer.getCamera();

            ObjectArrayList<?> chunkInfos = ((WorldRendererDuck)client.worldRenderer).unicopia_getChunkInfos();
            List<Object> backup = new ArrayList<>(chunkInfos);

            client.gameRenderer.setRenderHand(false);
            MatrixStack matrices = new MatrixStack();

            matrices.scale((float)width / height, 1, 1);

            client.gameRenderer.renderWorld(1, 0, matrices);
            if (recursionCount <= 1) {
                client.gameRenderer.setRenderHand(true);
            }
            framebuffer.endWrite();

            client.getFramebuffer().fbo = fbo;
            client.getFramebuffer().beginWrite(true);

            chunkInfos.clear();
            chunkInfos.addAll((List)backup);

            view.pop();
            RenderSystem.applyModelViewMatrix();

            window.setFramebufferWidth(i);
            window.setFramebufferHeight(j);

            client.options.setPerspective(perspective);
            client.cameraEntity = oldCameraEntity;

            RenderSystem.setProjectionMatrix(proj, VertexSorter.BY_Z);
            RenderSystem.setInverseViewRotationMatrix(invView);

            if (recursionCount <= 1) {
                camera.update(client.world,
                        client.getCameraEntity() == null ? client.player : client.getCameraEntity(),
                        perspective.isFirstPerson(),
                        perspective.isFrontView(),
                        1
                );
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
        }
    }

    public interface WorldRendererDuck {
        ObjectArrayList<?> unicopia_getChunkInfos();
    }
}
