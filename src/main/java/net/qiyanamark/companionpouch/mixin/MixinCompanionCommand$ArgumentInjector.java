package net.qiyanamark.companionpouch.mixin;

import iskallia.vault.command.Command;
import iskallia.vault.command.CompanionCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CompanionCommand.class, remap = false)
public abstract class MixinCompanionCommand$ArgumentInjector extends Command {
//    @WrapOperation(
//        method = "build",
//        at = @At(value = "INVOKE", target = "")
//    )
}
