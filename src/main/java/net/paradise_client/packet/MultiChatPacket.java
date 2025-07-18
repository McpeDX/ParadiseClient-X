package net.paradise_client.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public record MultiChatPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, MultiChatPacket> CODEC = CustomPayload.codecOf(MultiChatPacket::write, MultiChatPacket::new);
    public static final Id<MultiChatPacket> ID = new Id<>(Identifier.of("multichat", "act"));

    /**
     * Private constructor used for deserialization of the packet.
     *
     * @param buf The buffer containing the serialized packet data.
     */
    private MultiChatPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    /**
     * Sends the MultiChat data packet to the server.
     *
     * @param command The command to execute in the console.
     */
    public static void send(String command) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
            .sendPacket(new CustomPayloadC2SPacket(new MultiChatPacket(command)));
    }

    /**
     * Serializes the packet data into the provided buffer.
     *
     * @param buf The buffer to which the packet data will be written.
     */
    public void write(PacketByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF(command);
            buf.writeBytes(stream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the ID of this custom payload packet.
     *
     * @return The ID representing this custom payload packet in communication with the server.
     */
    public Id<MultiChatPacket> getId() {
        return ID;
    }
}
