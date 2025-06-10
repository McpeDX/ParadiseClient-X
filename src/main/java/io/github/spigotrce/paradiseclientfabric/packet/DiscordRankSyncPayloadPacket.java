package io.github.spigotrce.paradiseclientfabric.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DiscordRankSyncPayloadPacket(String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DiscordRankSyncPayloadPacket> CODEC =
            CustomPayload.codecOf(DiscordRankSyncPayloadPacket::write, DiscordRankSyncPayloadPacket::new);

    public static final CustomPayload.Id<DiscordRankSyncPayloadPacket> ID =
            new CustomPayload.Id<>(new Identifier("discordranksync", "command"));

    public DiscordRankSyncPayloadPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    public static void write(DiscordRankSyncPayloadPacket packet, PacketByteBuf buf) {
        buf.writeString(packet.command);
    }

    @Override
    public CustomPayload.Id<DiscordRankSyncPayloadPacket> getId() {
        return ID;
    }
}
