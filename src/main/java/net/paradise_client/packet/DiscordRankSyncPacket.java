package net.paradise_client.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public record DiscordRankSyncPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DiscordRankSyncPacket> CODEC = CustomPayload.codecOf(DiscordRankSyncPacket::write, DiscordRankSyncPacket::new);
    public static final CustomPayload.Id<DiscordRankSyncPacket> ID = new CustomPayload.Id(Identifier.of("discordranksync", "command"));

    public DiscordRankSyncPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public void write(PacketByteBuf buf) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(this.command);
        buf.writeBytes(out.toByteArray());
    }

    public CustomPayload.Id<DiscordRankSyncPacket> getId() {
        return ID;
    }
}
