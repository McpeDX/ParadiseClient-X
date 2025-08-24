package net.paradise_client.inject.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.paradise_client.Constants;
import net.paradise_client.ParadiseClient;
import net.paradise_client.protocol.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Mixin for the InGameHud class to inject custom HUD rendering behavior.
 * Displays FPS, TPS, Ping, Coordinates, etc.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    @Final
    private PlayerListHud playerListHud;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    private long lastTime = System.currentTimeMillis();
    private int tickCount = 0;
    private double tps = 20.0; // default 20 TPS

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, CallbackInfo ci) {
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client == null) {
            return;
        }

        ArrayList<String> text = new ArrayList<>();

        text.add(Constants.WINDOW_TITLE);
        text.add("Server " + ((!Objects.isNull(this.client.getCurrentServerEntry()) && ParadiseClient.HUD_MOD.showServerIP) ? this.client.getCurrentServerEntry().address : "Hidden"));
        text.add("Engine " + (Objects.isNull(this.client.getNetworkHandler()) ? "" : this.client.getNetworkHandler().getBrand()));
        text.add("FPS " + this.client.getCurrentFps());
        text.add("TPS " + String.format("%.2f", getTPS())); // new TPS line
        text.add("Protocol " + ProtocolVersion.getProtocolVersion(ParadiseClient.NETWORK_CONFIGURATION.protocolVersion).getVersionIntroducedIn());
        text.add("Players " + this.client.getNetworkHandler().getPlayerList().size());

        // Extra: show ping
        if (this.client.player != null && this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid()) != null) {
            int ping = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid()).getLatency();
            text.add("Ping " + ping + "ms");
        }

        // Extra: show coordinates
        PlayerEntity player = this.client.player;
        if (player != null) {
            text.add(String.format("XYZ %.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ()));
        }

        int i = 0;
        for (String s : text) {
            renderTextRed(context, s, 5, 5 + this.client.textRenderer.fontHeight * i);
            i++;
        }

        ParadiseClient.NOTIFICATION_MANAGER.drawNotifications(context, this.client.textRenderer);
    }

    /**
     * Simple client-side TPS calculator.
     */
    @Unique
    private double getTPS() {
        tickCount++;
        long now = System.currentTimeMillis();
        if (now - lastTime >= 1000) {
            tps = tickCount / ((now - lastTime) / 1000.0);
            if (tps > 20.0) tps = 20.0; // clamp
            tickCount = 0;
            lastTime = now;
        }
        return tps;
    }

    @SuppressWarnings("SameParameterValue")
    @Unique
    private void renderTextRed(DrawContext ct, String s, int x, int y) {
        char[] chars = s.toCharArray();
        int i = 0;
        for (char aChar : chars) {
            String c = String.valueOf(aChar);
            ct.drawText(this.client.textRenderer, c, x + i, y, 0xFFFF0000, false);
            i += getTextRenderer().getWidth(c);
        }
    }

    @Inject(method = "renderPlayerList", at = @At("HEAD"), cancellable = true)
    private void renderPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        assert this.client.world != null;
        Scoreboard scoreboard = this.client.world.getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
        if (!this.client.options.playerListKey.isPressed() || this.client.isInSingleplayer() && Objects.requireNonNull(this.client.player).networkHandler.getListedPlayerListEntries().size() <= 1 && scoreboardObjective == null) {
            this.playerListHud.setVisible(false);
            if (ParadiseClient.HUD_MOD.showPlayerList)
                this.renderTAB(context, context.getScaledWindowWidth(), scoreboard, scoreboardObjective);
        } else
            this.renderTAB(context, context.getScaledWindowWidth(), scoreboard, scoreboardObjective);
        ci.cancel();
    }

    @Unique
    private void renderTAB(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective scoreboardObjective) {
        this.playerListHud.setVisible(true);
        this.playerListHud.render(context, scaledWindowWidth, scoreboard, scoreboardObjective);
    }
            }
