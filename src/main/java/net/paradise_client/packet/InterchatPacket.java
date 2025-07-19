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

public record InterchatPacket(String uuid, String command) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, InterchatPacket> CODEC = CustomPayload.codecOf(InterchatPacket::write, InterchatPacket::new);
    public static final CustomPayload.Id<InterchatPacket> ID = new CustomPayload.Id(Identifier.of("interchat", "main"));

    private InterchatPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readString());
    }

    public void write(PacketByteBuf buf) {
        try {
            buf.writeBytes(this.executeProxyCommand(UUID.fromString(this.uuid), this.command));
        }
        catch (Exception e) {
            Helper.printChatMessage(("Error sending packet: " + e.getMessage()));
            Constants.LOGGER.error("Error sending packet: ", e);
        }
    }

    public CustomPayload.Id<InterchatPacket> getId() {
        return ID;
    }

    private byte[] forwardData(byte[] data) {
        try {
            byte[][] dataArray = InterchatPacket.divideArray(data, 32700);
            int i = 0;
            if (i < dataArray.length) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(new Random().nextInt());
                out.writeInt;
                out.writeInt(dataArray.length);
                out.writeShort(21);
                out.write;
                return out.toByteArray();
            }
        }
        catch (Exception e) {
            Helper.printChatMessage(("Error sending packet: " + e.getMessage()));
            Constants.LOGGER.error("Error sending packet: ", e);
        }
        return new byte[0];
    }

    private byte[] executeProxyCommand(UUID player, String command) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        this.writeUUID(out, player);
        this.writeString(out, command);
        return this.forwardData(out.toByteArray());
    }

    public static byte[][] divideArray(byte[] source, int chunkSize) {
        int length = (int) (double) (source.length / chunkSize);
        if (length <= 1) {
            return new byte[][]{source};
        }
        byte[][] ret = new byte[length][];
        int start = 0;
        for (int i = 0; i < ret.length; ++i) {
            int end = start + chunkSize;
            ret[i] = Arrays.copyOfRange(source, start, Math.min(end, source.length));
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
        out.write;
    }
  }
      
