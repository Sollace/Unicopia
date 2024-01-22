package com.minelittlepony.unicopia.particle;


import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class TargetBoundParticleEffect implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<TargetBoundParticleEffect> FACTORY = ParticleFactoryHelper.of(TargetBoundParticleEffect::new, TargetBoundParticleEffect::new);

    private final ParticleType<TargetBoundParticleEffect> type;
    private final int targetId;

    protected TargetBoundParticleEffect(ParticleType<TargetBoundParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this.type = type;
        this.targetId = -1;
    }

    protected TargetBoundParticleEffect(ParticleType<TargetBoundParticleEffect> type, PacketByteBuf buf) {
        this.type = type;
        this.targetId = buf.readInt();
    }

    public TargetBoundParticleEffect(ParticleType<TargetBoundParticleEffect> type, Entity target) {
        this.type = type;
        this.targetId = target.getId();
    }

    public int getTargetId() {
        return targetId;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(targetId);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s", Registries.PARTICLE_TYPE.getId(getType()), targetId);
    }
}
