package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector4f;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.client.render.model.FanModel;
import com.minelittlepony.unicopia.client.render.model.VertexLightSource;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.ColorHelper.Argb;

public class DustCloudParticle extends AbstractBillboardParticle {
    protected static final int SEGMENTS = 20;
    protected static final int SEPARATION = 270 / SEGMENTS;

    private float scaleFactor;

    protected Sprite sprite;
    private final FanModel model;

    private final VertexLightSource lightSource;

    public DustCloudParticle(BlockStateParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        maxAge = 1000;
        gravityStrength = 1;
        red = 0.6F;
        green = 0.6F;
        blue = 0.6F;
        alpha = (float)world.getRandom().nextTriangular(0.6, 0.2) * 0.3F;
        scaleFactor = (float)world.getRandom().nextTriangular(2, 1.2);
        sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(effect.getBlockState());
        lightSource = new VertexLightSource(world);
        model = new FanModel(sprite) {
            @Override
            protected int getLightAt(Vector4f pos, int light) {
                return lightSource.getLight(pos, light);
            }
        };
        if (!effect.getBlockState().isOf(Blocks.GRASS_BLOCK)) {
            int i = MinecraftClient.getInstance().getBlockColors().getColor(effect.getBlockState(), world, BlockPos.ofFloored(x, y, z), 0);
            red *= Color.r(i);
            green *= Color.g(i);
            blue *= Color.b(i);
        }
    }

    @Override
    protected Identifier getTexture() {
        return sprite.getAtlasId();
    }

    @Override
    public void tick() {
        super.tick();
        scaleFactor += 0.001F;
        scale(MathHelper.clamp(age / 5F, 0, 1) * scaleFactor);
        lightSource.tick();
    }

    @Override
    protected void renderQuads(Tessellator te, float x, float y, float z, float tickDelta) {
        float scale = getScale(tickDelta) * 0.5F;
        float alpha = this.alpha * (1 - ((float)age / maxAge));
        int color = Argb.withAlpha((int)(alpha * 255), Colors.WHITE);
        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);
        matrices.scale(1, 0.5F, 1);

        float angle = (MathHelper.sin((this.age + tickDelta) / 100F) * 360) / SEGMENTS;

        for (int i = 0; i < SEGMENTS; i++) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((i * angle)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((SEPARATION * i - angle)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((SEPARATION * i + angle)));
            float ringScale = 1 + MathHelper.sin(((i * 10) + age + tickDelta) * 0.05F) * 0.1F;

            BufferBuilder buffer = te.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            model.render(matrices, buffer, 0, scale * ringScale, color);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            matrices.pop();
        }
    }
}
