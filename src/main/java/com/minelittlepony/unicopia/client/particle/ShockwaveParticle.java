package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.RenderUtil;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ShockwaveParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particles/shockwave.png");

    public ShockwaveParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY,
            double velocityZ) {
        super(world, x, y, z, 0, 0, 0);
        maxAge = 20;
        setVelocity(0, 0, 0);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {
        if (age < 5 || age % 6 == 0) {
            BlockState state = world.getBlockState(BlockPos.ofFloored(this.x, this.y - 0.5, this.z));
            if (!state.isAir()) {
                world.playSound(this.x, this.y, this.z, state.getSoundGroup().getBreakSound(), SoundCategory.AMBIENT, 2.5F, 0.4F, true);
            }
        }

        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);

        for (int ring = -1; ring < 2; ring ++) {
            float scaleH = (1.5F + (age + tickDelta)) + ring;
            float scaleV = (2 + MathHelper.sin(age / 5F) * 2F) - Math.abs(ring);
            matrices.push();
            matrices.scale(scaleH, scaleV, scaleH);
            matrices.translate(-0.5, 0, -0.5);
            int sides = 5;
            for (int i = 0; i < sides; i++) {
                RenderUtil.renderFace(matrices, te, buffer, red, green, blue, 0.3F, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360 / sides));
                matrices.translate(-1, 0, 0);
            }
            matrices.pop();
        }
    }
}
