package dev.milow.aaigttracker.mixin;

import dev.milow.aaigttracker.client.app.AllAdvancementsIgtTrackerClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapSettingsMixin {
    @Shadow
    private boolean needsUpdate;

    @Unique
    private int aaigttracker$lastSettingsRevision = -1;

    @Inject(method = "extract", at = @At("HEAD"))
    private void aaigttracker$refreshLightingOnSettingsChange(LightmapRenderState state, float partialTick, CallbackInfo callbackInfo) {
        int revision = AllAdvancementsIgtTrackerClient.getVisualSettingsRevision();
        if (revision != this.aaigttracker$lastSettingsRevision) {
            // Refresh all lighting settings immediately, including toggles made while paused.
            this.needsUpdate = true;
            this.aaigttracker$lastSettingsRevision = revision;
        }
    }

    @Inject(method = "extract", at = @At("RETURN"))
    private void aaigttracker$applyFullbright(LightmapRenderState state, float partialTick, CallbackInfo callbackInfo) {
        if (AllAdvancementsIgtTrackerClient.isFullbrightEnabled() && state.needsUpdate) {
            // Raise the lightmap's ambient floor without changing gamma or adding potion effects.
            state.ambientColor = LightmapRenderStateExtractor.WHITE;
        }
    }
}
