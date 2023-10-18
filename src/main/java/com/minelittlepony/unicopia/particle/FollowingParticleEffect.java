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

public record FollowingParticleEffect (
        ParticleType<FollowingParticleEffect> type,
        WeakTarget target,
        float followSpeed,
        Optional<ParticleEffect> childEffect
    ) implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<FollowingParticleEffect> FACTORY = ParticleFactoryHelper.of(FollowingParticleEffect::new, FollowingParticleEffect::new);

    protected FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(type,
                new WeakTarget(reader),
                ParticleFactoryHelper.readFloat(reader),
                ParticleFactoryHelper.readOptional(reader, r -> {
                    r.expect(' ');
                    return ParticleFactoryHelper.read(r);
                }));
    }

    protected FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, PacketByteBuf buf) {
        this(type,
            new WeakTarget(buf),
            buf.readFloat(),
            buf.readOptional(b -> ParticleFactoryHelper.readEffect(b))
        );
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Vec3d target, float followSpeed) {
        this(type, new WeakTarget(target, (Entity)null), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Entity target, float followSpeed) {
        this(type, new WeakTarget(target.getCameraPosVec(1), target), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect withChild(ParticleEffect child) {
        return new FollowingParticleEffect(type, target, followSpeed, Optional.of(child));
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        target.write(buf);
        buf.writeFloat(followSpeed);
        buf.writeOptional(childEffect(), (b, child) -> {
            b.writeBoolean(true);
            b.writeInt(Registries.PARTICLE_TYPE.getRawId(child.getType()));
            child.write(buf);
        });
    }

    @Override
    public String asString() {
        return childEffect().map(child -> {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %s",
                    Registries.PARTICLE_TYPE.getId(getType()),
                    target.fixedPosition.x,
                    target.fixedPosition.y,
                    target.fixedPosition.z,
                    followSpeed, child.asString());
        }).orElseGet(() -> {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f",
                    Registries.PARTICLE_TYPE.getId(getType()),
                    target.fixedPosition.x,
                    target.fixedPosition.y,
                    target.fixedPosition.z,
                    followSpeed);
        });

    }
}
