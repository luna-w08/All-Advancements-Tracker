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
public abstract class LightmapFullbrightMixin {
    @Shadow
    private boolean needsUpdate;

    @Unique
    private boolean aaigttracker$fullbrightWasEnabled;

    @Inject(method = "extract", at = @At("HEAD"))
    private void aaigttracker$refreshLightingOnToggle(LightmapRenderState state, float partialTick, CallbackInfo callbackInfo) {
        boolean enabled = AllAdvancementsIgtTrackerClient.isFullbrightEnabled();
        if (enabled != this.aaigttracker$fullbrightWasEnabled) {
            // Re-extract vanilla values on either transition, including while paused.
            this.needsUpdate = true;
            this.aaigttracker$fullbrightWasEnabled = enabled;
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
