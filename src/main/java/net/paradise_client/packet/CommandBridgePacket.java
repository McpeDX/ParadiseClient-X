package net.paradise_client.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record CommandBridgePacket(String command, String serverID) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, CommandBridgePacket> CODEC = CustomPayload.codecOf(CommandBridgePacket::write, CommandBridgePacket::new);
    public static final CustomPayload.Id<CommandBridgePacket> ID = new CustomPayload.Id(Identifier.of("commandbridge", "main"));

    public CommandBridgePacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readString());
    }

    public void write(PacketByteBuf buf) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ExecuteCommand");
        out.writeUTF(this.serverID);
        out.writeUTF(UUID.randomUUID().toString());
        out.writeUTF("console");
        out.writeUTF(UUID.randomUUID().toString());
        out.writeUTF(this.command);
        buf.writeBytes(out.toByteArray());
    }

    public CustomPayload.Id<CommandBridgePacket> getId() {
        return ID;
    }
}
