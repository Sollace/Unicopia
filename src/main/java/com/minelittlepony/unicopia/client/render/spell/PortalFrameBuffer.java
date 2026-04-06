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
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.mixin.client.MixinMinecraftClient;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Nullable
    private PortalTexture texture;
    @Nullable
    private WorldRenderer renderer;

    private final Camera camera = new Camera();
    private final VirtualCameraEntity cameraEntity = new VirtualCameraEntity();

    private boolean closed;

    private final Pool pool = new Pool(3);

    private boolean pendingDraw;

    @Nullable
    private Frustum frustum;

    private final Identifier textureId;

    PortalFrameBuffer(UUID id) {
        this.textureId = Unicopia.id("portal_surface/" + id.toString());
    }

    public void draw(MatrixStack matrices, VertexConsumerProvider vertices) {
        matrices.translate(0, -0.001, 0);

        RenderSystem.assertOnRenderThread();

        if (!(closed || texture == null)) {
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getPortal(textureId)), 1, 2F, Colors.WHITE);
        } else {
            int skyColor = client.world.getSkyColor(client.gameRenderer.getCamera().getPos(), client.getRenderTickCounter().getTickProgress(false));
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicShield()), 0, 0, 2, skyColor);
        }
    }

    public void build(PortalSpellRenderer.State spell, CasterState caster) {
        closed = false;

        long refreshRate = Unicopia.getConfig().fancyPortalRefreshRate.get();
        if (refreshRate > 0 && texture != null && System.currentTimeMillis() % refreshRate != 0) {
            return;
        }

        if (pendingDraw && recursionCount > Math.max(0, Unicopia.getConfig().maxPortalRecursion.get())) {
            innerBuild(spell, caster);
            return;
        }

        if (pendingDraw) {
            return;
        }
        pendingDraw = true;
        if (recursionCount > 0) {
            innerBuild(spell, caster);
        } else {
            ((MixinMinecraftClient)client).getRenderTaskQueue().add(() -> innerBuild(spell, caster));
        }
    }

    private void innerBuild(PortalSpellRenderer.State spell, CasterState caster) {
        synchronized (client) {
            pendingDraw = false;

            if (recursionCount > 0) {
                return;
            }
            recursionCount++;

            @Nullable
            final Entity globalCameraEntity = client.getCameraEntity();

            try {
                if (closed || client.interactionManager == null) {
                    close();
                    return;
                }

                cameraEntity.updatePositionAndAngles(camera.getFocusedEntity(), spell);
                client.cameraEntity = cameraEntity;
                drawWorld();
            } finally {
                client.cameraEntity = globalCameraEntity;
                recursionCount--;
            }
        }
    }

    private void drawWorld() {
        final Window window = client.getWindow();
        final ClientWorld world = (ClientWorld)cameraEntity.getWorld();
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);

        final int width = window.getFramebufferWidth();
        final int height = window.getFramebufferHeight();

        final Matrix4f proj = RenderSystem.getProjectionMatrix();
        try {
            if (texture == null) {
                texture = new PortalTexture(textureId, width, height);
            }
            texture.resize(width, height);

            RenderUtil.copyBufferToBuffer(client.getFramebuffer(), texture.swapbuffer);

            if (renderer == null) {
                renderer = new WorldRenderer(client, client.getEntityRenderDispatcher(), client.getBlockEntityRenderDispatcher(), client.getBufferBuilders());
                client.gameRenderer.getCamera().update(world, cameraEntity, !client.options.getPerspective().isFirstPerson(), client.options.getPerspective().isFrontView(), tickDelta);
                renderer.setWorld(world);
                client.gameRenderer.getCamera().update(world, cameraEntity.focusedEntity, !client.options.getPerspective().isFirstPerson(), client.options.getPerspective().isFrontView(), tickDelta);
            }

            camera.update(world, cameraEntity, !client.options.getPerspective().isFirstPerson(), client.options.getPerspective().isFrontView(), tickDelta);

            float fov = 120;
            Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fov);
            RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.PERSPECTIVE);
            Quaternionf cameraInverseRotation = camera.getRotation().conjugate(new Quaternionf());
            Matrix4f positionMatrix = new Matrix4f().rotation(cameraInverseRotation);
            renderer.setupFrustum(camera.getPos(), positionMatrix, projectionMatrix);
            renderer.render(pool,
                    client.getRenderTickCounter(), false, camera, client.gameRenderer,
                    positionMatrix,
                    projectionMatrix
            );
            RenderUtil.copyBufferToTexture(client.getFramebuffer(), texture.getGlTexture());
            RenderUtil.copyBufferToBuffer(texture.swapbuffer, client.getFramebuffer());
        } finally {

            client.getBlockEntityRenderDispatcher().setWorld(client.world);
            RenderSystem.setProjectionMatrix(proj, ProjectionType.PERSPECTIVE);
        }
    }

    private class VirtualCameraEntity extends Entity {
        private static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(0, 0);
        @Nullable
        private Entity focusedEntity;

        public VirtualCameraEntity() {
            super(UEntities.CAST_SPELL, client.world);
        }

        public void updatePositionAndAngles(Entity focusedEntity, PortalSpellRenderer.State spell) {
            this.focusedEntity = focusedEntity;
            Camera camera = client.gameRenderer.getCamera();

            Vector4f transformedPos = spell.positionMatrix.transform(new Vector4f(camera.getPos().toVector3f(), 1));
            setPosition(transformedPos.x, transformedPos.y, transformedPos.z);
            setPitch(MathHelper.clamp(camera.getPitch() + spell.pitchChange, -90, 90));
            setYaw(MathHelper.wrapDegrees(camera.getYaw() + spell.yawChange));
        }

        @Override
        public EntityDimensions getDimensions(EntityPose pose) {
            return DIMENSIONS;
        }

        @Override
        protected void initDataTracker(Builder builder) {
        }

        @Override
        public boolean damage(ServerWorld world, DamageSource source, float amount) {
            return false;
        }

        @Override
        protected void readCustomDataFromNbt(NbtCompound nbt) { }

        @Override
        protected void writeCustomDataToNbt(NbtCompound nbt) { }
    }

    static class PortalTexture extends AbstractTexture implements AutoCloseable {
        public final SimpleFramebuffer swapbuffer;

        private int width;
        private int height;

        public PortalTexture(Identifier id, int width, int height) {
            this.width = width;
            this.height = height;
            glTexture = RenderSystem.getDevice().createTexture(() -> "Unicopia / Portal / Color", TextureFormat.RGBA8, width, height, 1);
            glTexture.setTextureFilter(FilterMode.NEAREST, false);
            swapbuffer = new SimpleFramebuffer("Unicopia/Portal Swap", width, height, true);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, this);
        }

        public void resize(int width, int height) {
            if (width != this.width || height != this.height) {
                this.width = width;
                this.height = height;
                glTexture.close();
                glTexture = RenderSystem.getDevice().createTexture(() -> "Unicopia / Portal / Color", TextureFormat.RGBA8, width, height, 1);
                glTexture.setTextureFilter(FilterMode.NEAREST, false);
                swapbuffer.resize(width, height);
            }
        }

        @Override
        public void close() {
            super.close();
            swapbuffer.delete();
        }
    }

    @Override
    public void close() {
        synchronized (client) {
            closed = true;
            if (texture != null) {
                PortalTexture fb = texture;
                texture = null;
                fb.close();
            }
            if (renderer != null) {
                renderer.getChunkBuilder().stop();
                renderer.close();
                renderer = null;
            }
            pool.clear();
        }
    }
}