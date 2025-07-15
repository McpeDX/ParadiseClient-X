package net.paradise_client.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.util.Collection;

public class BungeeGuardCommand extends Command {

    public BungeeGuardCommand() {
        super("bungeeguard", "Checks for BungeeGuard token");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSource> context) {
        checkForBungeeGuardToken();
        return 1;
    }

    private void checkForBungeeGuardToken() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || client.getNetworkHandler() == null) {
            Helper.printChatMessage("§cYou must be connected to a server!");
            return;
        }

        GameProfile profile = player.getGameProfile();
        PropertyMap properties = profile.getProperties();

        Property tokenProperty = null;
        Collection<Property> values = properties.values();
        for (Property property : values) {
            String name = getPrivateField(property, "name");
            if ("bungeeguard-token".equals(name)) {
                tokenProperty = property;
                break;
            }
        }

        if (tokenProperty != null) {
            String token = getPrivateField(tokenProperty, "value");
            if (token != null) {
                copyToClipboard(token);
                Helper.printChatMessage("§aBungeeGuard token found: §b§n" + token);
            } else {
                Helper.printChatMessage("§cCould not read token value");
            }
        } else {
            Helper.printChatMessage("§4No BungeeGuard token found");
        }
    }

    private void copyToClipboard(String token) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(token), null);
            Helper.printChatMessage("§aToken copied to clipboard");
        } catch (Exception e) {
            Helper.printChatMessage("§cFailed to copy token to clipboard");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private String getPrivateField(Property property, String fieldName) {
        try {
            Field field = Property.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(property);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
