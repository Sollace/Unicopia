package com.minelittlepony.unicopia.mixin.forgified;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;

@Pseudo
@Mixin(targets = "net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor")
interface MixinPacketAndPayloadAcceptor<L extends ClientCommonPacketListener> extends Consumer<Packet<? extends L>> {

}
