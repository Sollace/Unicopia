package com.minelittlepony.unicopia.client.render.bezier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector3f;

import net.minecraft.util.math.Vec3d;

public class Ribbon implements Iterable<BezierSegment> {
    public float width;
    public float angle;

    private final List<Node> nodes = new ArrayList<>();
    private final List<BezierSegment> segments = new ArrayList<>();
    private Node lastNode;

    public Ribbon(Vec3d position, Vector3f bottom, Vector3f top, float angle) {
        this.angle = angle;
        lastNode = new Node(position, position.toVector3f().add(bottom), position.toVector3f().add(top));
        nodes.add(lastNode);
    }

    public void addNode(Vec3d position, float angle) {
        Vector3f directionVector = position.subtract(lastNode.position()).toVector3f();

        Vector3f bottom = lastNode.bottom().add(directionVector).rotateAxis(angle, directionVector.x, directionVector.y, directionVector.z);
        Vector3f top = lastNode.top().add(directionVector).rotateAxis(angle, directionVector.x, directionVector.y, directionVector.z);

        Node oldNode = lastNode;
        lastNode = new Node(position, bottom, top);
        nodes.add(lastNode);
        segments.add(new BezierSegment(new Vector3f[] {
                oldNode.bottom(),
                oldNode.top(),
                lastNode.bottom(),
                lastNode.top()
        }));
    }

    @Override
    public Iterator<BezierSegment> iterator() {
        return segments.iterator();
    }

    record Node(Vec3d position, Vector3f bottom, Vector3f top) {

    }
}
