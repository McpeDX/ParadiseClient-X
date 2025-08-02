package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.paradise_client.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.paradise_client.command.Command;
import net.paradise_client.packet.InterchatPacket;

import java.util.concurrent.CompletableFuture;

/**
 * Command: /interchat <user> <command>
 * Sends a command to be executed on behalf of the specified player using a custom payload.
 */
public class Interchat extends Command {

    private final MinecraftClient client;

    public Interchat(MinecraftClient client) {
        super("interchat", "Sends a command on behalf of another player", false);
        this.client = client;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::buildL) // Incomplete command
            .then(argument("user", StringArgumentType.word())
                .suggests(this::suggestPlayerNames)
                .executes(this::buildL)
                .then(argument("command", StringArgumentType.greedyString())
                    .executes(this::executeCommand)));
    }

    private int executeCommand(CommandContext<?> context) {
        String user = context.getArgument("user", String.class);
        String command = context.getArgument("command", String.class);

        for (PlayerListEntry p : this.client.getNetworkHandler().getPlayerList()) {
            if (p.getProfile().getName().equalsIgnoreCase(user)) {
                Helper.sendPacket(new CustomPayloadC2SPacket(
                    new InterchatPacket(p.getProfile().getId().toString(), command)
                ));
                Helper.printChatMessage("Payload sent!");
                return 1;
            }
        }

        Helper.printChatMessage("Player not found!");
        return 1;
    }

    private CompletableFuture<Suggestions> suggestPlayerNames(CommandContext<?> ctx, SuggestionsBuilder builder) {
        String partialName;
        try {
            partialName = ctx.getArgument("user", String.class).toLowerCase();
        } catch (IllegalArgumentException ignored) {
            partialName = "";
        }

        for (PlayerListEntry entry : this.client.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (partialName.isEmpty() || name.toLowerCase().startsWith(partialName)) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }

    private int buildL(CommandContext<?> context) {
        Helper.printChatMessage("Usage: ,interchat <user> <command>");
        return 1;
    }
}
