package com.minelittlepony.unicopia.particle;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WeakTarget {
    public static final Codec<WeakTarget> CODEC = CodecUtils.xor(
            RecordCodecBuilder.create(instance -> instance.group(
                    CodecUtils.VECTOR.fieldOf("position").forGetter(i -> i.fixedPosition),
                    Codec.INT.fieldOf("targetId").forGetter(i -> i.targetId)
            ).apply(instance, WeakTarget::new)),
            CodecUtils.VECTOR.xmap(pos -> new WeakTarget(pos, null), target -> target.fixedPosition)
    );
    public static final PacketCodec<PacketByteBuf, WeakTarget> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecUtils.VECTOR, i -> i.fixedPosition,
            PacketCodecs.INTEGER, i -> i.targetId,
            WeakTarget::new
    );

    Vec3d fixedPosition;
    private int targetId;

    private WeakTarget(Vec3d fixedPosition, int targetId) {
        this.fixedPosition = fixedPosition;
        this.targetId = targetId;
    }

    public WeakTarget(Vec3d fixedPosition, @Nullable Entity entity) {
        this(fixedPosition, entity == null ? -1 : entity.getId());
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
}
