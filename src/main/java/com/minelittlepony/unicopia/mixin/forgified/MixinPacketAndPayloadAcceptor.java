package com.minelittlepony.unicopia.mixin.forgified;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;

@Pseudo
@Mixin(targets = "net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor")
abstract class MixinPacketAndPayloadAcceptor<L extends ClientCommonPacketListener> implements Consumer<Packet<? super L>> {
    @Shadow(remap = false)
    private @Final Consumer<Packet<? super L>> consumer;

    @Override
    public void accept(Packet<? super L> packet) {
        consumer.accept(packet);
    }
}
