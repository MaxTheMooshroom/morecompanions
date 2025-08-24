package net.qiyanamark.companionactivator.mixin;

import net.qiyanamark.companionactivator.server.ActivationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "iskallia.vault.world.data.PlayerCompanionData$CompanionData", remap = false)
public abstract class MixinCompanionDataSetActive {

    @Inject(method = "setActive", at = @At("HEAD"), cancellable = true)
    private void onSetActive(boolean active, CallbackInfo ci) {
        try {
            if (!active && Boolean.TRUE.equals(ActivationHandler.GUARD.get())) {

                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
