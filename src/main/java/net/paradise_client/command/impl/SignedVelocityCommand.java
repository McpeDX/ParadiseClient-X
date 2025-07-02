package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Collection;
import java.util.UUID;

public class SignedVelocityCommand extends Command {

    public SignedVelocityCommand() {
        super("signedvelocity", "Spoofs player-sent commands via Velocity plugin messaging.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(ctx -> {
            Helper.printChatMessage("§cUsage: .signedvelocity <player> <command>");
            return SINGLE_SUCCESS;
        }).then(argument("user", StringArgumentType.word())
            .suggests((ctx, builder) -> {
                String input = "";
                try {
                    input = ctx.getArgument("user", String.class);
                } catch (IllegalArgumentException ignored) {}

                final String partial = input.toLowerCase(); // ✅ Fixed here

                Collection<PlayerListEntry> playerList = getMinecraftClient().getNetworkHandler().getPlayerList();

                playerList.stream()
                        .map(p -> p.getProfile().getName())
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .forEach(builder::suggest);

                return builder.buildFuture();
            })
            .executes(ctx -> {
                Helper.printChatMessage("§cUsage: .signedvelocity <player> <command>");
                return SINGLE_SUCCESS;
            })
            .then(argument("command", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String targetName = ctx.getArgument("user", String.class);
                    String command = ctx.getArgument("command", String.class);

                    Collection<PlayerListEntry> playerList = getMinecraftClient().getNetworkHandler().getPlayerList();

                    PlayerListEntry targetPlayer = playerList.stream()
                            .filter(p -> p.getProfile().getName().equalsIgnoreCase(targetName))
                            .findFirst()
                            .orElse(null);

                    if (targetPlayer == null) {
                        Helper.printChatMessage("§cPlayer §7" + targetName + " §cnot found in tablist.");
                        return SINGLE_SUCCESS;
                    }

                    UUID uuid = targetPlayer.getProfile().getId();
                    Helper.sendPluginMessage("signedvelocity:main", out -> {
                        out.writeUTF(uuid.toString());
                        out.writeUTF("COMMAND_RESULT");
                        out.writeUTF("MODIFY");
                        out.writeUTF("/" + command);
                    });

                    Helper.printChatMessage("§aSpoofed command sent as §7" + targetName + "§a (UUID: " + uuid + ")");
                    return SINGLE_SUCCESS;
                })
            )
        );
    }
}
