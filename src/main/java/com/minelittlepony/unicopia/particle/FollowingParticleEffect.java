package com.minelittlepony.unicopia.particle;

import java.util.Locale;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public class FollowingParticleEffect implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<FollowingParticleEffect> FACTORY = ParticleFactoryHelper.of(FollowingParticleEffect::new, FollowingParticleEffect::new);

    private final ParticleType<FollowingParticleEffect> type;

    private Vec3d fixedTarget;

    private int movingTarget;

    private final float followSpeed;

    private Optional<ParticleEffect> childEffect = Optional.empty();

    protected FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(type, -1, ParticleFactoryHelper.readVector(reader), ParticleFactoryHelper.readFloat(reader));

        if (reader.canRead()) {
            reader.expect(' ');
            childEffect = ParticleFactoryHelper.read(reader);
        }
    }

    protected FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, PacketByteBuf buf) {
        this(type, buf.readInt(), new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat());

        if (buf.readBoolean()) {
            childEffect = ParticleFactoryHelper.read(Registries.PARTICLE_TYPE.get(buf.readInt()), buf);
        }
    }

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

    public ParticleEffect withChild(ParticleEffect child) {
        childEffect = Optional.of(child);
        return this;
    }

    public Optional<ParticleEffect> getChildEffect() {
        return childEffect;
    }

    public String getTargetDescriptor() {
        if (movingTarget > -1) {
            return "Moving(" + movingTarget + ")";
        }
        return fixedTarget.toString();
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
        getChildEffect().ifPresentOrElse(child -> {
            buf.writeBoolean(true);
            buf.writeInt(Registries.PARTICLE_TYPE.getRawId(child.getType()));
            child.write(buf);
        }, () -> buf.writeBoolean(false));
    }

    @Override
    public String asString() {
        return getChildEffect().map(child -> {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %s",
                    Registries.PARTICLE_TYPE.getId(getType()),
                    fixedTarget.x, fixedTarget.y, fixedTarget.z,
                    followSpeed, child.asString());
        }).orElseGet(() -> {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f",
                    Registries.PARTICLE_TYPE.getId(getType()),
                    fixedTarget.x, fixedTarget.y, fixedTarget.z,
                    followSpeed);
        });

    }
}
