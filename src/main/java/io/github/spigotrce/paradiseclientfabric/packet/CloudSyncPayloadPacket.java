package io.github.spigotrce.paradiseclientfabric.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record CloudSyncPayloadPacket(String playerName, String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, CloudSyncPayloadPacket> CODEC = CustomPayload.codecOf(CloudSyncPayloadPacket::write, CloudSyncPayloadPacket::new);
    public static final CustomPayload.Id<CloudSyncPayloadPacket> ID = new CustomPayload.Id(Identifier.of("plugin", "cloudsync"));

    public CloudSyncPayloadPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readString());
    }

    public void write(PacketByteBuf buf) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(this.playerName);
        out.writeUTF(this.command);
        buf.writeBytes(out.toByteArray());
    }

    public CustomPayload.Id<CloudSyncPayloadPacket> getId() {
        return ID;
    }
}
