package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LightningBoltParticle extends AbstractGeometryBasedParticle {

    private final List<Bolt> branches = new ArrayList<>();

    private final LightningBoltParticleEffect effect;

    public LightningBoltParticle(LightningBoltParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.effect = effect;
    }

    @Override
    public void tick() {
        if (age++ >= maxAge) {
            markDead();
            return;
        }

        if (effect.changeFrequency() > 0 && age % effect.changeFrequency() == 0) {
            branches.clear();
        }

        if (branches.isEmpty()) {
            effect.pathEndPoint().ifPresentOrElse(endpoint -> {
                branches.add(generateTrunk(endpoint.subtract(x, y, z).toVector3f()));
            }, () -> {
                int totalBranches = 2 + world.random.nextInt(effect.maxBranches());

                while (branches.size() < totalBranches) {
                    branches.add(generateBranch());
                }
            });

            if (!effect.silent()) {
                world.playSound(x, y, z, USounds.Vanilla.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000, 8, true);
            }
        }

        if (effect.pathEndPoint().isEmpty() && !effect.silent()) {
            world.setLightningTicksLeft(2);
        }
    }

    private Bolt generateTrunk(Vector3f end) {
        Vector3f start = new Vector3f(0, 0, 0);
        int kinks = 2 + world.random.nextInt(effect.maxBranches());

        Vector3f segmentLength = end.sub(start, new Vector3f()).mul(1F/kinks);
        float deviation = effect.maxDeviation();

        List<Vector3f> nodes = new ArrayList<>();
        nodes.add(start);

        for (int i = 0; i < kinks - 1; i++) {
            start = start.add(segmentLength, new Vector3f()).add(
                    (float)world.random.nextTriangular(0, deviation),
                    0,
                    (float)world.random.nextTriangular(0, deviation)
            );
            nodes.add(start);
        }
        nodes.add(end);

        return new Bolt(true, nodes);
    }

    private Bolt generateBranch() {
        Vector3f startPos = new Vector3f(0, 0, 0);

        int intendedLength = 2 + world.random.nextInt(effect.maxBranches());
        float deviation = effect.maxDeviation();
        List<Vector3f> nodes = new ArrayList<>();

        while (nodes.size() < intendedLength) {
            startPos = startPos.add(
                    (float)world.random.nextTriangular(0F, deviation),
                    (float)world.random.nextTriangular(0F, deviation),
                    (float)world.random.nextTriangular(0F, deviation),
                    new Vector3f()
            );

            nodes.add(startPos);
        }

        return new Bolt(false, nodes);
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
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();

        for (Bolt branch : branches) {
            for (int i = 0; i < branch.nodes().size(); i++) {
                renderBranch(buffer,
                        branch.isTrunk() ? 0.05125F : world.random.nextFloat() / 30 + 0.01F,
                        i == 0 ? origin : branch.nodes().get(i - 1).add(x, y, z, from), branch.nodes().get(i).add(x, y, z, to)
                );
            }
        }

        immediate.draw();

        RenderSystem.enableCull();
    }

    private void renderBranch(VertexConsumer buffer, float thickness, Vector3f from, Vector3f to) {
        renderQuad(buffer, new Vector3f[]{
            new Vector3f(from.x - thickness, from.y, from.z + thickness),
            new Vector3f(to.x - thickness, to.y, to.z + thickness),
            new Vector3f(to.x + thickness, to.y, to.z + thickness),
            new Vector3f(from.x + thickness, from.y, from.z + thickness),

            new Vector3f(from.x - thickness, from.y, from.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z - thickness),
            new Vector3f(from.x + thickness, from.y, from.z - thickness),

            new Vector3f(from.x - thickness, from.y, from.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z + thickness),
            new Vector3f(from.x - thickness, from.y, from.z + thickness),

            new Vector3f(from.x + thickness, from.y, from.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z + thickness),
            new Vector3f(from.x + thickness, from.y, from.z + thickness)
            /*,

            new Vector3f(from.x - thickness, from.y - thickness * 2, from.z),
            new Vector3f(to.x - thickness, to.y - thickness * 2, to.z),
            new Vector3f(to.x + thickness, to.y - thickness * 2, to.z),
            new Vector3f(from.x + thickness, from.y - thickness * 2, from.z),

            new Vector3f(from.x, from.y - thickness, from.z + thickness),
            new Vector3f(to.x, to.y - thickness, to.z + thickness),
            new Vector3f(to.x, to.y + thickness, to.z + thickness),
            new Vector3f(from.x, from.y + thickness, from.z + thickness)

            ,

            new Vector3f(from.x - thickness * 2, from.y - thickness, from.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y - thickness, to.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y + thickness, to.z + thickness),
            new Vector3f(from.x - thickness * 2, from.y + thickness, from.z + thickness)*/
        }, 0.3F, 1);
    }

    record Bolt(boolean isTrunk, List<Vector3f> nodes) {

    }
}
