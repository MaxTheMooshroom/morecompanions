package net.qiyanamark.companionpouch.mixins;

import java.util.Optional;

import iskallia.vault.init.ModConfigs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.qiyanamark.companionpouch.helper.HelperCompanions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;
import top.theillusivec4.curios.api.SlotResult;

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
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem)
            .map(SlotResult::stack);

        cir.setReturnValue(result);
        cir.cancel();
    }

    @Inject(
            at = @At("HEAD"),
            method = "grantVaultCompletionXP(Lnet/minecraft/world/entity/player/Player;I)V",
            cancellable = true
    )
    private static void grantVaultCompletionXP(Player player, int experience, CallbackInfo ci) {
        if (!player.level.isClientSide) {
            HelperCompanions.getCompanions(player).stream()
                    .filter(stack -> CompanionItem.isActive(stack) && CompanionItem.isOwner(stack, player))
                    .forEach((stack) -> {
                        int xp = Math.round((float)experience * ModConfigs.COMPANIONS.getCompletionXpShare());
                        if (xp > 0) {
                            CompanionItem.addCompanionXP(stack, xp);
                        }
                    });
        }
        ci.cancel();
    }
}
