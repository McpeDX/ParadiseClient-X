package io.github.spigotrce.paradiseclientfabric.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record T2CPayloadPacket(String command) implements CustomPayload
{
    public static final PacketCodec<PacketByteBuf, T2CPayloadPacket> CODEC = CustomPayload.codecOf(T2CPayloadPacket::write, T2CPayloadPacket::new);
    public static final CustomPayload.Id<T2CPayloadPacket> ID = new CustomPayload.Id(Identifier.of("t2c", "bcmd"));

    public T2CPayloadPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public void write(PacketByteBuf buf) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("T2Code-Console");
        out.writeUTF(this.command);
        buf.writeBytes(out.toByteArray());
    }

    public CustomPayload.Id<T2CPayloadPacket> getId() {
        return ID;
    }
}
