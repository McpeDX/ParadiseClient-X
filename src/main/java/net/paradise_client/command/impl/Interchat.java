package net.paradise_client.command.impl;

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
import net.paradise_client.command.Command;
import net.paradise_client.packet.InterchatPacket;

import java.util.concurrent.CompletableFuture;

/**
 * Command: /interchat <user> <command>
 * Sends a command to be executed on behalf of the specified player using a custom payload.
 */
public class Interchat extends Command {

    public Interchat(MinecraftClient minecraftClient) {
        super("interchat", "Forces player commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        // Builds: /interchat <user> <command>
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .executes(this::buildL) // Incomplete command
            .then(
                argument("user", StringArgumentType.word())
                    .suggests(this::buildL)
                    .executes(this::buildL)
                    .then(
                        argument("command", StringArgumentType.greedyString())
                            .executes(this::build)
                    )
            );
    }

    // Executes the full command: /interchat <user> <command>
    private int build(CommandContext<?> context) {
        String user = context.getArgument("user", String.class);
        String command = context.getArgument("command", String.class);

        for (PlayerListEntry p : this.getMinecraftClient().getNetworkHandler().getPlayerList()) {
            if (p.getProfile().getName().equalsIgnoreCase(user)) {
                // Send the custom payload to proxy the command
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

    // Suggest player names for the <user> argument
    private CompletableFuture<Suggestions> buildL(CommandContext<?> ctx, SuggestionsBuilder builder) {
        String partialName;
        try {
            partialName = ctx.getArgument("user", String.class).toLowerCase();
        } catch (IllegalArgumentException ignored) {
            partialName = "";
        }

        if (partialName.isEmpty()) {
            // Suggest all player names
            this.getMinecraftClient().getNetworkHandler().getPlayerList()
                .forEach(entry -> builder.suggest(entry.getProfile().getName()));
        } else {
            // Suggest matching player names
            String finalPartialName = partialName;
            this.getMinecraftClient().getNetworkHandler().getPlayerList().stream()
                .map(entry -> entry.getProfile().getName())
                .filter(name -> name.toLowerCase().startsWith(finalPartialName))
                .forEach(builder::suggest);
        }

        return builder.buildFuture();
    }

    // Handles incomplete command usage (e.g., just /interchat)
    private int buildL(CommandContext<?> context) {
        Helper.printChatMessage("Usage: ,interchat <user> <command>");
        return 1;
    }
}
