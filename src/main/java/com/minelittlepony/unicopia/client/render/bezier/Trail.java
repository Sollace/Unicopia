package com.minelittlepony.unicopia.client.render.bezier;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

import net.minecraft.util.math.Vec3d;

public class Trail {

    private final List<Segment> segments = new ArrayList<>();

    public final Vec3d pos;

    private final float height;

    public Trail(Vec3d pos, float height) {
        this.pos = pos;
        this.height = height;
        segments.add(new Segment(pos));
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void update(Vec3d newPosition) {
        if (segments.isEmpty()) {
            segments.add(new Segment(newPosition));
        } else {
            Vec3d last = segments.get(segments.size() - 1).position;
            if (newPosition.distanceTo(last) > 0.2) {
                segments.add(new Segment(newPosition));
            }
        }
    }

    public boolean tick() {
        if (segments.size() > 1) {
            segments.removeIf(Segment::tick);
        }
        return segments.isEmpty();
    }

    public final class Segment {
        Vec3d position;
        Vector3f offset;

        int age;
        int maxAge;

        Segment(Vec3d position) {
            this.position = position;
            this.offset = position.subtract(pos).toVector3f();
            this.maxAge = 90;
        }

        public float getAlpha() {
            return (1 - ((float)age / maxAge));
        }

        boolean tick() {
            return segments.indexOf(this) < segments.size() - 1 && age++ >= maxAge;
        }

        public BezierSegment getPlane(Segment to) {
            return new BezierSegment(offset, to.offset, height);
        }
    }
}
