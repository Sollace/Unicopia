package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.particle.ParticleHandle.Link;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class RainbowTrailParticle extends AbstractBillboardParticle implements Attachment {
    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/particles/rainboom_trail.png");

    private final List<Segment> segments = new ArrayList<>();

    private final Link link = new Link();

    public RainbowTrailParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        segments.add(new Segment(new Vec3d(x, y, z)));
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attach(Caster<?> caster) {
        link.attach(caster);
    }

    @Override
    public void detach() {
        link.detach();
    }

    @Override
    public void setAttribute(int key, Object value) {

    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {
        for (int i = 0; i < segments.size() - 1; i++) {
            Vector3f[] corners = segments.get(i).getPlane(segments.get(i + 1));
            float scale = getScale(tickDelta);

            for (int k = 0; k < 4; ++k) {
               Vector3f corner = corners[k];
               corner.scale(scale);
               corner.add(x, y, z);
            }

            renderQuad(te, buffer, corners, segments.get(i).getAlpha(), tickDelta);
        }
    }

    private void follow(Caster<?> caster) {
        Vec3d next = caster.getOriginVector();

        if (segments.isEmpty()) {
            segments.add(new Segment(next));
        } else {
            Vec3d last = segments.get(segments.size() - 1).position;
            if (next.distanceTo(last) > 0.2) {
                segments.add(new Segment(next));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        age = 0;

        if (link.linked()) {
            link.ifAbsent(() -> {}).ifPresent(this::follow);
        } else {
            follow(Pony.of(MinecraftClient.getInstance().player));
        }

        if (segments.size() > 1) {
            segments.removeIf(Segment::tick);
        }
        if (segments.isEmpty()) {
            markDead();
        }
    }

    private final class Segment {
        Vec3d position;
        Vector3f offset;

        int age;
        int maxAge;

        Segment(Vec3d position) {
            this.position = position;
            this.offset = new Vector3f((float)(position.getX() - x), (float)(position.getY() - y), (float)(position.getZ() - z));
            this.maxAge = 90;
        }

        float getAlpha() {
            return colorAlpha * (1 - ((float)age / maxAge));
        }

        boolean tick() {
            return segments.indexOf(this) < segments.size() - 1 && age++ >= maxAge;
        }

        Vector3f[] getPlane(Segment to) {
            float fromX = offset.getX();
            float toX = to.offset.getX();

            float fromZ = offset.getZ();
            float toZ = to.offset.getZ();

            float fromTopY = offset.getY() + 1;
            float fromBottomY = offset.getY();

            float toTopY = to.offset.getY() + 1;
            float toBottomY = to.offset.getY();

            return new Vector3f[]{
                    new Vector3f(fromX, fromBottomY, fromZ), // bottom left
                    new Vector3f(fromX, fromTopY, fromZ), // top left
                    new Vector3f(toX, toTopY, toZ), // top right
                    new Vector3f(toX, toBottomY, toZ)  // bottom right
            };
        }
    }
}
