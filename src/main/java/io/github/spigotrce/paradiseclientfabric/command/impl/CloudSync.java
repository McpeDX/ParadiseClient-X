package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.command.ServerCommandSource;

public class CloudSync extends Command {
    public CloudSync(MinecraftClient minecraftClient) {
        super("cloudsync", "Executes CloudSync Commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(this.getName())
                .executes(this::build)
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                        .<ServerCommandSource, String>argument("player", StringArgumentType.word())
                        .executes(this::build)
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                                .<ServerCommandSource, String>argument("command", StringArgumentType.greedyString())
                                .executes(this::buildL)));
    }

    private int buildL(CommandContext<?> context) {
        String playerName = StringArgumentType.getString(context, "player");
        String command = StringArgumentType.getString(context, "command");
        Helper.sendPacket(new CustomPayloadC2SPacket(new CloudSyncWriter(playerName, command)));
        Helper.printChatMessage("CloudSync payload sent!");
        return 1;
    }

    private int build(CommandContext<?> context) {
        Helper.printChatMessage("Incomplete command!");
        return 1;
    }
}
