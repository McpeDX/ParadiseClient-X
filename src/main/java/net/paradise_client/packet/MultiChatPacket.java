package net.paradise_client.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier; // ✅ Import added

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public record MultiChatPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, MultiChatPacket> CODEC = CustomPayload.codecOf(MultiChatPacket::write, MultiChatPacket::new);
    public static final Id<MultiChatPacket> ID = new Id<>(Identifier.of("multichat", "act"));

    private MultiChatPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public static void send(String command) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
            .sendPacket(new CustomPayloadC2SPacket(new MultiChatPacket(command)));
    }

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

    public Id<MultiChatPacket> getId() {
        return ID;
    }
}
