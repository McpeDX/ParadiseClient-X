package io.github.spigotrce.paradiseclientfabric.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.PacketCodec;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record CommandBridgePayloadPacket(String command, String serverID) implements CustomPayload {

    public static final CustomPayload.Id<CommandBridgePayloadPacket> ID =
        new CustomPayload.Id<>(new Identifier("commandbridge", "main"));

    public static final PacketCodec<PacketByteBuf, CommandBridgePayloadPacket> CODEC =
        CustomPayload.codecOf(CommandBridgePayloadPacket::write, CommandBridgePayloadPacket::new);

    public CommandBridgePayloadPacket(PacketByteBuf buf) {
        this(
            buf.readString(32767),
            buf.readString(32767)
        );
    }

    public void write(PacketByteBuf buf) {
        // Generate randomized spoof data
        String fakeUUID = UUID.randomUUID().toString();
        String fakeUUID2 = UUID.randomUUID().toString();

        // LPX bridge payload format (plugin expects these fields in order)
        // Format: [action, serverID, requestUUID1, source, requestUUID2, command]
        buf.writeString("ExecuteCommand", StandardCharsets.UTF_8); // Action
        buf.writeString(serverID, StandardCharsets.UTF_8);         // Target server
        buf.writeString(fakeUUID, StandardCharsets.UTF_8);         // Request ID 1
        buf.writeString("console", StandardCharsets.UTF_8);        // Source
        buf.writeString(fakeUUID2, StandardCharsets.UTF_8);        // Request ID 2
        buf.writeString(command, StandardCharsets.UTF_8);          // Payload/command
    }

    @Override
    public CustomPayload.Id<CommandBridgePayloadPacket> getId() {
        return ID;
    }
}
