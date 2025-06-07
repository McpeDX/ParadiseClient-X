package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.client.command.CommandManager.literal;
import static net.minecraft.client.command.CommandManager.argument;

public class Interchat extends Command {
    public Interchat(MinecraftClient minecraftClient) {
        super("interchat", "Forces player commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(this::showUsage)
            .then(argument("user", StringArgumentType.word())
                .suggests(this::suggestPlayers)
                .executes(this::showUsage)
                .then(argument("command", StringArgumentType.greedyString())
                    .executes(this::executeCommand)));
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        String user = StringArgumentType.getString(context, "user");
        String command = StringArgumentType.getString(context, "command");

        for (PlayerListEntry player : getMinecraftClient().getNetworkHandler().getPlayerList()) {
            if (player.getProfile().getName().equalsIgnoreCase(user)) {
                String uuid = player.getProfile().getId().toString();
                
                // Create proper payload
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeString(uuid);
                buf.writeString(command);
                
                Identifier channel = new Identifier("interchat", "command");
                CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);
                
                Helper.sendPacket(packet);
                Helper.printChatMessage("Payload sent to " + user + "!");
                return 1;
            }
        }

        Helper.printChatMessage("Player " + user + " not found!");
        return 1;
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        String partial = builder.getRemaining().toLowerCase();
        getMinecraftClient().getNetworkHandler().getPlayerList().forEach(player -> {
            String name = player.getProfile().getName();
            if (name.toLowerCase().startsWith(partial)) {
                builder.suggest(name);
            }
        });
        return builder.buildFuture();
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: .interchat <user> <command>");
        return 1;
    }
}
