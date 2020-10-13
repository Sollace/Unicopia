package com.minelittlepony.unicopia.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public class RainboomParticle extends Particle {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/particles/rainboom_ring.png");

    protected float prevBaseSize = 0;
    protected float baseSize = 0;
    protected float scale = 1;

    protected Quaternion rotation;

    public RainboomParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z, double angleX, double angleY, double angleZ) {
        super(world, x, y, z);

        rotation = Vector3f.POSITIVE_X.getRadialQuaternion((float)angleX);
        rotation.hamiltonProduct(Vector3f.POSITIVE_Y.getRadialQuaternion((float)angleY));
        rotation.hamiltonProduct(Vector3f.POSITIVE_Z.getRadialQuaternion((float)angleZ));
        setMaxAge(40);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        Vec3d cam = camera.getPos();

        float renderX = (float)(MathHelper.lerp(tickDelta, prevPosX, x) - cam.getX());
        float renderY = (float)(MathHelper.lerp(tickDelta, prevPosY, y) - cam.getY());
        float renderZ = (float)(MathHelper.lerp(tickDelta, prevPosZ, z) - cam.getZ());

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float scale = getSize(tickDelta);

        for(int k = 0; k < 4; ++k) {
           Vector3f corner = corners[k];
           corner.rotate(rotation);
           corner.scale(scale);
           corner.add(renderX, renderY, renderZ);
        }

        float alpha = colorAlpha * (1 - ((float)age / maxAge));

        int light = getColorMultiplier(tickDelta);

        Tessellator te = Tessellator.getInstance();
        BufferBuilder buffer = te.getBuffer();

        MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.alphaFunc(516, 0.003921569F);

        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        buffer.vertex(corners[0].getX(), corners[0].getY(), corners[0].getZ()).texture(0, 0).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[1].getX(), corners[1].getY(), corners[1].getZ()).texture(1, 0).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[2].getX(), corners[2].getY(), corners[2].getZ()).texture(0, 0).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[3].getX(), corners[3].getY(), corners[3].getZ()).texture(0, 1).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();

        te.draw();

        RenderSystem.enableCull();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.defaultBlendFunc();
    }

    public float getSize(float tickDelta) {
       return MathHelper.lerp(tickDelta, prevBaseSize, baseSize) * scale;
    }

    @Override
    public Particle scale(float scale) {
       this.scale = scale;
       return super.scale(scale);
    }

    @Override
    public void tick() {
        super.tick();

        prevBaseSize = baseSize;
        baseSize++;

        if (this.age == 1) {
            this.world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 5, 0.3F, true);
            this.world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 10, 1.3F, true);
        }
    }
}
