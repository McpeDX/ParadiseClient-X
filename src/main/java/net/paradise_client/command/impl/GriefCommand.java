package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Objects;

public class GriefCommand extends Command {

    public GriefCommand() {
        super("grief", "Multiple grief commands");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root
            .then(literal("tpall")
                .executes(context -> {
                    sendChatCommands(
                        "tpall",
                        "etpall",
                        "minecraft:tp @a @p",
                        "tp @a @p"
                    );
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("fill")
                .then(literal("air").executes(context -> runFill("air")))
                .then(literal("lava").executes(context -> runFill("lava")))
                .then(literal("tnt").executes(context -> runFill("tnt")))
                .executes(context -> {
                    error("fill <air|lava|tnt>");
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("sphere")
                .then(literal("air").executes(context -> runSphere("air", 10)))
                .then(literal("lava").executes(context -> runSphere("lava", 10)))
                .then(literal("tnt").executes(context -> runSphere("tnt", 10)))
                .executes(context -> {
                    error("sphere <air|lava|tnt>");
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("walls")
                .then(literal("bedrock").executes(context -> {
                    sendChatCommand("/walls bedrock");
                    return SINGLE_SUCCESS;
                }))
                .then(literal("lava").executes(context -> {
                    sendChatCommand("/walls lava");
                    return SINGLE_SUCCESS;
                }))
                .executes(context -> {
                    error("walls <bedrock|lava>");
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("cyl")
                .then(literal("lava").executes(context -> {
                    sendChatCommand("/cyl lava 5 10");
                    return SINGLE_SUCCESS;
                }))
                .then(literal("tnt").executes(context -> {
                    sendChatCommand("/cyl tnt 5 10");
                    return SINGLE_SUCCESS;
                }))
                .executes(context -> {
                    error("cyl <lava|tnt>");
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("set")
                .then(literal("lava").executes(context -> {
                    sendChatCommand("//set lava");
                    return SINGLE_SUCCESS;
                }))
                .then(literal("tnt").executes(context -> {
                    sendChatCommand("//set tnt");
                    return SINGLE_SUCCESS;
                }))
                .executes(context -> {
                    error("set <lava|tnt>");
                    return SINGLE_SUCCESS;
                })
            )

            .then(literal("undo")
                .executes(context -> {
                    sendChatCommand("//undo");
                    return SINGLE_SUCCESS;
                })
            )

            .executes(context -> {
                error("<tpall|fill|sphere|walls|cyl|set|undo>");
                return SINGLE_SUCCESS;
            });
    }

    private int runFill(String block) {
        sendChatCommand(String.format("minecraft:fill ~12 ~12 ~12 ~-12 ~-12 ~-12 %s", block));
        return SINGLE_SUCCESS;
    }

    private int runSphere(String block, int radius) {
        sendChatCommand(String.format("/sphere %s %d", block, radius));
        return SINGLE_SUCCESS;
    }

    private void sendChatCommand(String command) {
        ClientPlayNetworkHandler handler = getHandler();
        if (handler != null) {
            handler.sendChatCommand(command);
        } else {
            Helper.printChatMessage("§4§lError: NetworkHandler is null");
        }
    }

    private void sendChatCommands(String... commands) {
        for (String command : commands) {
            sendChatCommand(command);
            sleep(100); // slight delay between commands
        }
    }

    private ClientPlayNetworkHandler getHandler() {
        return MinecraftClient.getInstance().getNetworkHandler();
    }

    private void error(String usage) {
        Helper.printChatMessage("§4§lUsage: /" + getName() + " " + usage);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
