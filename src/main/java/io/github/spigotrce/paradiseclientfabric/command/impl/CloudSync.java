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

public class CloudSync extends Command {
    public CloudSync(MinecraftClient minecraftClient) {
        super("cloudsync", "Executes CloudSync Commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(this::showUsage)
            .then(argument("player", StringArgumentType.word())
                .executes(this::showUsage)
                .then(argument("command", StringArgumentType.greedyString())
                    .executes(this::executeCloudSyncCommand)));
    }

    private int executeCloudSyncCommand(CommandContext<CommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        String command = StringArgumentType.getString(context, "command");
        
        // Create payload
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(playerName);
        buf.writeString(command);
        
        Identifier channel = new Identifier("cloudsync", "command");
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);
        
        Helper.sendPacket(packet);
        Helper.printChatMessage("CloudSync payload sent!");
        return 1;
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: .cloudsync <player> <command>");
        return 1;
    }
}
