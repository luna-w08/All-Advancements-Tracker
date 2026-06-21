package dev.milow.aaigttracker.mixin;

import dev.milow.aaigttracker.client.app.AllAdvancementsIgtTrackerClient;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDarknessMixin {
    @Inject(method = "getEffectBlendFactor", at = @At("HEAD"), cancellable = true)
    private void aaigttracker$disableDarkness(
            Holder<MobEffect> effect,
            float partialTick,
            CallbackInfoReturnable<Float> callbackInfo
    ) {
        if (AllAdvancementsIgtTrackerClient.isNoDarknessEnabled() && MobEffects.DARKNESS.equals(effect)) {
            callbackInfo.setReturnValue(0.0F);
        }
    }
}
