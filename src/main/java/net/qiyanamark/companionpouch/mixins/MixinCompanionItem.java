package net.qiyanamark.companionpouch.mixins;

import java.util.Optional;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

@Mixin(value = CompanionItem.class, remap = false)
public abstract class MixinCompanionItem {
    @Inject(
        at = @At("HEAD"),
        method = "getCompanion(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
        cancellable = true
    )
    private static void getCompanion(LivingEntity entity, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (entity.isSpectator()) {
            cir.setReturnValue(Optional.empty());
            cir.cancel();
            return;
        }

        Optional<ItemStack> result = CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem || slot.getItem() instanceof ItemPouchCompanion)
            .map(slot -> slot.stack());

        cir.setReturnValue(result);
        cir.cancel();
    }
}
