package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LightningBoltParticle extends AbstractGeometryBasedParticle {

    private final List<List<Vector3f>> branches = new ArrayList<>();

    public LightningBoltParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void tick() {
        if (age++ >= maxAge) {
            markDead();
            return;
        }

        if (age % 5 == 0) {
            branches.clear();
        }
        if (branches.isEmpty()) {
            int totalBranches = 2 + world.random.nextInt(6);

            while (branches.size() < totalBranches) {
                branches.add(generateBranch());
            }

            world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000, 8, true);
        }

        world.setLightningTicksLeft(2);
    }

    private List<Vector3f> generateBranch() {
        Vector3f startPos = new Vector3f(0, 0, 0);

        int intendedLength = 2 + world.random.nextInt(6);

        List<Vector3f> nodes = new ArrayList<>();

        while (nodes.size() < intendedLength) {
            startPos = startPos.add(
                    (float)world.random.nextTriangular(0.1F, 3),
                    (float)world.random.nextTriangular(0.1F, 3),
                    (float)world.random.nextTriangular(0.1F, 3)
            );

            nodes.add(startPos);
        }

        return nodes;
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayer.getLightning());

        RenderSystem.disableCull();

        Vec3d cam = camera.getPos();

        float x = (float)(MathHelper.lerp(tickDelta, prevPosX, this.x) - cam.getX());
        float y = (float)(MathHelper.lerp(tickDelta, prevPosY, this.y) - cam.getY());
        float z = (float)(MathHelper.lerp(tickDelta, prevPosZ, this.z) - cam.getZ());

        Vector3f origin = new Vector3f(x, y, z);

        for (List<Vector3f> branch : branches) {
            for (int i = 0; i < branch.size(); i++) {
                renderBranch(buffer, i == 0 ? origin : new Vector3f(branch.get(i - 1).add(x, y, z)), new Vector3f(branch.get(i).add(x, y, z)));
            }
        }

        immediate.draw();

        RenderSystem.enableCull();
    }

    private void renderBranch(VertexConsumer buffer, Vector3f from, Vector3f to) {
        float thickness = world.random.nextFloat() / 30 + 0.01F;

        renderQuad(buffer, new Vector3f[]{
            new Vector3f(from.x - thickness, from.y, from.z),
            new Vector3f(to.x - thickness, to.y, to.z),
            new Vector3f(to.x + thickness, to.y, to.z),
            new Vector3f(from.x + thickness, from.y, from.z),

            new Vector3f(from.x - thickness, from.y - thickness * 2, from.z),
            new Vector3f(to.x - thickness, to.y - thickness * 2, to.z),
            new Vector3f(to.x + thickness, to.y - thickness * 2, to.z),
            new Vector3f(from.x + thickness, from.y - thickness * 2, from.z),

            new Vector3f(from.x, from.y - thickness, from.z + thickness),
            new Vector3f(to.x, to.y - thickness, to.z + thickness),
            new Vector3f(to.x, to.y + thickness, to.z + thickness),
            new Vector3f(from.x, from.y + thickness, from.z + thickness),

            new Vector3f(from.x - thickness * 2, from.y - thickness, from.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y - thickness, to.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y + thickness, to.z + thickness),
            new Vector3f(from.x - thickness * 2, from.y + thickness, from.z + thickness)
        }, 0.3F, 1);
    }
}
