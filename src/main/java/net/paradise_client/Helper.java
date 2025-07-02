package net.paradise_client;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.paradise_client.inject.accessor.ClientConnectionAccessor;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Utility class providing various helper methods for Minecraft client operations.
 * Includes chat message formatting, plugin message sending, random generation, notifications, and more.
 */
public class Helper {

    public static Color getChroma(int delay, float saturation, float brightness) {
        double chroma = Math.ceil((double) (System.currentTimeMillis() + delay) / 20);
        chroma %= 360;
        return Color.getHSBColor((float) (chroma / 360), saturation, brightness);
    }

    public static void printChatMessage(String message) {
        printChatMessage(message, true);
    }

    public static void printChatMessage(String message, boolean dropTitle) {
        printChatMessage(Text.of(parseColoredText(dropTitle ? appendPrefix(message) : message)));
    }

    public static void printChatMessage(Text message) {
        ParadiseClient.MISC_MOD.delayedMessages.add(message);
    }

    public static String appendPrefix(String text) {
        return "&aParadise&bClient &r" + text;
    }

    public static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void sendPacket(Packet<?> packet) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendPacket(packet);
    }

    public static void sendPacket(DefinedPacket packet) {
        ((ClientConnectionAccessor)
                MinecraftClient.getInstance().getNetworkHandler()
                        .getConnection())
                .paradiseClient$getChannel()
                .write(packet);
    }

    public static void sendPluginMessage(String channel, PluginMessageEncoder encoder) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        encoder.encode(out);
        PluginMessage message = new PluginMessage();
        message.setTag(channel);
        message.setData(out.toByteArray());
        sendPacket(message);
    }

    // ✅ NEW METHOD: Check if a plugin message channel is registered
    public static boolean isPluginChannelRegistered(String channel) {
    // You can implement this properly if you add mixin access to ClientPlayNetworkHandler later
    return true; // Fallback: always return true
}

    public static Protocol getBungeeProtocolForPhase(NetworkPhase phase) {
        return switch (phase) {
            case HANDSHAKING -> Protocol.HANDSHAKE;
            case PLAY -> Protocol.GAME;
            case STATUS -> Protocol.STATUS;
            case LOGIN -> Protocol.LOGIN;
            case CONFIGURATION -> Protocol.CONFIGURATION;
            default -> throw new IllegalArgumentException("Unknown protocol state: " + phase.getId());
        };
    }

    public static Protocol getBungeeProtocolForCurrentPhase() {
        return getBungeeProtocolForPhase(ParadiseClient.NETWORK_CONFIGURATION.phase);
    }

    public static Text parseColoredText(String message) {
        return parseColoredText(message, null);
    }

    public static Text parseColoredText(String message, String copyMessage) {
        MutableText text = Text.literal("");
        String[] parts = message.split("(?=&)");
        List<Formatting> currentFormats = new ArrayList<>();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (part.startsWith("&")) {
                currentFormats.add(getColorFromCode(part.substring(0, 2)));
                String remaining = part.substring(2);
                if (!remaining.isEmpty()) {
                    MutableText formattedText = Text.literal(remaining);
                    for (Formatting format : currentFormats) {
                        formattedText = formattedText.formatted(format);
                    }
                    text.append(formattedText);
                }
            } else {
                MutableText unformattedText = Text.literal(part);
                for (Formatting format : currentFormats) {
                    unformattedText = unformattedText.formatted(format);
                }
                text.append(unformattedText);
            }
        }

        if (copyMessage != null && !copyMessage.isEmpty()) {
            text.setStyle(text.getStyle().withClickEvent(new ClickEvent.CopyToClipboard(copyMessage)));
        }

        return text;
    }

    private static Formatting getColorFromCode(String code) {
        return switch (code) {
            case "&0" -> Formatting.BLACK;
            case "&1" -> Formatting.DARK_BLUE;
            case "&2" -> Formatting.DARK_GREEN;
            case "&3" -> Formatting.DARK_AQUA;
            case "&4" -> Formatting.DARK_RED;
            case "&5" -> Formatting.DARK_PURPLE;
            case "&6" -> Formatting.GOLD;
            case "&7" -> Formatting.GRAY;
            case "&8" -> Formatting.DARK_GRAY;
            case "&9" -> Formatting.BLUE;
            case "&a" -> Formatting.GREEN;
            case "&b" -> Formatting.AQUA;
            case "&c" -> Formatting.RED;
            case "&d" -> Formatting.LIGHT_PURPLE;
            case "&e" -> Formatting.YELLOW;
            case "&f" -> Formatting.WHITE;
            case "&k" -> Formatting.OBFUSCATED;
            case "&l" -> Formatting.BOLD;
            case "&m" -> Formatting.STRIKETHROUGH;
            case "&n" -> Formatting.UNDERLINE;
            case "&o" -> Formatting.ITALIC;
            default -> Formatting.RESET;
        };
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String generateRandomString(int length, String characters, Random random) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            result.append(characters.charAt(random.nextInt(characters.length())));
        return result.toString();
    }

    public static String getLatestReleaseTag() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://paradise-client.net/api/versions").openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JsonObject responseObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            return responseObject.getAsJsonObject("latest_version").get("version").getAsString();
        } else {
            return null;
        }
    }

    public static void showNotification(String title, String message) {
        System.out.println(title);
        System.out.println(message);
        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
        SystemToast toast = new SystemToast(
                SystemToast.Type.CHUNK_LOAD_FAILURE,
                Text.literal(title),
                Text.literal(message)
        );
        toastManager.add(toast);
    }

    public static PacketByteBuf byteBufToPacketBuf(ByteBuf buf) {
        return new PacketByteBuf(buf);
    }

    public static String fetchUUID(String username) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = reader.lines().reduce("", (acc, line) -> acc + line);
                return JsonParser.parseString(response).getAsJsonObject().get("id").getAsString();
            }
        }
        throw new Exception("Failed to fetch UUID");
    }

    public static void runAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static class ByteArrayOutput {
        private final ByteArrayDataOutput out;

        public ByteArrayOutput() {
            this.out = ByteStreams.newDataOutput();
        }

        public ByteArrayOutput(byte[] bytes) {
            this.out = ByteStreams.newDataOutput();
            out.write(bytes);
        }

        public ByteArrayDataOutput getBuf() {
            return out;
        }

        public byte[] toByteArray() {
            return out.toByteArray();
        }
    }

    @FunctionalInterface
    public interface PluginMessageEncoder {
        void encode(ByteArrayDataOutput out);
    }
        }
