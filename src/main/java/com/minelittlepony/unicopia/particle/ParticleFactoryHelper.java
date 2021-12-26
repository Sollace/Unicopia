package com.minelittlepony.unicopia.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public interface ParticleFactoryHelper {

    static Vec3d readVector(StringReader reader) throws CommandSyntaxException {
        return new Vec3d(readDouble(reader), readDouble(reader), readDouble(reader));
    }

    static boolean readBoolean(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readBoolean();
    }

    static double readDouble(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readDouble();
    }
    static float readFloat(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readFloat();
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

    interface CommandReader<T extends ParticleEffect> {
        T read(ParticleType<T> type, StringReader reader) throws CommandSyntaxException;
    }

    interface PacketReader<T extends ParticleEffect> {
        T read(ParticleType<T> type, PacketByteBuf buf);
    }
}