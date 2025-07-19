package net.paradise_client.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.paradise_client.Constants;
import net.paradise_client.Helper;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * InterchatPacket is a custom packet for transmitting chat commands or data
 * between clients and a proxy system using a unique UUID and command string.
 */
public record InterchatPacket(String uuid, String command) implements CustomPayload {

    // Codec used to serialize and deserialize this packet for networking
    public static final PacketCodec<PacketByteBuf, InterchatPacket> CODEC =
        CustomPayload.codecOf(InterchatPacket::write, InterchatPacket::new);

    // Unique identifier for the packet channel
    public static final CustomPayload.Id<InterchatPacket> ID =
        new CustomPayload.Id<>(Identifier.of("interchat", "main"));

    // Constructor that reads from buffer (used during packet decoding)
    private InterchatPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readString());
    }

    // Writes this packet's data to a PacketByteBuf
    public void write(PacketByteBuf buf) {
        try {
            // Write encoded proxy command data to the buffer
            buf.writeBytes(this.executeProxyCommand(UUID.fromString(this.uuid), this.command));
        } catch (Exception e) {
            Helper.printChatMessage("Error sending packet: " + e.getMessage());
            Constants.LOGGER.error("Error sending packet: ", e);
        }
    }

    // Returns the packet ID used by Minecraft networking
    public CustomPayload.Id<InterchatPacket> getId() {
        return ID;
    }

    /**
     * Encodes and wraps raw byte data for sending over the network.
     * Adds metadata like chunk count and protocol ID.
     */
    private byte[] forwardData(byte[] data) {
        try {
            byte[][] dataArray = InterchatPacket.divideArray(data, 32700);
            if (dataArray.length > 0) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                // Random unique message ID
                out.writeInt(new Random().nextInt());

                // Static identifier (could be used to indicate type or purpose)
                out.writeInt(42);

                // Number of data chunks to be sent
                out.writeInt(dataArray.length);

                // Protocol version or flag (e.g., 21)
                out.writeShort(21);

                // Write first chunk of data
                out.write(dataArray[0]);

                return out.toByteArray();
            }
        } catch (Exception e) {
            Helper.printChatMessage("Error forwarding data: " + e.getMessage());
            Constants.LOGGER.error("Error forwarding data: ", e);
        }

        return new byte[0]; // Fallback to empty byte array on error
    }

    /**
     * Builds and returns the full packet byte array for this command and UUID.
     */
    private byte[] executeProxyCommand(UUID player, String command) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // Write the UUID and command into the output stream
        this.writeUUID(out, player);
        this.writeString(out, command);

        // Wrap the data using the forwarding method
        return this.forwardData(out.toByteArray());
    }

    /**
     * Divides a byte array into smaller chunks of a specified size.
     * Useful when network protocols have maximum transmission size limits.
     */
    public static byte[][] divideArray(byte[] source, int chunkSize) {
        if (source.length <= chunkSize) {
            return new byte[][]{source};
        }

        int chunks = (int) Math.ceil((double) source.length / chunkSize);
        byte[][] ret = new byte[chunks][];

        int start = 0;
        for (int i = 0; i < chunks; ++i) {
            int end = Math.min(source.length, start + chunkSize);
            ret[i] = Arrays.copyOfRange(source, start, end);
            start += chunkSize;
        }
        return ret;
    }

    /**
     * Writes a UUID as two longs to the output stream.
     */
    private void writeUUID(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Writes a UTF-8 string to the output stream with a prefixed length.
     */
    private void writeString(ByteArrayDataOutput out, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length); // Write length first
        out.write(bytes);           // Then write actual bytes
    }
}
