package net.paradise_client.inject.mixin.etc;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.paradise_client.ParadiseClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    public abstract void sendMessage(Text message, boolean overlay);

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ParadiseClient.MISC_MOD.delayedMessages.forEach(this::sendMessage);
        ParadiseClient.MISC_MOD.delayedMessages.clear();
    }

    @Unique
    private void sendMessage(Text message) {
        this.sendMessage(message, false);
    }
}
