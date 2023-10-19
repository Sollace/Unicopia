package com.minelittlepony.unicopia.particle;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WeakTarget {
    Vec3d fixedPosition;
    private int targetId;

    public WeakTarget(Vec3d fixedPosition, @Nullable Entity entity) {
        this.fixedPosition = fixedPosition;
        targetId = entity == null ? -1 : entity.getId();
    }

    public WeakTarget(PacketByteBuf buf) {
        fixedPosition = ParticleFactoryHelper.readVector(buf);
        targetId = buf.readInt();
    }

    public WeakTarget(StringReader reader) throws CommandSyntaxException {
        this(ParticleFactoryHelper.readVector(reader), null);
    }

    public Vec3d getPosition(World world) {
        if (targetId > -1) {
            Entity e = world.getEntityById(targetId);
            if (e != null) {
                fixedPosition = e.getCameraPosVec(1);
            } else {
                targetId = -1;
            }
        }
        return fixedPosition;
    }

    @Override
    public String toString() {
        if (targetId > -1) {
            return "Moving(" + targetId + ")";
        }
        return "Fixed(" + fixedPosition + ")";
    }

    public void write(PacketByteBuf buf) {
        ParticleFactoryHelper.writeVector(buf, fixedPosition);
        buf.writeInt(targetId);
    }
}
