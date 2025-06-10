package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import io.github.spigotrce.paradiseclientfabric.packet.InterchatPayloadPacket;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Interchat extends Command {
    public Interchat(MinecraftClient minecraftClient) {
        super("interchat", "Forces player commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .executes(this::showUsage)
            .then(
                LiteralArgumentBuilder.<CommandSource>argument("user", StringArgumentType.word())
                    .suggests(this::suggestPlayers)
                    .then(
                        LiteralArgumentBuilder.<CommandSource>argument("command", StringArgumentType.greedyString())
                            .executes(this::executeCommand)
                    )
            );
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        String username = context.getArgument("user", String.class);
        String command = context.getArgument("command", String.class);

        PlayerListEntry targetPlayer = findPlayer(username);
        if (targetPlayer == null) {
            Helper.printChatMessage("Player not found!");
            return 0;
        }

        UUID playerId = targetPlayer.getProfile().getId();
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
            new InterchatPayloadPacket(playerId.toString(), command)
        );
        
        Helper.sendPacket(packet);
        Helper.printChatMessage("Payload sent to " + username + "!");
        return 1;
    }

    private PlayerListEntry findPlayer(String username) {
        if (this.getMinecraftClient().getNetworkHandler() == null) {
            return null;
        }

        return this.getMinecraftClient().getNetworkHandler().getPlayerList()
            .stream()
            .filter(player -> player.getProfile().getName().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        String partialName;
        try {
            partialName = context.getArgument("user", String.class).toLowerCase();
        } catch (IllegalArgumentException e) {
            partialName = "";
        }

        if (this.getMinecraftClient().getNetworkHandler() == null) {
            return builder.buildFuture();
        }

        this.getMinecraftClient().getNetworkHandler().getPlayerList()
            .stream()
            .map(entry -> entry.getProfile().getName())
            .filter(name -> partialName.isEmpty() || name.toLowerCase().startsWith(partialName))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: ,interchat <player> <command>");
        return 1;
    }
}
