package dev.milow.aaigttracker.mixin;

import dev.milow.aaigttracker.client.app.AllAdvancementsIgtTrackerClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void aaigttracker$disableFog(
            Camera camera,
            int renderDistance,
            DeltaTracker deltaTracker,
            float darkenWorldAmount,
            ClientLevel level,
            CallbackInfoReturnable<FogData> callbackInfo
    ) {
        if (!AllAdvancementsIgtTrackerClient.isNoFogEnabled()) {
            return;
        }

        FogData fogData = callbackInfo.getReturnValue();
        if (fogData == null) {
            return;
        }

        float distance = Math.max(1024.0F, renderDistance * 32.0F);
        fogData.environmentalStart = distance;
        fogData.renderDistanceStart = distance;
        fogData.environmentalEnd = distance + 1.0F;
        fogData.renderDistanceEnd = distance + 1.0F;
        fogData.skyEnd = distance + 1.0F;
        fogData.cloudEnd = distance + 1.0F;
    }
}
