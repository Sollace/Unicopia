package com.minelittlepony.unicopia.particle;

import java.util.Locale;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class FollowingParticleEffect implements ParticleEffect {
    public static final ParticleEffect.Factory<FollowingParticleEffect> FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public FollowingParticleEffect read(ParticleType<FollowingParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            reader.expect(' ');
            float speed = reader.readFloat();
            return new FollowingParticleEffect(type, -1, new Vec3d(x, y, z), speed);
        }

        @Override
        public FollowingParticleEffect read(ParticleType<FollowingParticleEffect> particleType, PacketByteBuf buf) {
            return new FollowingParticleEffect(particleType, buf.readInt(), new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat());
        }
    };

    private final ParticleType<FollowingParticleEffect> type;

    private Vec3d fixedTarget;

    private int movingTarget;

    private final float followSpeed;

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Vec3d target, float followSpeed) {
        this.type = type;
        this.fixedTarget = target;
        this.movingTarget = -1;
        this.followSpeed = followSpeed;
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Entity target, float followSpeed) {
        this(type, target.getId(), target.getCameraPosVec(1), followSpeed);
    }

    private FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, int movingTarget, Vec3d fixedTarget, float followSpeed) {
        this.type = type;
        this.movingTarget = movingTarget;
        this.fixedTarget = fixedTarget;
        this.followSpeed = followSpeed;
    }

    public Vec3d getTarget(World world) {
        if (movingTarget > -1) {
            Entity e = world.getEntityById(movingTarget);
            if (e != null) {
                fixedTarget = e.getCameraPosVec(1);
            } else {
                movingTarget = -1;
            }
        }
        return fixedTarget;
    }

    public float getSpeed() {
        return followSpeed;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(movingTarget);
        buf.writeDouble(fixedTarget.x);
        buf.writeDouble(fixedTarget.y);
        buf.writeDouble(fixedTarget.z);
        buf.writeFloat(followSpeed);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(getType()), fixedTarget.x, fixedTarget.y, fixedTarget.z, followSpeed);
    }

}
