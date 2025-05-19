package com.minelittlepony.unicopia.entity.collision;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MultiBox extends Box {
    private final Box first;
    private final BoxChildren children;

    public static MultiBox of(Box first, List<Box> children) {
        return new MultiBox(first, new BoxChildren(children));
    }

    public static Box unbox(Box box) {
        return box instanceof MultiBox m ? m.first : box;
    }

    public static void forEach(Box box, Consumer<Box> consumer) {
        if (box instanceof MultiBox m) {
            m.children.forEach(consumer);
        }
    }

    private MultiBox(Box first, BoxChildren children) {
        super(first.minX, first.minY, first.minZ, first.maxX, first.maxY, first.maxZ);
        this.first = unbox(first);
        this.children = children;
    }

    @Override
    public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
        return first.raycast(min, max).or(() -> children.raycast(min, max));
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return super.contains(x, y, z) || children.contains(x, y, z);
    }

    @Override
    public Box withMinX(double minX) {
        return new MultiBox(super.withMinX(minX), children);
    }

    @Override
    public Box withMinY(double minY) {
        return new MultiBox(super.withMinY(minY), children);
    }

    @Override
    public Box withMinZ(double minZ) {
        return new MultiBox(super.withMinZ(minZ), children);
    }

    @Override
    public Box withMaxX(double maxX) {
        return new MultiBox(super.withMaxX(maxX), children);
    }

    @Override
    public Box withMaxY(double maxY) {
        return new MultiBox(super.withMaxY(maxY), children);
    }

    @Override
    public Box withMaxZ(double maxZ) {
        return new MultiBox(super.withMaxZ(maxZ), children);
    }

    @Override
    public Box shrink(double x, double y, double z) {
        return new MultiBox(super.shrink(x, y, z), children.altered(b -> b.shrink(x, y, z)));
    }

    @Override
    public Box stretch(double x, double y, double z) {
        return new MultiBox(super.stretch(x, y, z), children.altered(b -> b.stretch(x, y, z)));
    }

    @Override
    public Box expand(double x, double y, double z) {
        return new MultiBox(super.expand(x, y, z), children.altered(b -> b.expand(x, y, z)));
    }

    @Override
    public Box intersection(Box box) {
        return new MultiBox(super.intersection(box), children);
    }

    @Override
    public Box union(Box box) {
        return new MultiBox(super.union(box), children);
    }

    @Override
    public Box offset(double x, double y, double z) {
        return new MultiBox(super.offset(x, y, z), children.altered(b -> b.offset(x, y, z)));
    }

    @Override
    public Box offset(BlockPos blockPos) {
        return new MultiBox(super.offset(blockPos), children.altered(b -> b.offset(blockPos)));
    }

    @Override
    public String toString() {
        return "MULTI_AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]{" + children + "}";
    }

    static final class BoxChildren {
        private final Box[] children;
        private final Supplier<String> toString;

        private BoxChildren(Box[] children) {
            this.children = children;
            toString = Suppliers.memoize(() -> Arrays.stream(this.children).map(Box::toString).collect(Collectors.joining(",")));
        }

        public BoxChildren(List<Box> children) {
            this(children.stream().map(MultiBox::unbox).toArray(Box[]::new));
        }

        public BoxChildren altered(Function<Box, Box> alteration) {
            BoxChildren copy = new BoxChildren(new Box[children.length]);
            for (int i = 0; i < children.length; i++) {
                copy.children[i] = alteration.apply(children[i]);
            }
            return copy;
        }

        public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
            Optional<Vec3d> trace = Optional.empty();
            for (int i = 0; trace.isEmpty() && i < children.length; i++) {
                trace = children[i].raycast(min, max);
            }
            return trace;
        }

        public boolean contains(double x, double y, double z) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].contains(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        public void forEach(Consumer<Box> consumer) {
            for (int i = 0; i < children.length; i++) {
                consumer.accept(children[i]);
            }
        }

        @Override
        public String toString() {
            return toString.get();
        }
    }
}
