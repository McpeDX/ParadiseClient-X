package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.paradise_client.Constants;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Random;

/**
 * Command to spam chat commands with delay, repetition, and plugin bypass options.
 * Supports chained commands, anti-kick, and plugin-specific spoof modes.
 */
public class SpamCommand extends Command {
    public static volatile boolean isRunning = false;
    private Thread spamThread;

    public SpamCommand() {
        super("spam", "Spams a command with delay and repeat count, with plugin bypass modes.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.then(literal("stop")
                .executes(ctx -> {
                    if (isRunning) {
                        isRunning = false;
                        Helper.printChatMessage("§cSpam stopped.");
                    } else {
                        Helper.printChatMessage("§7No spam is currently running.");
                    }
                    return SINGLE_SUCCESS;
                }))
            .then(argument("repeat", IntegerArgumentType.integer(1))
                .then(argument("delay", IntegerArgumentType.integer(0))
                    .then(argument("bypassMode", StringArgumentType.word()) // normal, essx, chatcontrol, randomized
                        .then(argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                int repeat = ctx.getArgument("repeat", Integer.class);
                                int delay = ctx.getArgument("delay", Integer.class);
                                String mode = ctx.getArgument("bypassMode", String.class).toLowerCase();
                                String inputCommand = ctx.getArgument("command", String.class);

                                if (isRunning) {
                                    Helper.printChatMessage("§cAlready spamming. Use §e/spam stop§c first.");
                                    return SINGLE_SUCCESS;
                                }

                                isRunning = true;
                                String cmd = inputCommand.startsWith("/") ? inputCommand.substring(1) : inputCommand;
                                Random random = new Random();
                                ClientPlayNetworkHandler net = getMinecraftClient().getNetworkHandler();

                                spamThread = new Thread(() -> {
                                    Helper.printChatMessage("§aSpam started: §f" + repeat + "x §adelay: " + delay + "ms §7[Mode: " + mode + "]");
                                    int watchdogCounter = 0;

                                    for (int i = 0; i < repeat && isRunning; i++) {
                                        if (getMinecraftClient().world == null || getMinecraftClient().player == null) {
                                            Helper.printChatMessage("§cDisconnected from server. Stopping spam.");
                                            isRunning = false;
                                            return;
                                        }

                                        String[] splitCmds = cmd.split(";");
                                        for (String part : splitCmds) {
                                            if (!isRunning) break;

                                            String finalCmd = applyBypassMode(part.trim(), mode, random);
                                            try {
                                                net.sendChatCommand(finalCmd);
                                                Constants.LOGGER.info("[SpamCommand] Sent: /" + finalCmd);
                                            } catch (Exception e) {
                                                Constants.LOGGER.error("Failed to send spam command", e);
                                            }

                                            try {
                                                Thread.sleep(delay + random.nextInt(35)); // jitter
                                            } catch (InterruptedException ignored) {}
                                        }

                                        // Anti-kick watchdog (every ~6 seconds)
                                        if (++watchdogCounter % 12 == 0) {
                                            try {
                                                net.sendChatCommand("ping");
                                            } catch (Exception ignored) {}
                                        }
                                    }

                                    isRunning = false;
                                    Helper.printChatMessage("§eSpam task completed.");
                                }, "SpamCommand-Thread");

                                spamThread.start();
                                return SINGLE_SUCCESS;
                            })))));
    }

    /**
     * Applies plugin-specific bypass logic to the given command.
     */
    private String applyBypassMode(String command, String mode, Random random) {
        switch (mode) {
            case "essx": // EssentialsX noise
                return command + " #" + random.nextInt(1000);
            case "chatcontrol":
                return command + " |" + random.nextInt(10000);
            case "randomized":
                return command + " " + generateRandomSuffix(random, 5);
            case "normal":
            default:
                return command;
        }
    }

    /**
     * Generates a random alphanumeric suffix.
     */
    private String generateRandomSuffix(Random random, int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
