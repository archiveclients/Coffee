/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper.event.events;

import cf.coffee.client.helper.event.events.base.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {

    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }
}
