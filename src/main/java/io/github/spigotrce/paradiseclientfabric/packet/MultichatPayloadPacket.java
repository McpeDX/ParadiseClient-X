package io.github.spigotrce.paradiseclientfabric.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import com.google.common.io.ByteArrayDataOutput;
import java.io.IOException;
import com.google.common.io.ByteStreams;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record MultichatPayloadPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, MultichatPayloadPacket> CODEC = CustomPayload.codecOf(MultichatPayloadPacket::write, MultichatPayloadPacket::new);
    public static final CustomPayload.Id<MultichatPayloadPacket> ID = new CustomPayload.Id(Identifier.of("multichat", "act"));

    private MultichatPayloadPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public void write(PacketByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF(this.command);
            buf.writeBytes(stream.toByteArray());
        }
        catch (IOException e) {
            Helper.printChatMessage(e.getMessage());
        }
    }

    public CustomPayload.Id<MultichatPayloadPacket> getId() {
        return ID;
    }
}
