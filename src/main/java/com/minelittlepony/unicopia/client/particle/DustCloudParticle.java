package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class DustCloudParticle extends AbstractBillboardParticle {
    //private static final Identifier TEXTURE = new Identifier("textures/particle/big_smoke_3.png");

    protected static final int SEGMENTS = 20;
    protected static final int SEPARATION = 270 / SEGMENTS;

    private float scaleFactor;

    protected Sprite sprite;
    private final RenderUtil.Vertex[] vertices;

    public DustCloudParticle(BlockStateParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        maxAge = 1000;
        gravityStrength = 1;
        red = 0.6F;
        green = 0.6F;
        blue = 0.6F;
        alpha = (float)world.getRandom().nextTriangular(0.6, 0.2);
        scaleFactor = (float)world.getRandom().nextTriangular(2, 1.2);
        sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(effect.getBlockState());
        vertices = new RenderUtil.Vertex[]{
                new RenderUtil.Vertex(-1, -1, 0, sprite.getMinU(), sprite.getMinV()),
                new RenderUtil.Vertex(-1,  1, 0, sprite.getMaxU(), sprite.getMinV()),
                new RenderUtil.Vertex( 1,  1, 0, sprite.getMaxU(), sprite.getMaxV()),
                new RenderUtil.Vertex( 1, -1, 0, sprite.getMinU(), sprite.getMaxV())
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
    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {
        float scale = getScale(tickDelta);
        float alpha = this.alpha * (1 - ((float)age / maxAge));
        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);
        matrices.scale(scale, scale * 0.5F, scale);

        float angle = ((this.age + tickDelta) % 360) / SEGMENTS;

        for (int i = 0; i < SEGMENTS; i++) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((i * angle) % 360));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((SEPARATION * i + angle) % 360));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((SEPARATION * i + angle) % 360));
            float ringScale = 1 + MathHelper.sin(((i * 10) + age + tickDelta) * 0.05F) * 0.1F;
            matrices.scale(ringScale, ringScale, ringScale);
            renderQuad(matrices, te, buffer, vertices, alpha, tickDelta);
            matrices.pop();
        }
    }
}
