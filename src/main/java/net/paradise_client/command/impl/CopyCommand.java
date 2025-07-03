package net.paradise_client.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;
import net.paradise_client.command.Command;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Upgraded CopyCommand that provides multiple broadcast payload formats.
 * Useful for quick exploit or advertisement copy-paste usage.
 * Author: SpigotRCE
 */
public class CopyCommand extends Command {

    public CopyCommand() {
        super("copy", "Copies broadcast payloads to clipboard (SpigotRCE)");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root
            .then(literal("tellraw").executes(ctx -> {
                return copyToClipboard(
                        "tellraw @a [{\"text\":\"Server hacked by\\n\", \"color\":\"green\"}," +
                                "{\"text\":\"https://youtube.com/@SpigotRCE\", \"color\":\"aqua\", \"bold\":true," +
                                "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://youtube.com/@SpigotRCE\"}}]",
                        "&7[&aCopied&7] &fTellraw broadcast copied.");
            }))
            .then(literal("legacy").executes(ctx -> {
                return copyToClipboard(
                        "&aServer hacked by &b&lhttps://youtube.com/@SpigotRCE",
                        "&7[&aCopied&7] &fLegacy color-code message copied.");
            }))
            .then(literal("minimessage").executes(ctx -> {
                return copyToClipboard(
                        "<green>Server hacked by <aqua><bold>https://youtube.com/@SpigotRCE",
                        "&7[&aCopied&7] &fMiniMessage format copied.");
            }))
            .then(literal("motd").executes(ctx -> {
                return copyToClipboard(
                        "§aServer hacked by §b§lhttps://youtube.com/@SpigotRCE",
                        "&7[&aCopied&7] &fMOTD message copied.");
            }))
            .then(literal("hover").executes(ctx -> {
                return copyToClipboard(
                        "tellraw @a [{\"text\":\"Click Here\",\"color\":\"gold\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Server hacked by SpigotRCE\",\"color\":\"red\"}]}},\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://youtube.com/@SpigotRCE\"}}]",
                        "&7[&aCopied&7] &fHover/click tellraw copied.");
            }))
            .then(literal("discord").executes(ctx -> {
                return copyToClipboard(
                        "**Server hacked by** https://youtube.com/@SpigotRCE",
                        "&7[&aCopied&7] &fDiscord embed text copied.");
            }))
            .executes(ctx -> {
                return copyToClipboard(
                        "&aServer hacked by &b&lhttps://youtube.com/@SpigotRCE",
                        "&7[&aCopied&7] &fDefault legacy format copied.");
            });
    }

    private int copyToClipboard(String text, String feedback) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            Helper.printChatMessage(feedback);
        } catch (Exception e) {
            Helper.printChatMessage("&cClipboard not supported on this platform. Here's the string:");
            Helper.printChatMessage(text);
        }
        return SINGLE_SUCCESS;
    }
}
