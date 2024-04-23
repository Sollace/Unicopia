package com.minelittlepony.unicopia.particle;

import java.util.Optional;

import com.minelittlepony.unicopia.util.serialization.PacketCodec;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

public interface ParticleFactoryHelper {
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    PacketCodec<ParticleEffect> PARTICLE_EFFECT_CODEC = PacketCodec.ofRegistry(Registries.PARTICLE_TYPE).andThen(
            (buf, type) -> type.getParametersFactory().read((ParticleType) type, buf),
            ParticleEffect::getType,
            (buf, effect) -> effect.write(buf)
    );
    PacketCodec<Optional<ParticleEffect>> OPTIONAL_PARTICLE_EFFECT_CODEC = PARTICLE_EFFECT_CODEC.asOptional();
    PacketCodec<Vec3d> VECTOR_CODEC = new PacketCodec<>(
            buf -> new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            (buf, vector) -> {
                buf.writeDouble(vector.x);
                buf.writeDouble(vector.y);
                buf.writeDouble(vector.z);
            }
    );
    PacketCodec<Optional<Vec3d>> OPTIONAL_VECTOR_CODEC = VECTOR_CODEC.asOptional();

    @SuppressWarnings("unchecked")
    static <T extends ParticleEffect> T read(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return (T)ParticleEffectArgumentType.readParameters(reader, Registries.PARTICLE_TYPE.getReadOnlyWrapper());
    }

    static Vec3d readVector(StringReader reader) throws CommandSyntaxException {
        return new Vec3d(readDouble(reader), readDouble(reader), readDouble(reader));
    }

    static boolean readBoolean(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readBoolean();
    }

    static <T> Optional<T> readOptional(StringReader reader, ReaderFunc<T> readFunc) throws CommandSyntaxException {
        if (reader.canRead()) {
            return Optional.ofNullable(readFunc.read(reader));
        }
        return Optional.empty();
    }

    static double readDouble(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readDouble();
    }

    static float readFloat(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readFloat();
    }

    static int readInt(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readInt();
    }

    @SuppressWarnings("deprecation")
    static <T extends ParticleEffect> ParticleEffect.Factory<T> of(CommandReader<T> commandReader, PacketReader<T> packetReader) {
        return new ParticleEffect.Factory<>() {
            @Override
            public T read(ParticleType<T> type, StringReader reader) throws CommandSyntaxException {
                return commandReader.read(type, reader);
            }
            @Override
            public T read(ParticleType<T> type, PacketByteBuf buf) {
                return packetReader.read(type, buf);
            }
        };
    }

    interface ReaderFunc<T> {
        T read(StringReader reader) throws CommandSyntaxException;
    }

    interface CommandReader<T extends ParticleEffect> {
        T read(ParticleType<T> type, StringReader reader) throws CommandSyntaxException;
    }

    interface PacketReader<T extends ParticleEffect> {
        T read(ParticleType<T> type, PacketByteBuf buf);
    }
}
