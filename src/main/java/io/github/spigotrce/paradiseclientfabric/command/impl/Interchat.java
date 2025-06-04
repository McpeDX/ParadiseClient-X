package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class Interchat extends Command {
    public Interchat(MinecraftClient minecraftClient) {
        super("interchat", "Forces player commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(this.getName())
                .executes(this::incomplete)
                .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("user", StringArgumentType.word())
                        .suggests(this::suggestPlayers)
                        .executes(this::incomplete)
                        .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("command", StringArgumentType.greedyString())
                                .executes(this::sendPayload)));
    }

    private int sendPayload(CommandContext<?> context) {
        String user = StringArgumentType.getString(context, "user");
        String command = StringArgumentType.getString(context, "command");

        for (PlayerListEntry p : this.getMinecraftClient().getNetworkHandler().getPlayerList()) {
            if (p.getProfile().getName().equalsIgnoreCase(user)) {
                String uuid = p.getProfile().getId().toString();
                Helper.sendPacket(new CustomPayloadC2SPacket(new InterchatWriter(uuid, command)));
                Helper.printChatMessage("Payload sent!");
                return 1;
            }
        }

        Helper.printChatMessage("Player not found!");
        return 1;
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<?> context, SuggestionsBuilder builder) {
        String partial = builder.getRemaining().toLowerCase();
        this.getMinecraftClient().getNetworkHandler().getPlayerList().forEach(entry -> {
            String name = entry.getProfile().getName();
            if (name.toLowerCase().startsWith(partial)) {
                builder.suggest(name);
            }
        });
        return builder.buildFuture();
    }

    private int incomplete(CommandContext<?> context) {
        Helper.printChatMessage("Incomplete command!");
        return 1;
    }
}
