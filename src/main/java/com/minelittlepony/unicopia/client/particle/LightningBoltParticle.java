package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.util.math.Vec3f;

public class LightningBoltParticle extends AbstractGeometryBasedParticle {

    private final List<List<Vec3d>> branches = new ArrayList<>();

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

    private List<Vec3d> generateBranch() {
        Vec3d startPos = new Vec3d(0, 0, 0);

        int intendedLength = 2 + world.random.nextInt(6);

        List<Vec3d> nodes = new ArrayList<>();

        while (nodes.size() < intendedLength) {
            startPos = startPos.add(
                    world.random.nextTriangular(0.1, 3),
                    world.random.nextTriangular(0.1, 3),
                    world.random.nextTriangular(0.1, 3)
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

        Vec3f origin = new Vec3f(x, y, z);

        for (List<Vec3d> branch : branches) {
            for (int i = 0; i < branch.size(); i++) {
                renderBranch(buffer, i == 0 ? origin : new Vec3f(branch.get(i - 1).add(x, y, z)), new Vec3f(branch.get(i).add(x, y, z)));
            }
        }

        immediate.draw();

        RenderSystem.enableCull();
    }

    private void renderBranch(VertexConsumer buffer, Vec3f from, Vec3f to) {
        float thickness = world.random.nextFloat() / 30 + 0.01F;

        renderQuad(buffer, new Vec3f[]{
            new Vec3f(from.getX() - thickness, from.getY(), from.getZ()),
            new Vec3f(to.getX() - thickness, to.getY(), to.getZ()),
            new Vec3f(to.getX() + thickness, to.getY(), to.getZ()),
            new Vec3f(from.getX() + thickness, from.getY(), from.getZ()),

            new Vec3f(from.getX() - thickness, from.getY() - thickness * 2, from.getZ()),
            new Vec3f(to.getX() - thickness, to.getY() - thickness * 2, to.getZ()),
            new Vec3f(to.getX() + thickness, to.getY() - thickness * 2, to.getZ()),
            new Vec3f(from.getX() + thickness, from.getY() - thickness * 2, from.getZ()),

            new Vec3f(from.getX(), from.getY() - thickness, from.getZ() + thickness),
            new Vec3f(to.getX(), to.getY() - thickness, to.getZ() + thickness),
            new Vec3f(to.getX(), to.getY() + thickness, to.getZ() + thickness),
            new Vec3f(from.getX(), from.getY() + thickness, from.getZ() + thickness),

            new Vec3f(from.getX() - thickness * 2, from.getY() - thickness, from.getZ() + thickness),
            new Vec3f(to.getX() - thickness * 2, to.getY() - thickness, to.getZ() + thickness),
            new Vec3f(to.getX() - thickness * 2, to.getY() + thickness, to.getZ() + thickness),
            new Vec3f(from.getX() - thickness * 2, from.getY() + thickness, from.getZ() + thickness)
        }, 0.3F, 1);
    }
}
