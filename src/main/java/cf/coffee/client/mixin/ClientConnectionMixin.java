/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.misc.AntiPacketKick;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.helper.util.Utils;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void dispatchPacketGet(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (Events.fireEvent(EventType.PACKET_RECEIVE, new PacketEvent(packet))) {
            ci.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    public void catchException(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (ModuleRegistry.getByClass(AntiPacketKick.class).isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;)V", cancellable = true, at = @At("HEAD"))
    public void dispatchPacketSend(Packet<?> packet, CallbackInfo ci) {
        if (!Utils.sendPackets) return;
        if (Events.fireEvent(EventType.PACKET_SEND, new PacketEvent(packet))) {
            ci.cancel();
        }
    }

}
