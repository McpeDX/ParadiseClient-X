package io.github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.spigotrce.paradiseclientfabric.Helper;
import net.minecraft.command.CommandSource;
import io.github.spigotrce.paradiseclientfabric.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import io.github.spigotrce.paradiseclientfabric.packet.CloudSyncPayloadPacket;

// Replace CommandSource with Void if you're not using it in Fabric commands
public class CloudSync extends Command {

    public CloudSync(MinecraftClient minecraftClient) {
        super("cloudsync", "Executes CloudSync Commands", minecraftClient);
    }

    @Override
    public LiteralArgumentBuilder<Void> build() {
        return LiteralArgumentBuilder.<Void>literal(this.getName())
            .executes(this::handleMissingArguments)
            .then(
                com.mojang.brigadier.builder.RequiredArgumentBuilder.<Void, String>argument("player", StringArgumentType.word())
                    .then(
                        com.mojang.brigadier.builder.RequiredArgumentBuilder.<Void, String>argument("command", StringArgumentType.greedyString())
                            .executes(this::executeCloudSync)
                    )
            );
    }

    private int handleMissingArguments(CommandContext<Void> context) {
        Helper.printChatMessage("§cUsage: .cloudsync <player> <command>");
        return 1;
    }

    private int executeCloudSync(CommandContext<Void> context) {
        String player = StringArgumentType.getString(context, "player");
        String command = StringArgumentType.getString(context, "command");

        try {
            PacketByteBuf payload = new CloudSyncPayloadPacket(player, command); // must extend PacketByteBuf or wrap it
            CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(
                new Identifier("paradiseclientfabric", "cloudsync"), // Custom channel
                payload
            );
            Helper.sendPacket(packet);

            Helper.printChatMessage("§aCloudSync payload sent to §f" + player + "§a!");
        } catch (Exception e) {
            Helper.printChatMessage("§cFailed to send CloudSync payload: §f" + e.getMessage());
        }

        return 1;
    }
   }
