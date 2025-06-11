package io.github.spigotrce.paradiseclientfabric.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ObjectOutputStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record BungeeCommandPayloadPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, BungeeCommandPayloadPacket> CODEC = CustomPayload.codecOf(BungeeCommandPayloadPacket::write, BungeeCommandPayloadPacket::new);
    public static final CustomPayload.Id<BungeeCommandPayloadPacket> ID = new CustomPayload.Id(Identifier.of("atlas", "out"));

    private BungeeCommandPayloadPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public void write(PacketByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oStream = new ObjectOutputStream(stream);
            oStream.writeUTF("commandBungee");
            oStream.writeObject(this.command);
            buf.writeBytes(stream.toByteArray());
        }
        catch (IOException e) {
            Helper.printChatMessage(e.getMessage());
        }
    }

    public CustomPayload.Id<BungeeCommandPayloadPacket> getId() {
        return ID;
    }
}
