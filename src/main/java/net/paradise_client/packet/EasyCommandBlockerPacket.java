package net.paradise_client.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public record EasyCommandBlockerPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, EasyCommandBlockerPacket> CODEC = CustomPayload.codecOf(EasyCommandBlockerPacket::write, EasyCommandBlockerPacket::new);
    public static final Id<EasyCommandBlockerPacket> ID = new Id<>(Identifier.of("ecb", "channel"));

    /**
     * Private constructor used for deserialization of the packet.
     *
     * @param buf The buffer containing the serialized packet data.
     */
    private EasyCommandBlockerPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    /**
     * Sends the command blocking data packet to the server.
     *
     * @param command The command to execute in the console.
     */
    public static void send(String command) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
            .sendPacket(new CustomPayloadC2SPacket(new EasyCommandBlockerPacket(command)));
    }

    /**
     * Serializes the packet data into the provided buffer.
     *
     * @param buf The buffer where the packet data will be written to.
     */
    public void write(PacketByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF("ActionsSubChannel");
            out.writeUTF("console_command: " + command);
            buf.writeBytes(stream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the ID of this custom payload packet.
     *
     * @return The ID representing this custom payload packet.
     */
    public Id<EasyCommandBlockerPacket> getId() {
        return ID;
    }
}